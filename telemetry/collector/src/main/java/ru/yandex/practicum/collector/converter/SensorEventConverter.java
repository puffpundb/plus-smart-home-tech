package ru.yandex.practicum.collector.converter;

import ru.yandex.practicum.collector.dto.sensorDto.*;
import ru.yandex.practicum.kafka.telemetry.event.*;


public class SensorEventConverter {
	public static SensorEventAvro convertToAvro(SensorEventDto dto) {
		SensorEventAvro avro = new SensorEventAvro();
		avro.setId(dto.getId());
		avro.setHubId(dto.getHubId());
		avro.setTimestamp(dto.getTimestamp().toEpochMilli());

		if (dto instanceof ClimateSensorEventDto climateSensorEventDto) {
			ClimateSensorAvro payload = new ClimateSensorAvro();
			payload.setTemperatureC(climateSensorEventDto.getTemperatureC());
			payload.setHumidity(climateSensorEventDto.getHumidity());
			payload.setCo2Level(climateSensorEventDto.getCo2Level());

			avro.setPayload(payload);
		} else if (dto instanceof LightSensorEventDto lightSensorEventDto) {
			LightSensorAvro payload = new LightSensorAvro();
			payload.setLinkQuality(lightSensorEventDto.getLinkQuality());
			payload.setLuminosity(lightSensorEventDto.getLuminosity());

			avro.setPayload(payload);
		} else if (dto instanceof MotionSensorEventDto motionSensorEventDto) {
			MotionSensorAvro payload = new MotionSensorAvro();
			payload.setLinkQuality(motionSensorEventDto.getLinkQuality());
			payload.setMotion(motionSensorEventDto.getMotion());
			payload.setVoltage(motionSensorEventDto.getVoltage());

			avro.setPayload(payload);
		} else if (dto instanceof SwitchSensorEventDto switchSensorEventDto) {
			SwitchSensorAvro payload = new SwitchSensorAvro();
			payload.setState(switchSensorEventDto.getState());

			avro.setPayload(payload);
		} else if (dto instanceof TemperatureSensorEventDto temperatureSensorEventDto) {
			TemperatureSensorAvro payload = new TemperatureSensorAvro();
			payload.setTemperatureC(temperatureSensorEventDto.getTemperatureC());
			payload.setTemperatureF(temperatureSensorEventDto.getTemperatureF());

			avro.setPayload(payload);
		}

		return avro;
	}
}
