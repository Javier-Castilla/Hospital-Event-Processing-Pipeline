package software.ulpgc.hospital.app.windows;

import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.Department;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.builders.AdmissionEventBuilder;
import software.ulpgc.hospital.app.windows.serialization.JacksonEventSerializer;
import software.ulpgc.hospital.model.serialization.SerializationException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        List<Event> events = new ArrayList<Event>();

        for (int i = 0; i < 100000; i++) {
            events.add(
                    new AdmissionEventBuilder()
                            .withAdmissionType(AdmissionEvent.AdmissionType.EMERGENCY)
                            .withBedNumber("B-102")
                            .withDepartment(Department.CARDIOLOGY)
                            .withId(UUID.randomUUID())
                            .withTimestamp(Timestamp.from(java.time.Instant.now()))
                            .build()
            );
        }

        events.forEach(event -> {
            try {
                System.out.println(new JacksonEventSerializer().serialize(event));
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
