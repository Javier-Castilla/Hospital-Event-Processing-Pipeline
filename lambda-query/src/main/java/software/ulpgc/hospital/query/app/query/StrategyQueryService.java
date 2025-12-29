package software.ulpgc.hospital.query.app.query;

import software.ulpgc.hospital.query.domain.query.QueryException;
import software.ulpgc.hospital.query.domain.query.QueryResult;
import software.ulpgc.hospital.query.domain.query.QueryService;
import software.ulpgc.hospital.query.domain.query.QueryStrategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyQueryService implements QueryService {
    private final Map<String, QueryStrategy> strategies;

    public StrategyQueryService(Map<String, QueryStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public QueryResult getEventById(String eventId) throws QueryException {
        QueryStrategy strategy = strategies.get("getById");
        if (strategy == null) {
            throw new QueryException("Strategy 'getById' not found");
        }
        Map<String, String> params = new HashMap<>();
        params.put("eventId", eventId);
        return strategy.execute(params);
    }

    @Override
    public QueryResult getAllEvents() throws QueryException {
        QueryStrategy strategy = strategies.get("getAll");
        if (strategy == null) {
            throw new QueryException("Strategy 'getAll' not found");
        }
        return strategy.execute(new HashMap<>());
    }
}
