package hubDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioRemovedEventDto extends HubEventDto {
	@NotBlank
	private String name;

	@Override
	public HubEventType getType() {
		return HubEventType.SCENARIO_REMOVED_EVENT;
	}
}
