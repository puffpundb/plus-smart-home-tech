package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "hub_id")
	private String hubId;
}
