package sensorDto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
public class LightSensorEventDto extends SensorEventDto {
	@NotNull
	Integer linkQuality;

	@NotNull
	Integer luminosity;

	@Override
	public SensorEventType getType() {
		return SensorEventType.LIGHT_SENSOR_EVENT;
	}
}
