package hubDto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioConditionDto {
	String sensorId;
	String type;
	String operation;
	Object value;
}
