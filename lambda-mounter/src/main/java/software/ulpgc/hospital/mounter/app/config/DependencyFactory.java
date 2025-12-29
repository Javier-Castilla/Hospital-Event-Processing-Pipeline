package software.ulpgc.hospital.mounter.app.config;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.mounter.app.processor.SimpleDataProcessor;
import software.ulpgc.hospital.mounter.app.repository.S3EventReader;
import software.ulpgc.hospital.mounter.app.repository.DynamoDatamartRepository;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;

public class DependencyFactory {

    private static DependencyFactory instance;
    private final DataProcessor dataProcessor;

    private DependencyFactory() {
        String bucketName = System.getenv("BUCKET_NAME");
        String tableName = System.getenv("TABLE_NAME");

        S3Client s3Client = S3Client.builder().build();
        DynamoDbClient dynamoClient = DynamoDbClient.builder().build();

        EventDeserializer deserializer = new JacksonEventDeserializer();

        S3EventReader reader = new S3EventReader(s3Client, bucketName, deserializer);
        DynamoDatamartRepository writer = new DynamoDatamartRepository(dynamoClient, tableName);

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
