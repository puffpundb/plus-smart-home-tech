package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioConditionId;

@Entity
@Table(name = "scenario_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioCondition {

	@EmbeddedId
	private ScenarioConditionId id;

	@MapsId("scenarioId")
	@ManyToOne
	@JoinColumn(name = "scenario_id", nullable = false)
	private Scenario scenario;

	@MapsId("sensorId")
	@ManyToOne
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@MapsId("conditionId")
	@ManyToOne
	@JoinColumn(name = "condition_id", nullable = false)
	private Condition condition;
}
