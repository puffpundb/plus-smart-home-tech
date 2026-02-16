package sensorDto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemperatureSensorEventDto extends SensorEventDto {
	@NotNull
	Integer temperatureC;

	@NotNull
	Integer temperatureF;

	@Override
	public SensorEventType getType() {
		return SensorEventType.TEMPERATURE_SENSOR_EVENT;
	}
}
