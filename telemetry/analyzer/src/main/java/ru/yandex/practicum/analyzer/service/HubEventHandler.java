package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.entity.enums.ConditionType;
import ru.yandex.practicum.analyzer.entity.enums.Operation;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioActionId;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioConditionId;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventHandler {

	private final ScenarioRepository scenarioRepository;
	private final SensorRepository sensorRepository;
	private final ConditionRepository conditionRepository;
	private final ActionRepository actionRepository;
	private final ScenarioConditionRepository scenarioConditionRepository;
	private final ScenarioActionRepository scenarioActionRepository;

	public void handle(ConsumerRecord<String, HubEventAvro> record) {
		HubEventAvro event = record.value();
		if (event == null) {
			log.warn("Получено null-событие");
			return;
		}

		String hubId = event.getHubId();

		Object payload = event.getPayload();
		if (payload instanceof DeviceAddedEventAvro deviceAdded) {
			handleDeviceAdded(hubId, deviceAdded);
		} else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
			handleDeviceRemoved(hubId, deviceRemoved);
		} else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
			handleScenarioAdded(hubId, scenarioAdded);
		} else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
			handleScenarioRemoved(hubId, scenarioRemoved);
		} else {
			log.warn("Неизвестный тип полезной нагрузки события: {}", payload != null ? payload.getClass() : "null");
		}
	}

	private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
		String sensorId = event.getId();

		if (sensorRepository.findByIdAndHubId(sensorId, hubId).isPresent()) {
			log.info("Устройство уже существует: sensorId={}, hubId={}", sensorId, hubId);
			return;
		}

		Sensor sensor = new Sensor();
		sensor.setId(sensorId);
		sensor.setHubId(hubId);
		sensorRepository.save(sensor);
		log.info("Устройство добавлено: sensorId={}, hubId={}", sensorId, hubId);
	}

	private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
		String sensorId = event.getId();

		sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(sensor -> {
			sensorRepository.delete(sensor);
			log.info("Устройство удалено: sensorId={}, hubId={}", sensorId, hubId);
		});
	}

	private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro eventAvro) {
		String scenarioName = eventAvro.getName();

		scenarioRepository.findByHubIdAndName(hubId, scenarioName)
				.ifPresent(this::deleteScenarioInternal);

		Scenario scenario = new Scenario();
		scenario.setHubId(hubId);
		scenario.setName(scenarioName);
		scenario = scenarioRepository.save(scenario);

		for (ScenarioConditionAvro condAvro : eventAvro.getConditions()) {
			Condition condition = new Condition();
			condition.setType(ConditionType.valueOf(condAvro.getType().name()));
			condition.setOperation(mapOperation(condAvro.getOperation()));
			condition.setValue(extractIntValue(condAvro));
			condition = conditionRepository.save(condition);

			Sensor sensor = sensorRepository.findByIdAndHubId(condAvro.getSensorId(), hubId)
					.orElseThrow(() -> new IllegalArgumentException(
							"Сенсор не найден: " + condAvro.getSensorId()));

			ScenarioConditionId scId = new ScenarioConditionId(
					scenario.getId(),
					sensor.getId(),
					condition.getId());
			ScenarioCondition scenarioCondition = new ScenarioCondition(scId, scenario, sensor, condition);
			scenarioConditionRepository.save(scenarioCondition);
		}

		for (DeviceActionAvro actAvro : eventAvro.getActions()) {
			ru.yandex.practicum.analyzer.entity.enums.ActionType actionType = mapActionType(actAvro.getType());

			Action action = new Action();
			action.setType(actionType);
			action.setValue(actAvro.getValue() != null ? actAvro.getValue() : 0);
			action = actionRepository.save(action);

			Sensor sensor = sensorRepository.findByIdAndHubId(actAvro.getSensorId(), hubId)
					.orElseThrow(() -> new IllegalArgumentException(
							"Сенсор не найден: " + actAvro.getSensorId()));

			ScenarioActionId saId = new ScenarioActionId(
					scenario.getId(),
					sensor.getId(),
					action.getId());
			ScenarioAction scenarioAction = new ScenarioAction(saId, scenario, sensor, action);
			scenarioActionRepository.save(scenarioAction);
		}

		log.info("Сценарий добавлен: hubId={}, name={}", hubId, scenarioName);
	}

	private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
		String scenarioName = event.getName();

		scenarioRepository.findByHubIdAndName(hubId, scenarioName)
				.ifPresent(this::deleteScenarioInternal);
	}

	private void deleteScenarioInternal(Scenario scenario) {
		scenarioRepository.delete(scenario);
	}

	private Operation mapOperation(ConditionOperationAvro avroOp) {
		return switch (avroOp) {
			case EQUALS -> Operation.EQUAL;
			case GREATER_THAN -> Operation.MORE;
			case LOWER_THAN -> Operation.LESS;
			default -> throw new IllegalArgumentException("Неизвестная операция: " + avroOp);
		};
	}

	private ru.yandex.practicum.analyzer.entity.enums.ActionType mapActionType(ActionTypeAvro avroType) {
		return switch (avroType) {
			case ACTIVATE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.HEATER;
			case DEACTIVATE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.FAN;
			case INVERSE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.LIGHT;
			case SET_VALUE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.CONDITIONER;
			default -> throw new IllegalArgumentException("Неизвестный тип действия: " + avroType);
		};
	}

	private Integer extractIntValue(ScenarioConditionAvro condAvro) {
		Object value = condAvro.getValue();
		if (value instanceof Boolean bool) {
			return bool ? 1 : 0;
		}
		if (value instanceof Integer integer) {
			return integer;
		}
		return 0;
	}
}
