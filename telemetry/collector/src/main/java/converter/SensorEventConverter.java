package converter;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import sensorDto.*;

public class SensorEventConverter {
	public SensorEventAvro convert(SensorEventDto dto) {
		SensorEventAvro avro = new SensorEventAvro();
		avro.setId(dto.getId());
		avro.setHubId(dto.getHubId());
		avro.setTimestamp(dto.getTimestamp().toEpochMilli());

		if (dto instanceof ClimateSensorEventDto climateSensorEventDto) {
			ClimateSensorEventDto payload = new ClimateSensorEventDto();
			payload.setTemperatureC(climateSensorEventDto.getTemperatureC());
			payload.setHumidity(climateSensorEventDto.getHumidity());
			payload.setCo2Level(climateSensorEventDto.getCo2Level());

			avro.setPayload(payload);
		} else if (dto instanceof LightSensorEventDto lighSensorEventDto) {
			LightSensorEventDto payload = new LightSensorEventDto();
			payload.setLinkQuality(lighSensorEventDto.getLinkQuality());
			payload.setLuminosity(lighSensorEventDto.getLuminosity());

			avro.setPayload(payload);
		} else if (dto instanceof MotionSensorEventDto motionSensorEventDto) {
			MotionSensorEventDto payload = new MotionSensorEventDto();
			payload.setLinkQuality(motionSensorEventDto.getLinkQuality());
			payload.setMotion(motionSensorEventDto.getMotion());
			payload.setVoltage(motionSensorEventDto.getVoltage());

			avro.setPayload(payload);
		} else if (dto instanceof SwitchSensorEventDto switchSensorEventDto) {
			SwitchSensorEventDto payload = new SwitchSensorEventDto();
			payload.setState(switchSensorEventDto.getState());

			avro.setPayload(payload);
		} else if (dto instanceof TemperatureSensorEventDto temperatureSensorEventDto) {
			TemperatureSensorEventDto payload = new TemperatureSensorEventDto();
			payload.setTemperatureC(temperatureSensorEventDto.getTemperatureC());
			payload.setTemperatureF(temperatureSensorEventDto.getTemperatureF());

			avro.setPayload(payload);
		}

		return avro;
	}
}
