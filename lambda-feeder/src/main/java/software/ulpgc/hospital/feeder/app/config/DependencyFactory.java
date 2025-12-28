package software.ulpgc.hospital.feeder.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.app.publisher.SQSMessagePublisher;
import software.ulpgc.hospital.feeder.domain.validator.EventValidator;
import software.ulpgc.hospital.feeder.app.validator.JsonEventValidator;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.EventSerializer;
import software.ulpgc.hospital.app.windows.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.app.windows.serialization.JacksonEventSerializer;

public class DependencyFactory {
    private static DependencyFactory instance;

    private final EventDeserializer<Event> eventDeserializer;
    private final EventValidator eventValidator;
    private final EventSerializer eventSerializer;
    private final MessagePublisher messagePublisher;

    private DependencyFactory() {
        String queueUrl = System.getenv("QUEUE_URL");
        String region = System.getenv("AWS_REGION");

        this.eventDeserializer = new JacksonEventDeserializer<>(Event.class);
        this.eventValidator = new JsonEventValidator();
        this.eventSerializer = new JacksonEventSerializer();

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        this.messagePublisher = new SQSMessagePublisher(sqsClient, queueUrl);
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public EventDeserializer<Event> getEventDeserializer() {
        return eventDeserializer;
    }

    public EventValidator getEventValidator() {
        return eventValidator;
    }

    public EventSerializer getEventSerializer() {
        return eventSerializer;
    }

    public MessagePublisher getMessagePublisher() {
        return messagePublisher;
    }
}
