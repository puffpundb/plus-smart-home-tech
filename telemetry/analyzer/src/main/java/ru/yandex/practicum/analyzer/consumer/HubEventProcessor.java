package ru.yandex.practicum.analyzer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

	private final HubEventHandler hubEventHandler;
	private final String bootstrapServers;
	private final String groupId;
	private final String topic;
	private final String autoOffsetReset;
	private final String maxPollRecords;

	private final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
	private volatile boolean running = true;

	public HubEventProcessor(
			HubEventHandler hubEventHandler,
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.kafka.consumer.hub-event-group-id}") String groupId,
			@Value("${kafka.topics.hub-events}") String topic,
			@Value("${spring.kafka.consumer.hub-event.auto-offset-reset:earliest}") String autoOffsetReset,
			@Value("${spring.kafka.consumer.hub-event.max-poll-records:10}") String maxPollRecords) {
		this.hubEventHandler = hubEventHandler;
		this.bootstrapServers = bootstrapServers;
		this.groupId = groupId;
		this.topic = topic;
		this.autoOffsetReset = autoOffsetReset;
		this.maxPollRecords = maxPollRecords;
	}

	@Override
	public void run() {
		KafkaConsumer<String, HubEventAvro> consumer = createConsumer();
		try {
			consumer.subscribe(Collections.singletonList(topic));
			log.info("HubEventProcessor подписан на топик: {}", topic);

			while (running) {
				ConsumerRecords<String, HubEventAvro> records = consumer.poll(POLL_TIMEOUT);

				for (ConsumerRecord<String, HubEventAvro> record : records) {
					log.info("Получено событие хаба из топика: {}", record.topic());
					hubEventHandler.handle(record);
				}

				consumer.commitSync();
			}
		} catch (Exception e) {
			log.error("Ошибка в HubEventProcessor", e);
		} finally {
			consumer.close();
			log.info("HubEventProcessor остановлен");
		}
	}

	private KafkaConsumer<String, HubEventAvro> createConsumer() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"deserializer.HubEventDeserializer");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		return new KafkaConsumer<>(props);
	}

	public void stop() {
		running = false;
	}
}