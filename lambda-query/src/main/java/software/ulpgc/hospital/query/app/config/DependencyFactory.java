package software.ulpgc.hospital.query.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.query.app.repository.DynamoDatamartRepository;
import software.ulpgc.hospital.query.app.repository.S3EventRepository;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

public class DependencyFactory {
    private static DependencyFactory instance;
    private final DatamartRepository datamartRepository;
    private final EventRepository eventRepository;

    private DependencyFactory() {
        String datamartTable = System.getenv("DATAMART_TABLE");
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
}
