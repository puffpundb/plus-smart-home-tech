package hubDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceAddedEventDto extends HubEventDto {
	@NotBlank
	String id;

	@NotBlank
	String type;

	@Override
	public HubEventType getType() {
		return HubEventType.DEVICE_ADDED_EVENT;
	}
}
