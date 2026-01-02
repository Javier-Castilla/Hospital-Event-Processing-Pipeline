package software.ulpgc.hospital.app.stage.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DynamoDBEventCreationStatusRepository implements EventCreationStatusRepository {
    private final DynamoDbClient dynamoDb;
    private final String tableName;
    private final EventCreationStatusItemMapper mapper;

    public DynamoDBEventCreationStatusRepository(DynamoDbClient dynamoDb, String tableName, EventCreationStatusItemMapper mapper) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
        this.mapper = mapper;
    }

    @Override
    public EventCreationStatus create(EventCreationStatus status) {
        try {
            dynamoDb.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(mapper.toItem(status))
                    .conditionExpression("attribute_not_exists(#id)")
                    .expressionAttributeNames(Map.of("#id", "id"))
                    .build());
            return status;
        } catch (ConditionalCheckFailedException e) {
            throw new RuntimeException("EventCreationStatus already exists: " + status.id(), e);
        }
    }

    @Override
    public EventCreationStatus update(EventCreationStatus status) {
        Map<String, AttributeValue> key = Map.of(
                "id", AttributeValue.builder().s(status.id().toString()).build()
        );

        Map<String, String> names = new HashMap<>();
        names.put("#id", "id");
        names.put("#stage", "stage");
        names.put("#updatedAt", "updatedAt");
        names.put("#s3Location", "s3Location");
        names.put("#error", "error");

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":stage", AttributeValue.builder().s(status.stage().name()).build());
        values.put(":updatedAt", AttributeValue.builder().s(status.updatedAt().toString()).build());

        StringBuilder setExpr = new StringBuilder("SET #stage = :stage, #updatedAt = :updatedAt");
        StringBuilder removeExpr = new StringBuilder();

        if (status.s3Location() != null) {
            values.put(":s3Location", AttributeValue.builder().s(status.s3Location()).build());
            setExpr.append(", #s3Location = :s3Location");
        } else {
            removeExpr.append("#s3Location");
        }

        if (status.error() != null) {
            values.put(":error", AttributeValue.builder().s(status.error()).build());
            setExpr.append(", #error = :error");
        } else {
            if (!removeExpr.isEmpty()) removeExpr.append(", ");
            removeExpr.append("#error");
        }

        String updateExpression = removeExpr.isEmpty()
                ? setExpr.toString()
                : setExpr + " REMOVE " + removeExpr;

        try {
            dynamoDb.updateItem(UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .updateExpression(updateExpression)
                    .conditionExpression("attribute_exists(#id)")
                    .expressionAttributeNames(names)
                    .expressionAttributeValues(values)
                    .build());
            return status;
        } catch (ConditionalCheckFailedException e) {
            throw new RuntimeException("EventCreationStatus not found: " + status.id(), e);
        }
    }

    @Override
    public Optional<EventCreationStatus> findById(UUID id) {
        GetItemResponse res = dynamoDb.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.builder().s(id.toString()).build()))
                .consistentRead(true)
                .build());

        if (!res.hasItem() || res.item().isEmpty()) return Optional.empty();

        return Optional.of(mapper.fromItem(res.item()));
    }

    private static final String EVENT_ID_INDEX = "eventId-index";

    public Optional<EventCreationStatus> findByEventId(UUID eventId) {
        QueryResponse res = dynamoDb.query(QueryRequest.builder()
                .tableName(tableName)
                .indexName(EVENT_ID_INDEX)
                .keyConditionExpression("#eventId = :eventId")
                .expressionAttributeNames(Map.of("#eventId", "eventId"))
                .expressionAttributeValues(Map.of(
                        ":eventId", AttributeValue.builder().s(eventId.toString()).build()
                ))
                .limit(1)
                .build());

        if (!res.hasItems() || res.items().isEmpty()) return Optional.empty();
        return Optional.of(mapper.fromItem(res.items().get(0)));
    }

}
