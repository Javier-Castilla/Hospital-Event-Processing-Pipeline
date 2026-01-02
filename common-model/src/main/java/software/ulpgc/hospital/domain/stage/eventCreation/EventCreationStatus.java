package software.ulpgc.hospital.domain.stage.eventCreation;

import software.ulpgc.hospital.domain.model.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventCreationStatus(
        UUID id,
        EventType eventType,
        UUID eventId,
        EventCreationStage stage,
        Instant createdAt,
        Instant updatedAt,
        String s3Location,
        String error
) {}
