package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventHandler {

	private final SensorService sensorService;
	private final ScenarioService scenarioService;

	public void handle(ConsumerRecord<String, HubEventAvro> record) {
		HubEventAvro event = record.value();
		if (event == null) {
			log.warn("Получено null-событие");
			return;
		}

		String hubId = event.getHubId();
		Object payload = event.getPayload();

		if (payload instanceof DeviceAddedEventAvro deviceAdded) {
			sensorService.addDevice(hubId, deviceAdded.getId());
		} else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
			sensorService.removeDevice(hubId, deviceRemoved.getId());
		} else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
			scenarioService.addScenario(hubId, scenarioAdded);
		} else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
			scenarioService.removeScenario(hubId, scenarioRemoved.getName());
		} else {
			log.warn("Неизвестный тип полезной нагрузки события: {}",
					payload != null ? payload.getClass() : "null");
		}
	}
}
