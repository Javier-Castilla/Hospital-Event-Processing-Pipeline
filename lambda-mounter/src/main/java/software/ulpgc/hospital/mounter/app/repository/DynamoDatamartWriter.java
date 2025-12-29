package software.ulpgc.hospital.mounter.app.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.mounter.domain.repository.DatamartWriter;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryException;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryResult;

import java.util.HashMap;
import java.util.Map;

public class DynamoDatamartWriter implements DatamartWriter {
    private final DynamoDbClient dynamoClient;
    private final String tableName;

    public DynamoDatamartWriter(DynamoDbClient dynamoClient, String tableName) {
        this.dynamoClient = dynamoClient;
        this.tableName = tableName;
    }

    @Override
    public RepositoryResult write(Event event) throws RepositoryException {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("eventId", AttributeValue.builder().s(event.getStreamId().toString()).build());
            item.put("timestamp", AttributeValue.builder().s(event.getTimestamp().toString()).build());
            item.put("eventType", AttributeValue.builder().s(event.getClass().getSimpleName()).build());

            PutItemRequest putRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoClient.putItem(putRequest);

            String location = String.format("dynamodb://%s/%s", tableName, event.getStreamId());
            return new RepositoryResult(location, true);
        } catch (DynamoDbException e) {
            throw new RepositoryException("Failed to write to DynamoDB: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RepositoryException("Unexpected error writing to DynamoDB", e);
        }
    }
}
