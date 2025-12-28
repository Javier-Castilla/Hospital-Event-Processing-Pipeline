package software.ulpgc.hospital.model.serialization;

import software.ulpgc.hospital.model.Event;

public interface EventDeserializer<T extends Event> {
    T deserialize(String json) throws SerializationException;
}
