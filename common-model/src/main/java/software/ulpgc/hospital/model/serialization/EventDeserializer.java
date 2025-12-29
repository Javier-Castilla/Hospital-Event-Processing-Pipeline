package software.ulpgc.hospital.model.serialization;

import software.ulpgc.hospital.model.Event;

public interface EventDeserializer {
    Event deserialize(String json) throws SerializationException;
}
