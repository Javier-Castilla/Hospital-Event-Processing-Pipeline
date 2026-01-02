package software.ulpgc.hospital.domain.model;

import java.util.UUID;

public record PatientSnapshot(
            UUID patientId,
            String name,
            String surname,
            int age,
            String gender,
            String nationalId
    ) {}