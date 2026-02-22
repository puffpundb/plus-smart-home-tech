package ru.yandex.practicum.collector.dto.hubDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceRemovedEventDto extends HubEventDto {
	@NotBlank
	String id;

	@Override
	public HubEventType getType() {
		return HubEventType.DEVICE_REMOVED;
	}
}
