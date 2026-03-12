package ru.yandex.practicum.analyzer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.ScenarioEvaluator;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

	private final ScenarioEvaluator scenarioEvaluator;
	private final String bootstrapServers;
	private final String groupId;
	private final String topic;
	private final String autoOffsetReset;
	private final String maxPollRecords;

	private final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
	private volatile boolean running = true;

	public SnapshotProcessor(
			ScenarioEvaluator scenarioEvaluator,
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.kafka.consumer.snapshot-group-id}") String groupId,
			@Value("${kafka.topics.snapshots}") String topic,
			@Value("${spring.kafka.consumer.snapshot.auto-offset-reset:earliest}") String autoOffsetReset,
			@Value("${spring.kafka.consumer.snapshot.max-poll-records:100}") String maxPollRecords) {
		this.scenarioEvaluator = scenarioEvaluator;
		this.bootstrapServers = bootstrapServers;
		this.groupId = groupId;
		this.topic = topic;
		this.autoOffsetReset = autoOffsetReset;
		this.maxPollRecords = maxPollRecords;
	}

	@Override
	public void run() {
		KafkaConsumer<Void, SensorsSnapshotAvro> consumer = createConsumer();
		try {
			consumer.subscribe(Collections.singletonList(topic));
			log.info("SnapshotProcessor подписан на топик: {}", topic);
			while (running) {
				ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(POLL_TIMEOUT);

				for (org.apache.kafka.clients.consumer.ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
					SensorsSnapshotAvro snapshot = record.value();
					log.info("Получен снапшот для хаба: {}", snapshot.getHubId());
					scenarioEvaluator.evaluate(snapshot);
				}

				consumer.commitSync();
			}
		} catch (Exception e) {
			log.error("Ошибка в SnapshotProcessor", e);
		} finally {
			consumer.close();
			log.info("SnapshotProcessor остановлен");
		}
	}

	private KafkaConsumer<Void, SensorsSnapshotAvro> createConsumer() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"deserializer.SensorsSnapshotDeserializer");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		return new KafkaConsumer<>(props);
	}

	public void start() {
		run();
	}
	public void stop() {
		running = false;
	}
}
