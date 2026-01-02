package software.ulpgc.hospital.query.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.app.stage.repository.DynamoDBEventCreationStatusRepository;
import software.ulpgc.hospital.app.stage.repository.EventCreationStatusItemMapper;
import software.ulpgc.hospital.domain.model.serialization.EventDeserializer;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.query.app.repository.DynamoDatamartRepository;
import software.ulpgc.hospital.query.app.repository.S3EventRepository;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

public class DependencyFactory {
    private static DependencyFactory instance;
    private final DatamartRepository datamartRepository;
    private final EventRepository eventRepository;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    private DependencyFactory() {
        String datamartTable = System.getenv("DATAMART_TABLE");
        String eventStatusTable = System.getenv("STATUS_TABLE_NAME");
        String bucketName = System.getenv("BUCKET_NAME");
        String region = System.getenv("AWS_REGION");

        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        EventDeserializer deserializer = new JacksonEventDeserializer();

        this.datamartRepository = new DynamoDatamartRepository(dynamoClient, datamartTable);
        this.eventRepository = new S3EventRepository(s3Client, bucketName, deserializer);
        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();
        eventCreationStatusRepository = new DynamoDBEventCreationStatusRepository(dynamoDb, eventStatusTable, new EventCreationStatusItemMapper());
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public DatamartRepository getDatamartRepository() {
        return datamartRepository;
    }

    public EventRepository getEventRepository() {
        return eventRepository;
    }

    public EventCreationStatusRepository getEventCreationStatusRepository() {
        return eventCreationStatusRepository;
    }
}
