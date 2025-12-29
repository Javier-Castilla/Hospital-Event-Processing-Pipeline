package software.ulpgc.hospital.app.implementation.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.SerializationException;

public class JacksonEventDeserializer<T extends Event> implements EventDeserializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> eventClass;

    public JacksonEventDeserializer(Class<T> eventClass) {
        this.eventClass = eventClass;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public JacksonEventDeserializer(ObjectMapper mapper, Class<T> eventClass) {
        this.mapper = mapper;
        this.eventClass = eventClass;
    }

    @Override
    public T deserialize(String json) throws SerializationException {
        try {
            return mapper.readValue(json, eventClass);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize event from JSON", e);
        }
    }
}
