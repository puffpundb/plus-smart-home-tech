package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.entity.enums.ActionType;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

import java.time.Instant;

@Slf4j
@Service
public class ActionExecutor {

	private final HubRouterControllerBlockingStub hubRouterClient;

	public ActionExecutor(@GrpcClient("hub-router")
						  HubRouterControllerBlockingStub hubRouterClient) {
		this.hubRouterClient = hubRouterClient;
	}

	public void executeAction(String hubId, String scenarioName,
							  String sensorId, ActionType type, Integer value) {
		DeviceActionProto actionProto = DeviceActionProto.newBuilder()
				.setSensorId(sensorId)
				.setType(mapToProtoActionType(type))
				.setValue(value)
				.build();

		Instant now = Instant.now();
		DeviceActionRequest request = DeviceActionRequest.newBuilder()
				.setHubId(hubId)
				.setScenarioName(scenarioName)
				.setAction(actionProto)
				.setTimestamp(Timestamp.newBuilder()
						.setSeconds(now.getEpochSecond())
						.setNanos(now.getNano())
						.build())
				.build();

		try {
			Empty response = hubRouterClient.handleDeviceAction(request);
			log.info("Действие выполнено: hubId={}, scenario={}, sensor={}, type={}, value={}",
					hubId, scenarioName, sensorId, type, value);
		} catch (Exception e) {
			log.error("Ошибка при выполнении действия: hubId={}, scenario={}, error={}",
					hubId, scenarioName, e.getMessage());
		}
	}

	private ActionTypeProto mapToProtoActionType(ActionType actionType) {
		return switch (actionType) {
			case HEATER -> ActionTypeProto.ACTIVATE;
			case FAN -> ActionTypeProto.DEACTIVATE;
			case LIGHT -> ActionTypeProto.INVERSE;
			case CONDITIONER -> ActionTypeProto.SET_VALUE;
		};
	}
}