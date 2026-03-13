package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.*;
import ru.yandex.practicum.analyzer.entity.enums.ConditionType;
import ru.yandex.practicum.analyzer.entity.enums.Operation;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioEvaluator {

	private final ScenarioRepository scenarioRepository;
	private final ActionExecutor actionExecutor;

	@Transactional
	public void evaluate(SensorsSnapshotAvro snapshot) {
		String hubId = snapshot.getHubId();
		log.info("Проверка сценариев для хаба: {}", hubId);

		List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
		if (scenarios.isEmpty()) {
			log.debug("Нет сценариев для хаба: {}", hubId);
			return;
		}

		Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

		for (Scenario scenario : scenarios) {
			evaluateScenario(scenario, sensorsState);
		}
	}


	private void evaluateScenario(Scenario scenario, Map<String, SensorStateAvro> sensorsState) {
		List<ScenarioCondition> conditions = scenario.getScenarioConditions();
		List<ScenarioAction> actions = scenario.getScenarioActions();

		log.debug("Проверка сценария '{}' ({} условий, {} действий)",
				scenario.getName(), conditions.size(), actions.size());

		boolean allConditionsMet = conditions.stream()
				.allMatch(sc -> checkCondition(sensorsState, sc));

		log.info("Сценарий '{}': условия выполнены = {}", scenario.getName(), allConditionsMet);

		if (allConditionsMet) {
			log.info("Сценарий активирован: scenarioId={}, name={}", scenario.getId(), scenario.getName());
			for (ScenarioAction scenarioAction : actions) {
				Action action = scenarioAction.getAction();
				String sensorId = scenarioAction.getSensor().getId();

				log.info("Отправка команды: sensorId={}, action={}, value={}",
						sensorId, action.getType(), action.getValue());

				actionExecutor.executeAction(
						scenario.getHubId(),
						scenario.getName(),
						sensorId,
						action.getType(),
						action.getValue()
				);
			}
		}
	}

	private boolean checkCondition(Map<String, SensorStateAvro> sensorsState, ScenarioCondition sc) {
		String sensorId = sc.getSensor().getId();
		SensorStateAvro state = sensorsState.get(sensorId);

		if (state == null) {
			log.debug("Условие: нет состояния для датчика {}", sensorId);
			return false;
		}

		Object rawData = state.getData();
		if (rawData == null) {
			log.debug("Условие: данные датчика {} пустые", sensorId);
			return false;
		}

		Integer currentValue = extractValue(rawData, sc.getCondition().getType());
		if (currentValue == null) {
			log.debug("Условие: не удалось извлечь числовое значение из {}", rawData);
			return false;
		}

		Condition condition = sc.getCondition();
		Integer thresholdValue = condition.getValue();

		if (thresholdValue == null) {
			log.debug("Условие: пороговое значение равно null");
			return false;
		}

		Operation operation = condition.getOperation();
		boolean result = switch (operation) {
			case MORE -> currentValue > thresholdValue;
			case LESS -> currentValue < thresholdValue;
			case EQUAL -> currentValue.equals(thresholdValue);
		};

		log.debug("Условие: sensorId={}, operation={}, expected={}, actual={}, result={}",
				sensorId, operation, thresholdValue, currentValue, result);

		return result;
	}

	private Integer extractValue(Object data, ConditionType conditionType) {
		if (data == null) {
			return null;
		}

		return switch (data) {
			case ClimateSensorAvro climate -> extractClimateValue(climate, conditionType);
			case MotionSensorAvro motion -> extractMotionValue(motion, conditionType);
			case LightSensorAvro light -> extractLightValue(light, conditionType);
			case SwitchSensorAvro switchSensor -> extractSwitchValue(switchSensor, conditionType);
			case Integer value -> value;
			case Boolean flag -> flag ? 1 : 0;
			default -> {
				log.warn("Неизвестный тип данных для извлечения значения: {}", data.getClass());
				yield null;
			}
		};
	}

	private Integer extractClimateValue(ClimateSensorAvro sensor, ConditionType type) {
		return switch (type) {
			case TEMPERATURE -> sensor.getTemperatureC();
			case HUMIDITY -> sensor.getHumidity();
			case CO2LEVEL -> sensor.getCo2Level();
			default -> null;
		};
	}

	private Integer extractMotionValue(MotionSensorAvro sensor, ConditionType type) {
		return (type == ConditionType.MOTION) ? (sensor.getMotion() ? 1 : 0) : null;
	}

	private Integer extractLightValue(LightSensorAvro sensor, ConditionType type) {
		return (type == ConditionType.LUMINOSITY) ? sensor.getLuminosity() : null;
	}

	private Integer extractSwitchValue(SwitchSensorAvro sensor, ConditionType type) {
		return (type == ConditionType.SWITCH) ? (sensor.getState() ? 1 : 0) : null;
	}
}
