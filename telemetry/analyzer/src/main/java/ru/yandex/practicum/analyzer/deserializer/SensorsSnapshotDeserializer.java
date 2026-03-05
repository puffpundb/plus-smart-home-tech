package ru.yandex.practicum.analyzer.deserializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.IOException;
import java.util.Map;

public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {
	@Override
	public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
		if (data == null) return null;

		try {
			SpecificDatumReader<SensorsSnapshotAvro> reader =
					new SpecificDatumReader<>(SensorsSnapshotAvro.getClassSchema());
			BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);

			return reader.read(null, decoder);
		} catch (IOException e) {
			throw new RuntimeException("Ошибка десериализации SensorsSnapshotAvro", e);
		}
	}
}
