package software.ulpgc.hospital.app.implementation.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventSerializer;
import software.ulpgc.hospital.model.serialization.SerializationException;

public class JacksonEventSerializer implements EventSerializer {
    private final ObjectMapper mapper;

    public JacksonEventSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public JacksonEventSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String serialize(Event event) throws SerializationException {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize event: " + event.getStreamId(), e);
        }
    }
}
