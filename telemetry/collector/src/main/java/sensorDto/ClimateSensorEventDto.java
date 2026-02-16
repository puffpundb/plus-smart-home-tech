package sensorDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEventDto extends SensorEventDto {
	@NotNull
	Integer temperatureC;

	@NotNull
	Integer humidity;

	@NotNull
	Integer co2Level;

	@Override
	public SensorEventType getType() {
		return SensorEventType.CLIMATE_SENSOR_EVENT;
	}
}
