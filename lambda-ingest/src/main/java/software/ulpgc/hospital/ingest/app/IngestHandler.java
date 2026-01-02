package software.ulpgc.hospital.ingest.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStage;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.ingest.app.config.DependencyFactory;
import software.ulpgc.hospital.ingest.domain.processor.EventProcessor;
import software.ulpgc.hospital.ingest.domain.processor.ProcessResult;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class IngestHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    private final EventProcessor eventProcessor;
    private final EventCreationStatusRepository eventCreationStatusRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngestHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.eventProcessor = factory.getEventProcessor();
        this.eventCreationStatusRepository = factory.getEventCreationStatusRepository();
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        context.getLogger().log("Processing records=" + event.getRecords().size() + " requestId=" + context.getAwsRequestId());
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            String messageId = message.getMessageId();
            try {
                String body = message.getBody();
                String payload = extractPayload(body);
                Map<String, SQSEvent.MessageAttribute> attributes = message.getMessageAttributes();
                ProcessResult result = eventProcessor.process(payload, mapAttributesToString(attributes));
                if (result.isSuccess()) updateStored(UUID.fromString(result.getEventId()), result.getStorageLocation());
                else updateFailed(UUID.fromString(result.getEventId()), "Failed to store event. messageId=" + messageId);
                if (!result.isSuccess()) failures.add(new SQSBatchResponse.BatchItemFailure(messageId));
            } catch (Exception e) {
                failures.add(new SQSBatchResponse.BatchItemFailure(messageId));
                context.getLogger().log("Error messageId=" + messageId + " requestId=" + context.getAwsRequestId() + " message=" + e.getMessage());
            }
        }
        return new SQSBatchResponse(failures);
    }

    private Map<String, String> mapAttributesToString(Map<String, SQSEvent.MessageAttribute> attributes) {
        return attributes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStringValue()));
    }

    private void updateStored(UUID eventId, String storageLocation) {
        Optional<EventCreationStatus> currentOpt = eventCreationStatusRepository.findByEventId(eventId);
        if (currentOpt.isEmpty()) return;
        EventCreationStatus current = currentOpt.get();
        Instant now = Instant.now();
        EventCreationStatus updated = new EventCreationStatus(
                current.id(),
                current.eventType(),
                current.eventId(),
                EventCreationStage.STORED,
                current.createdAt(),
                now,
                storageLocation,
                null
        );
        eventCreationStatusRepository.update(updated);
    }

    private void updateFailed(UUID eventId, String error) {
        Optional<EventCreationStatus> currentOpt = eventCreationStatusRepository.findByEventId(eventId);
        if (currentOpt.isEmpty()) return;
        EventCreationStatus current = currentOpt.get();
        Instant now = Instant.now();
        EventCreationStatus updated = new EventCreationStatus(
                current.id(),
                current.eventType(),
                current.eventId(),
                EventCreationStage.FAILED,
                current.createdAt(),
                now,
                current.s3Location(),
                error
        );
        eventCreationStatusRepository.update(updated);
    }

    private String extractPayload(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode eventJsonNode = node.get("eventJson");
            if (eventJsonNode != null && !eventJsonNode.isNull() && eventJsonNode.isTextual()) return eventJsonNode.asText();
            JsonNode eventNode = node.get("event");
            if (eventNode != null && !eventNode.isNull()) return objectMapper.writeValueAsString(eventNode);
            return body;
        } catch (Exception e) {
            return body;
        }
    }
}
