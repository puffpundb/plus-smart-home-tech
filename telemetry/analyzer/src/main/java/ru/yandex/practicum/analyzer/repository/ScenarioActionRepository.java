package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.entity.ScenarioAction;
import ru.yandex.practicum.analyzer.entity.keys.ScenarioActionId;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
	@Modifying
	@Query("DELETE FROM ScenarioAction a WHERE a.id.scenarioId = :scenarioId")
	void deleteByScenarioId(@Param("scenarioId") Long scenarioId);
}
