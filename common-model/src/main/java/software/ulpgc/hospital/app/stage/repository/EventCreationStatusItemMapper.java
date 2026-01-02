package software.ulpgc.hospital.app.stage.repository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.ulpgc.hospital.domain.model.EventType;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStage;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EventCreationStatusItemMapper {
    Map<String, AttributeValue> toItem(EventCreationStatus s) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(s.id().toString()).build());
        item.put("eventType", AttributeValue.builder().s(s.eventType().name()).build());
        item.put("eventId", AttributeValue.builder().s(s.eventId().toString()).build());
        item.put("stage", AttributeValue.builder().s(s.stage().name()).build());
        item.put("createdAt", AttributeValue.builder().s(s.createdAt().toString()).build());
        item.put("updatedAt", AttributeValue.builder().s(s.updatedAt().toString()).build());
        putOpt(item, "s3Location", s.s3Location());
        putOpt(item, "error", s.error());
        return item;
    }

    EventCreationStatus fromItem(Map<String, AttributeValue> item) {
        return new EventCreationStatus(
                UUID.fromString(req(item, "id")),
                EventType.valueOf(req(item, "eventType")),
                UUID.fromString(req(item, "eventId")),
                EventCreationStage.valueOf(req(item, "stage")),
                Instant.parse(req(item, "createdAt")),
                Instant.parse(req(item, "updatedAt")),
                opt(item, "s3Location"),
                opt(item, "error")
        );
    }

    private static void putOpt(Map<String, AttributeValue> item, String k, String v) {
        if (v != null) item.put(k, AttributeValue.builder().s(v).build());
    }

    private static String req(Map<String, AttributeValue> item, String k) {
        String v = opt(item, k);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing attribute: " + k);
        return v;
    }

    private static String opt(Map<String, AttributeValue> item, String k) {
        AttributeValue v = item.get(k);
        return v == null ? null : v.s();
    }
}
