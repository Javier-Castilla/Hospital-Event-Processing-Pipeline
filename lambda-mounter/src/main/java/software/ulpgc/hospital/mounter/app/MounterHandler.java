package software.ulpgc.hospital.mounter.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import software.ulpgc.hospital.mounter.app.config.DependencyFactory;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;
import software.ulpgc.hospital.mounter.domain.processor.ProcessResult;

public class MounterHandler implements RequestHandler<S3Event, Void> {
    private final DataProcessor dataProcessor;

    public MounterHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.dataProcessor = factory.getDataProcessor();
    }

    @Override
    public Void handleRequest(S3Event event, Context context) {
        context.getLogger().log("Processing " + event.getRecords().size() + " S3 events");

        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            try {
                String s3Key = record.getS3().getObject().getKey();
                context.getLogger().log("Processing S3 object: " + s3Key);

                ProcessResult result = dataProcessor.process(s3Key);

                if (result.isSuccess()) {
                    context.getLogger().log("Successfully processed event " + result.getEventId() +
                            " to " + result.getDatamartLocation());
                } else {
                    context.getLogger().log("Failed to process event from: " + s3Key);
                }
            } catch (Exception e) {
                context.getLogger().log("Error processing S3 event: " + e.getMessage());
            }
        }

        return null;
    }
}
