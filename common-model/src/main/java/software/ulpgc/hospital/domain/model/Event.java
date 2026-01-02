package software.ulpgc.hospital.domain.model;

import java.sql.Timestamp;
import java.util.UUID;

public interface Event {
    UUID getStreamId();
    Timestamp getTimestamp();
    EventType getEventType();
}
