package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioActionId;

@Entity
@Table(name = "scenario_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioAction {

	@EmbeddedId
	private ScenarioActionId id;

	@MapsId("scenarioId")
	@ManyToOne
	@JoinColumn(name = "scenario_id", nullable = false)
	private Scenario scenario;

	@MapsId("sensorId")
	@ManyToOne
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@MapsId("actionId")
	@ManyToOne
	@JoinColumn(name = "action_id", nullable = false)
	private Action action;
}
