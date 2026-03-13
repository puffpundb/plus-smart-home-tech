package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import deserializer.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import serializer.CommonSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class AggregationStarter {

	private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();
	private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

	private final String bootstrapServers;
	private final String groupId;

	private final String keyDeserializer;
	private final String valueDeserializer;
	private final String autoOffsetReset;
	private final String maxPollRecords;
	private final String maxPollIntervalMs;
	private final String fetchMaxBytes;
	private final String maxPartitionFetchBytes;

	private final Duration POLL_TIMEOUT;
	private final String TOPIC_SENSORS;
	private final String TOPIC_SNAPSHOTS;
	private final int COMMIT_INTERVAL;

	public AggregationStarter(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.kafka.consumer.group-id}") String groupId,
			@Value("${spring.kafka.consumer.key-deserializer}") String keyDeserializer,
			@Value("${spring.kafka.consumer.value-deserializer}") String valueDeserializer,
			@Value("${spring.kafka.consumer.auto-offset-reset}") String autoOffsetReset,
			@Value("${spring.kafka.consumer.max-poll-records}") String maxPollRecords,
			@Value("${spring.kafka.consumer.max-poll-interval-ms}") String maxPollIntervalMs,
			@Value("${spring.kafka.consumer.fetch-max-bytes}") String fetchMaxBytes,
			@Value("${spring.kafka.consumer.max-partition-fetch-bytes}") String maxPartitionFetchBytes,
			@Value("${kafka.topics.sensors}") String topicSensors,
			@Value("${kafka.topics.snapshots}") String topicSnapshots,
			@Value("${spring.kafka.consumer.poll-timeout:1}") int pollTimeout,
			@Value("${spring.kafka.consumer.commit-interval:10}") int commitInterval) {
		this.bootstrapServers = bootstrapServers;
		this.groupId = groupId;
		this.keyDeserializer = keyDeserializer;
		this.valueDeserializer = valueDeserializer;
		this.autoOffsetReset = autoOffsetReset;
		this.maxPollRecords = maxPollRecords;
		this.maxPollIntervalMs = maxPollIntervalMs;
		this.fetchMaxBytes = fetchMaxBytes;
		this.maxPartitionFetchBytes = maxPartitionFetchBytes;
		this.TOPIC_SENSORS = topicSensors;
		this.TOPIC_SNAPSHOTS = topicSnapshots;
		this.POLL_TIMEOUT = Duration.ofSeconds(pollTimeout);
		this.COMMIT_INTERVAL = commitInterval;
	}

	public void start() {
		KafkaConsumer<String, SensorEventAvro> consumer = createConsumer();
		KafkaProducer<String, SensorsSnapshotAvro> producer = createProducer();

		boolean flag = true;

		try {
			consumer.subscribe(Collections.singletonList(TOPIC_SENSORS));

			while (flag) {
				ConsumerRecords<String, SensorEventAvro> records = consumer.poll(POLL_TIMEOUT);

				int count = 0;
				for (ConsumerRecord<String, SensorEventAvro> record : records) {
					SensorEventAvro event = record.value();
					Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(event);

					updatedSnapshot.ifPresent(snapshot -> {
						ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
								new ProducerRecord<>(TOPIC_SNAPSHOTS, null, snapshot.getHubId(), snapshot);

						producer.send(producerRecord, (metadata, exception) -> {
							if (exception != null) {
								log.error("Не удалось отправить снапшот для хаба: {}", snapshot.getHubId(), exception);
							} else {
								log.info("Снапшот успешно записан для хаба: {} (partition: {}, offset: {})",
										snapshot.getHubId(), metadata.partition(), metadata.offset());
							}
						});
					});

					manageOffsets(record, count, consumer);
					count++;
				}

				consumer.commitAsync(currentOffsets, commitCallback());
			}
		} catch (WakeupException ignored) {
			flag = false;
		} catch (Exception e) {
			log.error("Ошибка во время обработки событий", e);
		} finally {
			producer.flush();
			consumer.commitSync(currentOffsets);
			consumer.close();
			producer.close();
		}
	}

	private KafkaConsumer<String, SensorEventAvro> createConsumer() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetchMaxBytes);
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
		return new KafkaConsumer<>(props);
	}

	private KafkaProducer<String, SensorsSnapshotAvro> createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CommonSerializer.class.getName());
		return new KafkaProducer<>(props);
	}

	private void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count, KafkaConsumer<String, SensorEventAvro> consumer) {
		TopicPartition partition = new TopicPartition(record.topic(), record.partition());
		OffsetAndMetadata offset = new OffsetAndMetadata(record.offset() + 1);
		currentOffsets.put(partition, offset);

		if (count % COMMIT_INTERVAL == 0) {
			consumer.commitAsync(currentOffsets, commitCallback());
		}
	}

	private OffsetCommitCallback commitCallback() {
		return (offsets, exception) -> {
			if (exception != null) {
				log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
			}
		};
	}

	private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
		String hubId = event.getHubId();
		String sensorId = event.getId();

		SensorsSnapshotAvro snapshot = snapshots.get(hubId);
		if (snapshot == null) {
			snapshot = createNewSnapshot(hubId);
			snapshots.put(hubId, snapshot);
		}

		Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
		SensorStateAvro oldState = sensorsState.get(sensorId);

		if (oldState != null) {
			if (oldState.getTimestamp().toEpochMilli() > event.getTimestamp()) {
				return Optional.empty();
			}

			if (isDataEqual(oldState.getData(), event.getPayload())) {
				return Optional.empty();
			}
		}

		SensorStateAvro newState = new SensorStateAvro();
		newState.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));
		newState.setData(event.getPayload());

		sensorsState.put(sensorId, newState);

		snapshot.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));

		return Optional.of(snapshot);
	}

	private SensorsSnapshotAvro createNewSnapshot(String hubId) {
		SensorsSnapshotAvro snapshot = new SensorsSnapshotAvro();
		snapshot.setHubId(hubId);
		snapshot.setTimestamp(Instant.ofEpochSecond(0L));
		snapshot.setSensorsState(new HashMap<>());
		return snapshot;
	}

	private boolean isDataEqual(Object data1, Object data2) {
		if (data1 == null && data2 == null) return true;
		if (data1 == null || data2 == null) return false;

		if (!data1.getClass().equals(data2.getClass())) return false;

		if (data1 instanceof SpecificRecordBase && data2 instanceof SpecificRecordBase) {
			SpecificRecordBase r1 = (SpecificRecordBase) data1;
			SpecificRecordBase r2 = (SpecificRecordBase) data2;

			Schema schema = r1.getSchema();
			for (Schema.Field field : schema.getFields()) {
				int pos = field.pos();
				Object f1 = r1.get(pos);
				Object f2 = r2.get(pos);

				if (!Objects.equals(f1, f2)) {
					return false;
				}
			}
			return true;
		}

		return data1.equals(data2);
	}
}