package ru.yandex.practicum.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.Metadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import serializer.CommonSerializer;

import java.util.Properties;

@Slf4j
@Service
public class KafkaTelemetryProducer {
	private final KafkaProducer<String, SpecificRecordBase> producer;
	private final String sensorTopic;
	private final String hubTopic;

	public KafkaTelemetryProducer(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
								  @Value("${kafka.topics.sensors}") String sensorTopic,
								  @Value("${kafka.topics.hubs}") String hubTopic) {

		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CommonSerializer.class.getName());

		this.producer = new KafkaProducer<>(props);
		this.sensorTopic = sensorTopic;
		this.hubTopic = hubTopic;
	}

	public void sendSensor(SpecificRecordBase e) {
		ProducerRecord<String, SpecificRecordBase> rec = new ProducerRecord<>(sensorTopic, e);
		log.info("KafkaTelemetryProducer: record - {}", rec);

		producer.send(rec, this::logCallback);
	}

	public void sendHub(SpecificRecordBase h) {
		ProducerRecord<String, SpecificRecordBase> rec = new ProducerRecord<>(hubTopic, h);
		log.info("KafkaTelemetryProducer: record - {}", rec);

		producer.send(rec, this::logCallback);
	}

	@PreDestroy
	public void closeProducer() {
		producer.flush();
		producer.close();
	}

	private void logCallback(RecordMetadata md, Exception e) {
		if (e == null) log.info("Запись передана: topic = {}, partition = {}, offset = {}",
				md.topic(), md.partition(), md.offset());
		else log.error("Ошибка передачи записи", e);
	}
}
