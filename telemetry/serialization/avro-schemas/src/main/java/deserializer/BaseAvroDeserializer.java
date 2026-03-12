package deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
	private final DecoderFactory decoderFactory;
	private final Schema schema;

	public BaseAvroDeserializer(Schema schema) {
		this.decoderFactory = DecoderFactory.get();
		this.schema = schema;
	}

	@Override
	public T deserialize(String s, byte[] data) {
		if (data == null) {
			log.info("Received null data for topic: {}", s);
			return null;
		}

		log.info("Deserializing {} bytes from topic: {}", data.length, s);

		try {
			SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
			T result = reader.read(null, decoderFactory.binaryDecoder(data, null));
			log.info("Successfully deserialized Avro object: {}", result);
			return result;
		} catch (IOException e) {
			log.error("Ошибка десериализации Avro для topic={}, schema={}",
					s, schema.getName(), e);
			throw new RuntimeException("Ошибка десериализации Avro: " + e.getMessage(), e);
		}
	}
}
