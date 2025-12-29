package software.ulpgc.hospital.mounter.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.mounter.app.processor.SimpleDataProcessor;
import software.ulpgc.hospital.mounter.app.repository.DynamoDatamartWriter;
import software.ulpgc.hospital.mounter.app.repository.S3EventReader;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;
import software.ulpgc.hospital.mounter.domain.repository.DatamartWriter;
import software.ulpgc.hospital.mounter.domain.repository.EventReader;

public class DependencyFactory {
    private static DependencyFactory instance;
    private final DataProcessor dataProcessor;

    private DependencyFactory() {
        String bucketName = System.getenv("BUCKET_NAME");
        String tableName = System.getenv("TABLE_NAME");
        String region = System.getenv("AWS_REGION");

        EventDeserializer deserializer = new JacksonEventDeserializer<>(Event.class);

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        EventReader reader = new S3EventReader(s3Client, bucketName, deserializer);
        DatamartWriter writer = new DynamoDatamartWriter(dynamoClient, tableName);

        this.dataProcessor = new SimpleDataProcessor(reader, writer);
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }
}
