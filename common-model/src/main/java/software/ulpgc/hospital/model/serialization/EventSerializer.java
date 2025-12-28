package software.ulpgc.hospital.model.serialization;

import software.ulpgc.hospital.model.Event;

public interface EventSerializer {
    String serialize(Event event) throws SerializationException;
}
