package software.ulpgc.hospital.ingest.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import software.ulpgc.hospital.ingest.app.config.DependencyFactory;
import software.ulpgc.hospital.ingest.domain.processor.EventProcessor;
import software.ulpgc.hospital.ingest.domain.processor.ProcessResult;

import java.util.ArrayList;
import java.util.List;

public class IngestHandler implements RequestHandler<SQSEvent, Void> {

    private final EventProcessor eventProcessor;

    public IngestHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.eventProcessor = factory.getEventProcessor();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        context.getLogger().log("Processing " + event.getRecords().size() + " messages from SQS");

        List<String> failures = new ArrayList<>();

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                String messageBody = message.getBody();
                context.getLogger().log("Processing message: " + message.getMessageId());

                ProcessResult result = eventProcessor.process(messageBody);

                if (result.isSuccess()) {
                    context.getLogger().log("Successfully stored event " + result.getEventId() +
                            " at " + result.getStorageLocation());
                } else {
                    failures.add(message.getMessageId());
                    context.getLogger().log("Failed to store event from message: " + message.getMessageId());
                }

            } catch (Exception e) {
                failures.add(message.getMessageId());
                context.getLogger().log("Error processing message " + message.getMessageId() +
                        ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            context.getLogger().log("Failed to process " + failures.size() + " messages: " + failures);
        }

        return null;
    }
}
