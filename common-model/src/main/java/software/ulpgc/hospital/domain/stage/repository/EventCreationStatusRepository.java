package software.ulpgc.hospital.domain.stage.repository;

import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;

import java.util.Optional;
import java.util.UUID;

public interface EventCreationStatusRepository {
    EventCreationStatus create(EventCreationStatus status);
    EventCreationStatus update(EventCreationStatus status);
    Optional<EventCreationStatus> findById(UUID id);
    Optional<EventCreationStatus> findByEventId(UUID eventId);
}
