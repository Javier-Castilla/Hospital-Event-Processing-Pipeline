package software.ulpgc.hospital.app.docs;

import java.lang.reflect.RecordComponent;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordSchema {
    public static Map<String, Object> of(Class<?> type) {
        if (!type.isRecord()) {
            return Map.of("type", javaType(type));
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = java.util.Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        for (RecordComponent c : type.getRecordComponents()) {
            properties.put(c.getName(), schemaFor(c.getType()));
        }

        return Map.of(
                "type", "object",
                "required", required,
                "properties", properties
        );
    }

    private static Map<String, Object> schemaFor(Class<?> type) {
        if (type.isRecord()) return of(type);
        if (type.isEnum()) return Map.of("type", "string", "enum", enumValues(type));
        if (type.equals(UUID.class)) return Map.of("type", "string", "format", "uuid");
        if (type.equals(Timestamp.class)) return Map.of("type", "string", "format", "date-time");
        return Map.of("type", javaType(type));
    }

    private static List<String> enumValues(Class<?> enumType) {
        return java.util.Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .toList();
    }

    private static String javaType(Class<?> type) {
        if (type.equals(String.class)) return "string";
        if (type.equals(Integer.class) || type.equals(int.class)) return "integer";
        if (type.equals(Long.class) || type.equals(long.class)) return "integer";
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return "boolean";
        return type.getSimpleName();
    }
}
