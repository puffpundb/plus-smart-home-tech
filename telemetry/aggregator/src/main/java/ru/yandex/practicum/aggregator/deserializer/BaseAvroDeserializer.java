package ru.yandex.practicum.aggregator.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
	private final DecoderFactory decoderFactory;
	private final Schema schema;

	public BaseAvroDeserializer(Schema schema) {
		this.decoderFactory = DecoderFactory.get();
		this.schema = schema;
	}

	@Override
	public T deserialize(String s, byte[] data) {
		if (data == null) return null;

		try {
			SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);

			return reader.read(null, decoderFactory.binaryDecoder(data, null));
		} catch (IOException e) {
			throw new RuntimeException("Ошибка десериализации Avro", e);
		}
	}
}
