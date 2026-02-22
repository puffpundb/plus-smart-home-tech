package ru.yandex.practicum.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import serializer.HubEventAvroSerializer;
import serializer.SensorEventAvroSerializer;

import java.util.Properties;

@Slf4j
@Service
public class KafkaTelemetryProducer {
	private final KafkaProducer<String, SensorEventAvro> sensorProducer;
	private final KafkaProducer<String, HubEventAvro> hubProducer;

	private static final String SENSOR_TOPIC_V1 = "telemetry.sensors.v1";
	private static final String HUB_TOPIC_V1 = "telemetry.hubs.v1";

	public KafkaTelemetryProducer() {
		Properties sensorProps = new Properties();
		sensorProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		sensorProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		sensorProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SensorEventAvroSerializer.class.getName());

		Properties hubProps = new Properties();
		hubProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		hubProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		hubProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, HubEventAvroSerializer.class.getName());

		this.sensorProducer = new KafkaProducer<>(sensorProps);
		this.hubProducer = new KafkaProducer<>(hubProps);
	}

	public void sendSensor(SensorEventAvro e) {
		ProducerRecord<String, SensorEventAvro> rec = new ProducerRecord<>(SENSOR_TOPIC_V1, e);
		log.info("KafkaTelemetryProducer: record - {}", rec);

		sensorProducer.send(rec);
	}

	public void sendHub(HubEventAvro h) {
		ProducerRecord<String, HubEventAvro> rec = new ProducerRecord<>(HUB_TOPIC_V1, h);
		log.info("KafkaTelemetryProducer: record - {}", rec);

		hubProducer.send(rec);
	}

	@PreDestroy
	public void closeProducers() {
		sensorProducer.close();
		hubProducer.close();
	}
}
