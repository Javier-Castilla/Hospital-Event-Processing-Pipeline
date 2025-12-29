package software.ulpgc.hospital.app.implementation.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.ulpgc.hospital.model.ConsultationEvent;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.SerializationException;

public class JacksonEventDeserializer implements EventDeserializer {
    private final ObjectMapper mapper;

    public JacksonEventDeserializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public Event deserialize(String json) throws SerializationException {
        try {
            JsonNode node = mapper.readTree(json);
            String eventType = node.get("eventType").asText();

            return switch (eventType) {
                case "ADMISSION" -> mapper.treeToValue(node, AdmissionEvent.class);
                case "CONSULTATION" -> mapper.treeToValue(node, ConsultationEvent.class);
                default -> throw new SerializationException("Unknown event type: " + eventType, null);
            };
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize event", e);
        }
    }
}
