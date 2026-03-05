package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "operation")
	private String operation;

	@Column(name = "value")
	private Integer value;
}
