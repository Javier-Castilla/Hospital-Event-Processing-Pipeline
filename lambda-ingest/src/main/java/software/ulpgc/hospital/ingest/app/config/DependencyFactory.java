package software.ulpgc.hospital.ingest.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventSerializer;
import software.ulpgc.hospital.app.stage.repository.DynamoDBEventCreationStatusRepository;
import software.ulpgc.hospital.app.stage.repository.EventCreationStatusItemMapper;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.ingest.app.storage.S3EventStorage;
import software.ulpgc.hospital.ingest.domain.processor.EventProcessor;
import software.ulpgc.hospital.ingest.domain.processor.SimpleEventProcessor;
import software.ulpgc.hospital.ingest.domain.storage.EventStorage;
import software.ulpgc.hospital.domain.model.serialization.EventDeserializer;
import software.ulpgc.hospital.domain.model.serialization.EventSerializer;

public class DependencyFactory {
    private static DependencyFactory instance;

    private final EventProcessor eventProcessor;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    private DependencyFactory() {
        String bucketName = System.getenv("BUCKET_NAME");
        String region = System.getenv("AWS_REGION");
        String eventStatusTable = System.getenv("STATUS_TABLE_NAME");

        EventSerializer serializer = new JacksonEventSerializer();
        EventDeserializer deserializer = new JacksonEventDeserializer();

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        EventStorage storage = new S3EventStorage(s3Client, bucketName, serializer);

        this.eventProcessor = new SimpleEventProcessor(deserializer, storage);

        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();
        this.eventCreationStatusRepository = new DynamoDBEventCreationStatusRepository(dynamoDb, eventStatusTable, new EventCreationStatusItemMapper());
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    public EventCreationStatusRepository getEventCreationStatusRepository() {
        return eventCreationStatusRepository;
    }
}
