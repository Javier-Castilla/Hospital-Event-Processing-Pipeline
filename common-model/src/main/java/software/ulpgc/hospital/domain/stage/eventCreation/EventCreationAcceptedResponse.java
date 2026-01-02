package software.ulpgc.hospital.domain.stage.eventCreation;

import java.util.UUID;

public record EventCreationAcceptedResponse(UUID eventCreationId, String statusURL) {
}
