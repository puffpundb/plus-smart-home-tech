package ru.yandex.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.analyzer.entity.enums.ConditionType;
import ru.yandex.practicum.analyzer.entity.enums.Operation;

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
	@Enumerated(EnumType.STRING)
	private ConditionType type;

	@Column(name = "operation")
	@Enumerated(EnumType.STRING)
	private Operation operation;;

	@Column(name = "value")
	private Integer value;
}
