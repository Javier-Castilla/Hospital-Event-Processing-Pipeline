package software.ulpgc.hospital.domain.model.serialization;

import software.ulpgc.hospital.domain.model.Event;

public interface EventDeserializer {
    Event deserialize(String json) throws SerializationException;
}
