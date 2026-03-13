package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hub_id")
	private String hubId;

	@Column(name = "name")
	private String name;

	@OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ScenarioCondition> scenarioConditions;

	@OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ScenarioAction> scenarioActions;
}