package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

@GrpcService
@Slf4j
public class GrpcCollectorController extends CollectorControllerGrpc.CollectorControllerImplBase {
	@Override
	public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
		try {
			log.info("Получено событие хаба: {}", request);

			switch (request.getPayloadCase()) {
				case DEVICE_ADDED -> {
					DeviceAddedEventProto event = request.getDeviceAdded();
					log.info("Устройство добавлено: id={}, type={}", event.getId(), event.getType());
				}
				case DEVICE_REMOVED -> {
					DeviceRemovedEventProto event = request.getDeviceRemoved();
					log.info("Устройство удалено: id={}", event.getId());
				}
				case SCENARIO_ADDED -> {
					ScenarioAddedEventProto event = request.getScenarioAdded();
					log.info("Сценарий добавлен: name={}", event.getName());
				}
				case SCENARIO_REMOVED -> {
					ScenarioRemovedEventProto event = request.getScenarioRemoved();
					log.info("Сценарий удалён: name={}", event.getName());
				}
				default -> log.warn("Неизвестный тип события хаба: {}", request.getPayloadCase());
			}

			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		} catch (Exception e) {
			generateError(e, responseObserver);
		}
	}

	@Override
	public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
		try {
			log.info("Получено событие датчика: {}", request);

			switch (request.getPayloadCase()) {
				case MOTION_SENSOR -> {
					MotionSensorProto sensor = request.getMotionSensor();
					log.info("Датчик движения: motion={}, voltage={}",
							sensor.getMotion(), sensor.getVoltage());
				}
				case TEMPERATURE_SENSOR -> {
					TemperatureSensorProto sensor = request.getTemperatureSensor();
					log.info("Датчик температуры: C={}, F={}",
							sensor.getTemperatureC(), sensor.getTemperatureF());
				}
				case LIGHT_SENSOR -> {
					LightSensorProto sensor = request.getLightSensor();
					log.info("Датчик освещения: luminosity={}", sensor.getLuminosity());
				}
				case CLIMATE_SENSOR -> {
					ClimateSensorProto sensor = request.getClimateSensor();
					log.info("Климатический датчик: temp={}, humidity={}, co2={}",
							sensor.getTemperatureC(), sensor.getHumidity(), sensor.getCo2Level());
				}
				case SWITCH_SENSOR -> {
					SwitchSensorProto sensor = request.getSwitchSensor();
					log.info("Переключатель: state={}", sensor.getState());
				}
				default -> log.warn("Неизвестный тип события: {}", request.getPayloadCase());
			}

			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		} catch (Exception e) {
			generateError(e, responseObserver);
		}
	}

	private void generateError(Exception e, StreamObserver<Empty> responseObserver) {
		responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
				.withDescription(e.getLocalizedMessage())
				.withCause(e)));
	}
}
