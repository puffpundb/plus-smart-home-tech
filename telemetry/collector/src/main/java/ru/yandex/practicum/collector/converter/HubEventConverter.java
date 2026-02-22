package ru.yandex.practicum.collector.converter;

import ru.yandex.practicum.collector.dto.hubDto.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;


public class HubEventConverter {
	public static HubEventAvro convertToAvro(HubEventDto dto) {
		HubEventAvro avro = new HubEventAvro();
		avro.setHubId(dto.getHubId());
		avro.setTimestamp(dto.getTimestamp().toEpochMilli());

		if (dto instanceof DeviceAddedEventDto deviceAddedEventDto) {
			DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
			payload.setId(deviceAddedEventDto.getId());
			payload.setDeviceType(DeviceTypeAvro.valueOf(deviceAddedEventDto.getDeviceType()));

			avro.setPayload(payload);
		} else if (dto instanceof DeviceRemovedEventDto deviceRemovedEventDto) {
			DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
			payload.setId(deviceRemovedEventDto.getId());

			avro.setPayload(payload);
		} else if (dto instanceof ScenarioAddedEventDto scenarioAddedEventDto) {
			ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
			payload.setName(scenarioAddedEventDto.getName());

			List<ScenarioConditionAvro> conditions = new ArrayList<>();
			for (ScenarioConditionDto conditionDto : scenarioAddedEventDto.getConditions()) {
				ScenarioConditionAvro condition = new ScenarioConditionAvro();
				condition.setSensorId(conditionDto.getSensorId());
				condition.setType(ConditionTypeAvro.valueOf(conditionDto.getType()));
				condition.setOperation(ConditionOperationAvro.valueOf(conditionDto.getOperation()));

				if (conditionDto.getValue() instanceof Integer) {
					condition.setValue(conditionDto.getValue());
				} else if (conditionDto.getValue() instanceof Boolean) {
					condition.setValue(conditionDto.getValue());
				} else {
					condition.setValue(null);
				}

				conditions.add(condition);
			}
			payload.setConditions(conditions);

			List<DeviceActionAvro> actions = new ArrayList<>();
			for (DeviceActionDto actionDto : scenarioAddedEventDto.getActions()) {
				DeviceActionAvro action = new DeviceActionAvro();
				action.setSensorId(actionDto.getSensorId());
				action.setType(ActionTypeAvro.valueOf(actionDto.getType()));

				if (actionDto.getValue() != null) {
					action.setValue(actionDto.getValue());
				} else {
					action.setValue(null);
				}

				actions.add(action);
			}
			payload.setActions(actions);

			avro.setPayload(payload);
		} else if (dto instanceof ScenarioRemovedEventDto scenarioRemoved) {
			ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
			payload.setName(scenarioRemoved.getName());

			avro.setPayload(payload);
		}

		return avro;
	}
}
