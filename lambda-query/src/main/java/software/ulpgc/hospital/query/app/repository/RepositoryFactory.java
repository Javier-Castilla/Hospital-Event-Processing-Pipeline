package software.ulpgc.hospital.query.app.repository;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

public class RepositoryFactory {
    
    public static EventRepository createRepository(String type, String tableName, String region) {
        EventRepository baseRepository = switch (type.toLowerCase()) {
            case "dynamodb" -> createDynamoRepository(tableName, region);
            case "mock" -> new MockEventRepository();
            default -> throw new IllegalArgumentException("Unknown repository type: " + type);
        };

        // Aplicar decoradores
        baseRepository = new LoggingEventRepository(baseRepository);
        baseRepository = new MetricsEventRepository(baseRepository);
        
        return baseRepository;
    }

    private static EventRepository createDynamoRepository(String tableName, String region) {
        DynamoDbClient dynamoClient = DynamoDbClient.builder()
            .region(Region.of(region != null ? region : "us-east-1"))
            .build();
        return new DynamoEventRepository(dynamoClient, tableName);
    }
}
