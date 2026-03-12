package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.entity.Sensor;
import ru.yandex.practicum.analyzer.repository.SensorRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {

	private final SensorRepository sensorRepository;

	@Transactional(readOnly = true)
	public boolean exists(String sensorId, String hubId) {
		return sensorRepository.findByIdAndHubId(sensorId, hubId).isPresent();
	}

	@Transactional
	public void addDevice(String hubId, String sensorId) {
		if (exists(sensorId, hubId)) {
			log.info("Устройство уже существует: sensorId={}, hubId={}", sensorId, hubId);
			return;
		}
		Sensor sensor = new Sensor();
		sensor.setId(sensorId);
		sensor.setHubId(hubId);
		sensorRepository.save(sensor);
		log.info("Устройство добавлено: sensorId={}, hubId={}", sensorId, hubId);
	}

	@Transactional
	public void removeDevice(String hubId, String sensorId) {
		sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(sensor -> {
			sensorRepository.delete(sensor);
			log.info("Устройство удалено: sensorId={}, hubId={}", sensorId, hubId);
		});
	}
}
