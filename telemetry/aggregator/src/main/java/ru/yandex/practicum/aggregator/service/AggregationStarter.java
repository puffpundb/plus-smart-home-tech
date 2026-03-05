package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.deserializer.SensorEventDeserializer;
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

	private final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
	private final String TOPIC_SENSORS;
	private final String TOPIC_SNAPSHOTS;
	private final int COMMIT_INTERVAL = 10;

	public AggregationStarter(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.kafka.consumer.group-id}") String groupId,
			@Value("${kafka.topics.sensors}") String topicSensors,
			@Value("${kafka.topics.snapshots}") String topicSnapshots) {
		this.bootstrapServers = bootstrapServers;
		this.groupId = groupId;
		this.TOPIC_SENSORS = topicSensors;
		this.TOPIC_SNAPSHOTS = topicSnapshots;
	}

	public void start() {
		KafkaConsumer<Void, SensorEventAvro> consumer = createConsumer();
		KafkaProducer<String, SensorsSnapshotAvro> producer = createProducer();

		try {
			consumer.subscribe(Collections.singletonList(TOPIC_SENSORS));

			while (true) {
				ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(POLL_TIMEOUT);

				int count = 0;
				for (ConsumerRecord<Void, SensorEventAvro> record : records) {
					SensorEventAvro event = record.value();
					Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(event);

					updatedSnapshot.ifPresent(snapshot -> {
						ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
								new ProducerRecord<>(TOPIC_SNAPSHOTS, null, snapshot.getHubId(), snapshot);
						producer.send(producerRecord);
						log.info("Снапшот отправлен для хаба: {}", snapshot.getHubId());
					});

					manageOffsets(record, count, consumer);
					count++;
				}

				consumer.commitAsync(currentOffsets, commitCallback());
			}
		} catch (WakeupException ignored) {
		} catch (Exception e) {
			log.error("Ошибка во время обработки событий", e);
		} finally {
			producer.flush();
			consumer.commitSync(currentOffsets);
			consumer.close();
			producer.close();
		}
	}

	private KafkaConsumer<Void, SensorEventAvro> createConsumer() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "3072000");
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "307200");
		return new KafkaConsumer<>(props);
	}

	private KafkaProducer<String, SensorsSnapshotAvro> createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CommonSerializer.class.getName());
		return new KafkaProducer<>(props);
	}

	private void manageOffsets(ConsumerRecord<Void, SensorEventAvro> record, int count, KafkaConsumer<Void, SensorEventAvro> consumer) {
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
			if (oldState.getTimestamp().toEpochMilli() >= event.getTimestamp()) {
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
		if (data1 == null && data2 == null) {
			return true;
		}
		if (data1 == null || data2 == null) {
			return false;
		}
		return data1.equals(data2);
	}
}