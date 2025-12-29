package software.ulpgc.hospital.query.app.config;

import software.ulpgc.hospital.query.app.query.GetAllEventsStrategy;
import software.ulpgc.hospital.query.app.query.GetEventByIdStrategy;
import software.ulpgc.hospital.query.app.query.StrategyQueryService;
import software.ulpgc.hospital.query.app.repository.RepositoryFactory;
import software.ulpgc.hospital.query.domain.query.QueryService;
import software.ulpgc.hospital.query.domain.query.QueryStrategy;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.HashMap;
import java.util.Map;

public class DependencyFactory {
    private static DependencyFactory instance;
    private final QueryService queryService;

    private DependencyFactory() {
        String tableName = System.getenv("TABLE_NAME");
        String region = System.getenv("AWS_REGION");
        String repoType = System.getenv("REPO_TYPE") != null ?
                System.getenv("REPO_TYPE") : "dynamodb";

        EventRepository repository = RepositoryFactory.createRepository(
                repoType,
                tableName,
                region
        );

        Map<String, QueryStrategy> strategies = new HashMap<>();
        strategies.put("getAll", new GetAllEventsStrategy(repository));
        strategies.put("getById", new GetEventByIdStrategy(repository));

        this.queryService = new StrategyQueryService(strategies);
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public QueryService getQueryService() {
        return queryService;
    }
}
