package software.ulpgc.hospital.model;

import java.sql.Timestamp;
import java.util.UUID;

public interface Event {
    UUID getStreamId();
    Timestamp getTimestamp();
    EventType getEventType();
}
