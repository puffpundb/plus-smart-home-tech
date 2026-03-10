package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcCollectorController extends CollectorControllerGrpc.CollectorControllerImplBase {

	private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;

	@Override
	public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
		try {
			log.info("Получено событие датчика: sensorId={}, hubId={}", request.getId(), request.getHubId());

			SensorEventAvro avro = new SensorEventAvro();
			avro.setId(request.getId());
			avro.setHubId(request.getHubId());
			avro.setTimestamp(request.getTimestamp().getSeconds() * 1000L + request.getTimestamp().getNanos() / 1_000_000);

			switch (request.getPayloadCase()) {
				case MOTION_SENSOR:
					MotionSensorAvro motionAvro = new MotionSensorAvro();
					motionAvro.setLinkQuality(request.getMotionSensor().getLinkQuality());
					motionAvro.setMotion(request.getMotionSensor().getMotion());
					motionAvro.setVoltage(request.getMotionSensor().getVoltage());
					avro.setPayload(motionAvro);
					break;
				case TEMPERATURE_SENSOR:
					TemperatureSensorAvro tempAvro = new TemperatureSensorAvro();
					tempAvro.setTemperatureC(request.getTemperatureSensor().getTemperatureC());
					tempAvro.setTemperatureF(request.getTemperatureSensor().getTemperatureF());
					avro.setPayload(tempAvro);
					break;
				case LIGHT_SENSOR:
					LightSensorAvro lightAvro = new LightSensorAvro();
					lightAvro.setLinkQuality(request.getLightSensor().getLinkQuality());
					lightAvro.setLuminosity(request.getLightSensor().getLuminosity());
					avro.setPayload(lightAvro);
					break;
				case CLIMATE_SENSOR:
					ClimateSensorAvro climateAvro = new ClimateSensorAvro();
					climateAvro.setTemperatureC(request.getClimateSensor().getTemperatureC());
					climateAvro.setHumidity(request.getClimateSensor().getHumidity());
					climateAvro.setCo2Level(request.getClimateSensor().getCo2Level());
					avro.setPayload(climateAvro);
					break;
				case SWITCH_SENSOR:
					SwitchSensorAvro switchAvro = new SwitchSensorAvro();
					switchAvro.setState(request.getSwitchSensor().getState());
					avro.setPayload(switchAvro);
					break;
				case PAYLOAD_NOT_SET:
				default:
					avro.setPayload(null);
					break;
			}

			ProducerRecord<String, SpecificRecordBase> record =
					new ProducerRecord<>("telemetry.sensors.v1", request.getId(), avro);
			kafkaProducer.send(record);

			log.info("Событие датчика отправлено в Kafka: sensorId={}", request.getId());

			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.error("Ошибка при обработке события датчика: sensorId={}", request.getId(), e);
			responseObserver.onError(new StatusRuntimeException(
					Status.INTERNAL
							.withDescription(e.getLocalizedMessage())
							.withCause(e)));
		}
	}

	@Override
	public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
		try {
			log.info("Получено событие хаба: hubId={}, type={}", request.getHubId(), request.getPayloadCase());

			HubEventAvro avro = new HubEventAvro();
			avro.setHubId(request.getHubId());
			avro.setTimestamp(request.getTimestamp().getSeconds() * 1000L + request.getTimestamp().getNanos() / 1_000_000);

			switch (request.getPayloadCase()) {
				case DEVICE_ADDED:
					DeviceAddedEventAvro deviceAddedAvro = new DeviceAddedEventAvro();
					deviceAddedAvro.setId(request.getDeviceAdded().getId());
					deviceAddedAvro.setDeviceType(DeviceTypeAvro.valueOf(request.getDeviceAdded().getType().name()));
					avro.setPayload(deviceAddedAvro);
					break;
				case DEVICE_REMOVED:
					DeviceRemovedEventAvro deviceRemovedAvro = new DeviceRemovedEventAvro();
					deviceRemovedAvro.setId(request.getDeviceRemoved().getId());
					avro.setPayload(deviceRemovedAvro);
					break;
				case SCENARIO_ADDED:
					ScenarioAddedEventAvro scenarioAddedAvro = new ScenarioAddedEventAvro();
					scenarioAddedAvro.setName(request.getScenarioAdded().getName());
					scenarioAddedAvro.setConditions(new ArrayList<>());
					scenarioAddedAvro.setActions(new ArrayList<>());

					for (ScenarioConditionProto conditionProto : request.getScenarioAdded().getConditionList()) {
						ScenarioConditionAvro conditionAvro = new ScenarioConditionAvro();
						conditionAvro.setSensorId(conditionProto.getSensorId());
						conditionAvro.setType(ConditionTypeAvro.valueOf(conditionProto.getType().name()));
						conditionAvro.setOperation(ConditionOperationAvro.valueOf(conditionProto.getOperation().name()));

						switch (conditionProto.getValueCase()) {
							case BOOL_VALUE:
								conditionAvro.setValue(conditionProto.getBoolValue());
								break;
							case INT_VALUE:
								conditionAvro.setValue(conditionProto.getIntValue());
								break;
							case VALUE_NOT_SET:
							default:
								conditionAvro.setValue(null);
								break;
						}

						scenarioAddedAvro.getConditions().add(conditionAvro);
					}

					for (DeviceActionProto actionProto : request.getScenarioAdded().getActionList()) {
						DeviceActionAvro actionAvro = new DeviceActionAvro();
						actionAvro.setSensorId(actionProto.getSensorId());
						actionAvro.setType(ActionTypeAvro.valueOf(actionProto.getType().name()));

						if (actionProto.hasValue()) {
							actionAvro.setValue(actionProto.getValue());
						} else {
							actionAvro.setValue(null);
						}

						scenarioAddedAvro.getActions().add(actionAvro);
					}

					avro.setPayload(scenarioAddedAvro);
					break;
				case SCENARIO_REMOVED:
					ScenarioRemovedEventAvro scenarioRemovedAvro = new ScenarioRemovedEventAvro();
					scenarioRemovedAvro.setName(request.getScenarioRemoved().getName());
					avro.setPayload(scenarioRemovedAvro);
					break;
				case PAYLOAD_NOT_SET:
				default:
					avro.setPayload(null);
					break;
			}

			ProducerRecord<String, SpecificRecordBase> record =
					new ProducerRecord<>("telemetry.hubs.v1", request.getHubId(), avro);
			kafkaProducer.send(record);

			log.info("Событие хаба отправлено в Kafka: hubId={}", request.getHubId());

			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.error("Ошибка при обработке события хаба: hubId={}", request.getHubId(), e);
			responseObserver.onError(new StatusRuntimeException(
					Status.INTERNAL
							.withDescription(e.getLocalizedMessage())
							.withCause(e)));
		}
	}
}
