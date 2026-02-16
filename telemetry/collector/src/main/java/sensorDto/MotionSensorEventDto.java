package sensorDto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Data
public class MotionSensorEventDto extends SensorEventDto {
	@NotNull
	Integer linkQuality;

	@NotNull
	Boolean motion;

	@NotNull
	Integer voltage;

	@Override
	public SensorEventType getType() {
		return SensorEventType.MOTION_SENSOR_EVENT;
	}
}
