package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.converter.HubEventConverter;
import ru.yandex.practicum.collector.converter.SensorEventConverter;
import ru.yandex.practicum.collector.dto.hubDto.HubEventDto;
import ru.yandex.practicum.collector.dto.sensorDto.SensorEventDto;

@RequiredArgsConstructor
@Service
public class CollectorService {
	private final KafkaTelemetryProducer producer;

	public void sendSensor(SensorEventDto dto) {
		producer.sendSensor(SensorEventConverter.convertToAvro(dto));
	}

	public void sendHub(HubEventDto dto) {
		producer.sendHub(HubEventConverter.convertToAvro(dto));
	}
}
