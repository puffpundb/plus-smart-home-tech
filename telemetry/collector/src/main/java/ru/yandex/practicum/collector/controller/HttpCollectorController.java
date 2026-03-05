package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.dto.hubDto.HubEventDto;
import ru.yandex.practicum.collector.dto.sensorDto.SensorEventDto;
import ru.yandex.practicum.collector.service.CollectorService;

@RequiredArgsConstructor
@RestController
@Slf4j
public class HttpCollectorController {
	private final CollectorService service;

	@PostMapping("/events/sensors")
	public ResponseEntity<Void> collectSensor(@Valid @RequestBody SensorEventDto sensorEventDto) {
		log.info("CollectorController: POST /events/sensors - {}", sensorEventDto);
		service.sendSensor(sensorEventDto);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/events/hubs")
	public ResponseEntity<Void> collectHub(@Valid @RequestBody HubEventDto hubEventDto) {
		log.info("CollectorController: POST /events/hubs - {}", hubEventDto);
		service.sendHub(hubEventDto);

		return ResponseEntity.ok().build();
	}
}
