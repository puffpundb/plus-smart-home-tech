package deserializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.IOException;

public class HubEventDeserializer implements Deserializer<HubEventAvro> {
	@Override
	public HubEventAvro deserialize(String topic, byte[] data) {
		if (data == null) return null;

		try {
			SpecificDatumReader<HubEventAvro> reader =
					new SpecificDatumReader<>(HubEventAvro.getClassSchema());
			BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);

			return reader.read(null, decoder);
		} catch (IOException e) {
			throw new RuntimeException("Ошибка десериализации HubEventAvro", e);
		}
	}
}
