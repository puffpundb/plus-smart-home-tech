package ru.yandex.practicum.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import serializer.CommonSerializer;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

	private final String bootstrapServers;

	public KafkaProducerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}

	@Bean
	public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CommonSerializer.class);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		return new KafkaProducer<>(props);
	}
}
