package software.ulpgc.hospital.query.app.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.ulpgc.hospital.model.DepartmentStats;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoDatamartRepository implements DatamartRepository {
    private final DynamoDbClient dynamoClient;
    private final String tableName;

    public DynamoDatamartRepository(DynamoDbClient dynamoClient, String tableName) {
        this.dynamoClient = dynamoClient;
        this.tableName = tableName;
    }

    @Override
    public DepartmentStats findById(String id) throws RepositoryException {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("pk", AttributeValue.builder().s(id).build());

            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = dynamoClient.getItem(request);

            if (!response.hasItem()) {
                throw new RepositoryException("Stats not found: " + id);
            }

            return mapToStats(response.item());
        } catch (DynamoDbException e) {
            throw new RepositoryException("Failed to get stats by id", e);
        }
    }

    @Override
    public List<DepartmentStats> query(Map<String, String> filters) throws RepositoryException {
        try {
            if (filters.containsKey("department") && filters.containsKey("date")) {
                return queryByDepartmentAndDate(filters.get("department"), filters.get("date"));
            }

            String filterExpression = buildFilterExpression(filters);
            Map<String, AttributeValue> expressionValues = buildExpressionValues(filters);
            Map<String, String> expressionNames = buildExpressionNames(filters);

            ScanRequest.Builder scanBuilder = ScanRequest.builder().tableName(tableName);

            if (!filterExpression.isEmpty()) {
                scanBuilder.filterExpression(filterExpression);
                scanBuilder.expressionAttributeValues(expressionValues);
                if (!expressionNames.isEmpty()) {
                    scanBuilder.expressionAttributeNames(expressionNames);
                }
            }

            ScanResponse response = dynamoClient.scan(scanBuilder.build());

            return response.items().stream()
                    .map(this::mapToStats)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RepositoryException("Failed to query stats", e);
        }
    }

    private List<DepartmentStats> queryByDepartmentAndDate(String department, String date) throws RepositoryException {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("pk", AttributeValue.builder().s("DEPT#" + department + "#" + date).build());

            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = dynamoClient.getItem(request);

            if (!response.hasItem()) {
                return List.of(DepartmentStats.empty(department, LocalDate.parse(date)));
            }

            return List.of(mapToStats(response.item()));
        } catch (Exception e) {
            throw new RepositoryException("Failed to get specific stats", e);
        }
    }

    private String buildFilterExpression(Map<String, String> filters) {
        StringBuilder expression = new StringBuilder();
        boolean first = true;

        if (filters.containsKey("department")) {
            expression.append("department = :department");
            first = false;
        }

        if (filters.containsKey("date")) {
            if (!first) expression.append(" AND ");
            expression.append("#d = :date");
            first = false;
        }

        if (filters.containsKey("minAdmissions")) {
            if (!first) expression.append(" AND ");
            expression.append("totalAdmissions >= :minAdmissions");
        }

        return expression.toString();
    }

    private Map<String, AttributeValue> buildExpressionValues(Map<String, String> filters) {
        Map<String, AttributeValue> values = new HashMap<>();

        if (filters.containsKey("department")) {
            values.put(":department", AttributeValue.builder().s(filters.get("department")).build());
        }

        if (filters.containsKey("date")) {
            values.put(":date", AttributeValue.builder().s(filters.get("date")).build());
        }

        if (filters.containsKey("minAdmissions")) {
            values.put(":minAdmissions", AttributeValue.builder().n(filters.get("minAdmissions")).build());
        }

        return values;
    }

    private Map<String, String> buildExpressionNames(Map<String, String> filters) {
        Map<String, String> names = new HashMap<>();

        if (filters.containsKey("date")) {
            names.put("#d", "date");
        }

        return names;
    }

    private DepartmentStats mapToStats(Map<String, AttributeValue> item) {
        return new DepartmentStats(
                item.get("department").s(),
                LocalDate.parse(item.get("date").s()),
                Integer.parseInt(item.get("totalAdmissions").n()),
                Integer.parseInt(item.get("emergencyAdmissions").n()),
                Integer.parseInt(item.get("scheduledAdmissions").n()),
                Integer.parseInt(item.get("transferAdmissions").n())
        );
    }
}
