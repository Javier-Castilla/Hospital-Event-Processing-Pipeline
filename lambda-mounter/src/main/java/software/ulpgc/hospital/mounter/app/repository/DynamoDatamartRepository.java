package software.ulpgc.hospital.mounter.app.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.ulpgc.hospital.model.DepartmentStats;
import software.ulpgc.hospital.mounter.domain.repository.DatamartRepository;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DynamoDatamartRepository implements DatamartRepository {
    private final DynamoDbClient dynamoClient;
    private final String tableName;

    public DynamoDatamartRepository(DynamoDbClient dynamoClient, String tableName) {
        this.dynamoClient = dynamoClient;
        this.tableName = tableName;
    }

    @Override
    public DepartmentStats getOrCreate(String department, String date) throws RepositoryException {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("pk", AttributeValue.builder().s("DEPT#" + department + "#" + date).build());

            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = dynamoClient.getItem(request);

            if (!response.hasItem()) {
                return DepartmentStats.empty(department, LocalDate.parse(date));
            }

            Map<String, AttributeValue> item = response.item();
            return new DepartmentStats(
                    department,
                    LocalDate.parse(date),
                    Integer.parseInt(item.get("totalAdmissions").n()),
                    Integer.parseInt(item.get("emergencyAdmissions").n()),
                    Integer.parseInt(item.get("scheduledAdmissions").n()),
                    Integer.parseInt(item.get("transferAdmissions").n())
            );
        } catch (Exception e) {
            throw new RepositoryException("Failed to get stats", e);
        }
    }

    @Override
    public void save(DepartmentStats stats) throws RepositoryException {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("pk", AttributeValue.builder().s(stats.getPartitionKey()).build());
            item.put("department", AttributeValue.builder().s(stats.department()).build());
            item.put("date", AttributeValue.builder().s(stats.date().toString()).build());
            item.put("totalAdmissions", AttributeValue.builder().n(String.valueOf(stats.totalAdmissions())).build());
            item.put("emergencyAdmissions", AttributeValue.builder().n(String.valueOf(stats.emergencyAdmissions())).build());
            item.put("scheduledAdmissions", AttributeValue.builder().n(String.valueOf(stats.scheduledAdmissions())).build());
            item.put("transferAdmissions", AttributeValue.builder().n(String.valueOf(stats.transferAdmissions())).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoClient.putItem(request);
        } catch (Exception e) {
            throw new RepositoryException("Failed to save stats", e);
        }
    }
}
