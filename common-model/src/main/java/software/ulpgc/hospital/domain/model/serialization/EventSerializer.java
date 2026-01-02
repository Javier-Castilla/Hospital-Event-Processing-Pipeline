package software.ulpgc.hospital.domain.model.serialization;

import software.ulpgc.hospital.domain.model.Event;

public interface EventSerializer {
    String serialize(Event event) throws SerializationException;
}
