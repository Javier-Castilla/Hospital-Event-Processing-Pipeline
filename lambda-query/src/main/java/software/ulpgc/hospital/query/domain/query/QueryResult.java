package software.ulpgc.hospital.query.domain.query;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record QueryResult(
        List<Map<String, Object>> data,
        int count,
        boolean success,
        String message,
        Map<String, Object> metadata
) {
    public static class Builder {
        private List<Map<String, Object>> data = Collections.emptyList();
        private int count = 0;
        private boolean success = true;
        private String message = "";
        private final Map<String, Object> metadata = new HashMap<>();

        public Builder data(List<Map<String, Object>> data) {
            this.data = data;
            this.count = data.size();
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public QueryResult build() {
            metadata.put("timestamp", Instant.now().toString());
            return new QueryResult(data, count, success, message, metadata);
        }
    }
}
