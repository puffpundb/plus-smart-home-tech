package ru.yandex.practicum.collector.dto.hubDto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "type"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = DeviceAddedEventDto.class, name = "DEVICE_ADDED"),
		@JsonSubTypes.Type(value = DeviceRemovedEventDto.class, name = "DEVICE_REMOVED"),
		@JsonSubTypes.Type(value = ScenarioAddedEventDto.class, name = "SCENARIO_ADDED"),
		@JsonSubTypes.Type(value = ScenarioRemovedEventDto.class, name = "SCENARIO_REMOVED")
})
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class HubEventDto {
	@NotBlank
	String hubId;

	@NotNull
	Instant timestamp;

	@NotNull
	public abstract HubEventType getType();
}
