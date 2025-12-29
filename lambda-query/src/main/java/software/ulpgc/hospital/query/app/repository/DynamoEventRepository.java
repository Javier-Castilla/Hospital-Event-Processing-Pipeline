package software.ulpgc.hospital.query.app.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoEventRepository implements EventRepository {
    private final DynamoDbClient dynamoClient;
    private final String tableName;

    public DynamoEventRepository(DynamoDbClient dynamoClient, String tableName) {
        this.dynamoClient = dynamoClient;
        this.tableName = tableName;
    }

    @Override
    public Map<String, Object> findById(String eventId) throws RepositoryException {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("eventId", AttributeValue.builder().s(eventId).build());

            GetItemRequest getRequest = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = dynamoClient.getItem(getRequest);

            if (!response.hasItem()) {
                return null;
            }

            return convertAttributeMapToMap(response.item());
        } catch (DynamoDbException e) {
            throw new RepositoryException("Failed to read from DynamoDB: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RepositoryException("Unexpected error querying DynamoDB", e);
        }
    }

    @Override
    public List<Map<String, Object>> findAll() throws RepositoryException {
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .build();

            ScanResponse response = dynamoClient.scan(scanRequest);

            return response.items().stream()
                    .map(this::convertAttributeMapToMap)
                    .collect(Collectors.toList());
        } catch (DynamoDbException e) {
            throw new RepositoryException("Failed to scan DynamoDB: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RepositoryException("Unexpected error scanning DynamoDB", e);
        }
    }

    private Map<String, Object> convertAttributeMapToMap(Map<String, AttributeValue> attributeMap) {
        Map<String, Object> result = new HashMap<>();
        attributeMap.forEach((key, value) -> {
            if (value.s() != null) {
                result.put(key, value.s());
            } else if (value.n() != null) {
                result.put(key, value.n());
            }
        });
        return result;
    }
}
