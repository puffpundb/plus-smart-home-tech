package ru.yandex.practicum.collector.dto.hubDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioAddedEventDto extends HubEventDto {
	@NotBlank
	String name;

	@NotNull
	List<ScenarioConditionDto> conditions;

	@NotNull
	List<DeviceActionDto> actions;

	@Override
	public HubEventType getType() {
		return HubEventType.SCENARIO_ADDED;
	}
}
