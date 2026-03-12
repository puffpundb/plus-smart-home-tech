package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.entity.enums.ConditionType;
import ru.yandex.practicum.analyzer.entity.enums.Operation;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioActionId;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioConditionId;
import ru.yandex.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

	private final ScenarioRepository scenarioRepository;
	private final ConditionRepository conditionRepository;
	private final ActionRepository actionRepository;
	private final ScenarioConditionRepository scenarioConditionRepository;
	private final ScenarioActionRepository scenarioActionRepository;
	private final SensorRepository sensorRepository;

	@Transactional
	public void addScenario(String hubId, ScenarioAddedEventAvro eventAvro) {
		String scenarioName = eventAvro.getName();

		scenarioRepository.findByHubIdAndName(hubId, scenarioName)
				.ifPresent(this::deleteScenarioInternal);

		Scenario scenario = new Scenario();
		scenario.setHubId(hubId);
		scenario.setName(scenarioName);
		scenario = scenarioRepository.save(scenario);

		for (ScenarioConditionAvro condAvro : eventAvro.getConditions()) {
			processCondition(scenario, hubId, condAvro);
		}

		for (DeviceActionAvro actAvro : eventAvro.getActions()) {
			processAction(scenario, hubId, actAvro);
		}

		log.info("Сценарий добавлен: hubId={}, name={}", hubId, scenarioName);
	}

	private void processCondition(Scenario scenario, String hubId, ScenarioConditionAvro condAvro) {
		Condition condition = new Condition();
		condition.setType(ConditionType.valueOf(condAvro.getType().name()));
		condition.setOperation(mapOperation(condAvro.getOperation()));
		condition.setValue(extractIntValue(condAvro));
		condition = conditionRepository.save(condition);

		Sensor sensor = findSensorOrThrow(condAvro.getSensorId(), hubId);

		ScenarioConditionId scId = new ScenarioConditionId(
				scenario.getId(),
				sensor.getId(),
				condition.getId());

		ScenarioCondition scenarioCondition = new ScenarioCondition(scId, scenario, sensor, condition);
		scenarioConditionRepository.save(scenarioCondition);
	}

	private void processAction(Scenario scenario, String hubId, DeviceActionAvro actAvro) {
		ru.yandex.practicum.analyzer.entity.enums.ActionType actionType = mapActionType(actAvro.getType());

		Action action = new Action();
		action.setType(actionType);
		action.setValue(actAvro.getValue() != null ? actAvro.getValue() : 0);
		action = actionRepository.save(action);

		Sensor sensor = findSensorOrThrow(actAvro.getSensorId(), hubId);

		ScenarioActionId saId = new ScenarioActionId(
				scenario.getId(),
				sensor.getId(),
				action.getId());

		ScenarioAction scenarioAction = new ScenarioAction(saId, scenario, sensor, action);
		scenarioActionRepository.save(scenarioAction);
	}

	private Sensor findSensorOrThrow(String sensorId, String hubId) {
		return sensorRepository.findByIdAndHubId(sensorId, hubId)
				.orElseThrow(() -> new IllegalArgumentException("Сенсор не найден: " + sensorId));
	}

	@Transactional
	public void removeScenario(String hubId, String scenarioName) {
		scenarioRepository.findByHubIdAndName(hubId, scenarioName)
				.ifPresent(this::deleteScenarioInternal);
	}

	@Transactional
	void deleteScenarioInternal(Scenario scenario) {
		Long scenarioId = scenario.getId();

		scenarioConditionRepository.deleteByScenarioId(scenarioId);
		scenarioActionRepository.deleteByScenarioId(scenarioId);
		scenarioRepository.delete(scenario);

		log.info("Сценарий и все его связи удалены: id={}", scenarioId);
	}

	private Operation mapOperation(ConditionOperationAvro avroOp) {
		if (avroOp == null) {
			throw new IllegalArgumentException("OperationAvro не может быть null");
		}
		return switch (avroOp) {
			case EQUALS -> Operation.EQUAL;
			case GREATER_THAN -> Operation.MORE;
			case LOWER_THAN -> Operation.LESS;
		};
	}

	private ru.yandex.practicum.analyzer.entity.enums.ActionType mapActionType(ActionTypeAvro avroType) {
		if (avroType == null) {
			throw new IllegalArgumentException("ActionTypeAvro не может быть null");
		}
		return switch (avroType) {
			case ACTIVATE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.HEATER;
			case DEACTIVATE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.FAN;
			case INVERSE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.LIGHT;
			case SET_VALUE -> ru.yandex.practicum.analyzer.entity.enums.ActionType.CONDITIONER;
		};
	}

	private Integer extractIntValue(ScenarioConditionAvro condAvro) {
		if (condAvro == null || condAvro.getValue() == null) {
			return 0;
		}
		Object value = condAvro.getValue();
		if (value instanceof Boolean bool) {
			return bool ? 1 : 0;
		}
		if (value instanceof Integer integer) {
			return integer;
		}
		if (value instanceof String str) {
			try {
				return Integer.parseInt(str.trim());
			} catch (NumberFormatException e) {
				log.warn("Не удалось распарсить значение условия в int: {}", str);
				return 0;
			}
		}
		log.warn("Неподдерживаемый тип значения условия: {}", value.getClass());
		return 0;
	}
}
