package serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class SensorEventAvroSerializer implements Serializer<SensorEventAvro> {
	private final EncoderFactory encoderFactory = EncoderFactory.get();

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		Serializer.super.configure(configs, isKey);
	}

	@Override
	public byte[] serialize(String s, SensorEventAvro sensorEventAvro) {
		if (sensorEventAvro == null) return null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
			SpecificDatumWriter<SensorEventAvro> datumWriter = new SpecificDatumWriter<>(SensorEventAvro.class);

			datumWriter.write(sensorEventAvro, encoder);
			encoder.flush();

			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Ошибка сериализации SensorEventAvro", e);
		}
	}

	@Override
	public byte[] serialize(String topic, Headers headers, SensorEventAvro data) {
		return Serializer.super.serialize(topic, headers, data);
	}

	@Override
	public void close() {
		Serializer.super.close();
	}
}
