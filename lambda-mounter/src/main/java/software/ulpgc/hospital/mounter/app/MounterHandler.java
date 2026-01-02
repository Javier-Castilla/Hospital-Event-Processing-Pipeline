package software.ulpgc.hospital.mounter.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStage;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.mounter.app.config.DependencyFactory;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;
import software.ulpgc.hospital.mounter.domain.processor.ProcessResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MounterHandler implements RequestHandler<S3Event, Void> {

    private final DataProcessor dataProcessor;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    public MounterHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.dataProcessor = factory.getDataProcessor();
        this.eventCreationStatusRepository = factory.getEventCreationStatusRepository();
    }

    @Override
    public Void handleRequest(S3Event event, Context context) {
        context.getLogger().log("Processing records=" + event.getRecords().size() + " requestId=" + context.getAwsRequestId());

        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            String s3Key = null;

            try {
                s3Key = record.getS3().getObject().getUrlDecodedKey();

                UUID eventCreationId = extractEventCreationId(s3Key);
                if (eventCreationId == null) {
                    context.getLogger().log("Skip. Cannot extract eventCreationId from key=" + s3Key);
                    continue;
                }

                Optional<EventCreationStatus> currentOpt = eventCreationStatusRepository.findById(eventCreationId);
                if (currentOpt.isEmpty()) {
                    context.getLogger().log("Skip. Status not found eventCreationId=" + eventCreationId + " key=" + s3Key);
                    continue;
                }

                EventCreationStatus current = currentOpt.get();
                ProcessResult result = dataProcessor.process(s3Key);
                Instant now = Instant.now();

                if (result.isSuccess()) {
                    EventCreationStatus updated = new EventCreationStatus(
                            current.id(),
                            current.eventType(),
                            current.eventId(),
                            EventCreationStage.MOUNTED,
                            current.createdAt(),
                            now,
                            result.getDatamartLocation(),
                            null
                    );
                    eventCreationStatusRepository.update(updated);
                    context.getLogger().log("OK. eventCreationId=" + eventCreationId + " key=" + s3Key);
                } else {
                    EventCreationStatus updated = new EventCreationStatus(
                            current.id(),
                            current.eventType(),
                            current.eventId(),
                            EventCreationStage.FAILED,
                            current.createdAt(),
                            now,
                            current.s3Location(),
                            "Failed to process key=" + s3Key
                    );
                    eventCreationStatusRepository.update(updated);
                    context.getLogger().log("FAIL. eventCreationId=" + eventCreationId + " key=" + s3Key);
                }

            } catch (Exception e) {
                context.getLogger().log("Error processing key=" + s3Key + " requestId=" + context.getAwsRequestId() + " message=" + e.getMessage());
                context.getLogger().log(getStackTrace(e));
            }
        }

        return null;
    }

    private static final Pattern EVENT_CREATION_ID =
            Pattern.compile("eventCreationId=([0-9a-fA-F\\-]{36})");

    private UUID extractEventCreationId(String s3Key) {
        if (s3Key == null) return null;
        Matcher m = EVENT_CREATION_ID.matcher(s3Key);
        if (!m.find()) return null;
        try {
            return UUID.fromString(m.group(1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
