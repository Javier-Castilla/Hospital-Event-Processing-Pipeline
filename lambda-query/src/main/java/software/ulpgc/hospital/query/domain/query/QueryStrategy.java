package software.ulpgc.hospital.query.domain.query;

import java.util.Map;

public interface QueryStrategy {
    QueryResult execute(Map<String, String> parameters) throws QueryException;
}
