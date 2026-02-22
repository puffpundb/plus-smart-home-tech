package ru.yandex.practicum.collector.dto.sensorDto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SwitchSensorEventDto extends SensorEventDto {
	@NotNull
	Boolean state;

	@Override
	public SensorEventType getType() {
		return SensorEventType.SWITCH_SENSOR_EVENT;
	}
}
