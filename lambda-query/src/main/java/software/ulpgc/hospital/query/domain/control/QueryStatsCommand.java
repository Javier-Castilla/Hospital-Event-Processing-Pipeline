package software.ulpgc.hospital.query.domain.control;

import software.ulpgc.hospital.model.DepartmentStats;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.List;
import java.util.Map;

public class QueryStatsCommand implements Command {
    private final Input input;
    private final Output output;
    private final DatamartRepository datamartRepository;

    public QueryStatsCommand(Input input, Output output, DatamartRepository datamartRepository) {
        this.input = input;
        this.output = output;
        this.datamartRepository = datamartRepository;
    }

    @Override
    public Response execute() {
        try {
            return this.output.result(this.datamartRepository.query(this.input.query()));
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public interface Input {
        Map<String, String> query();
    }

    public interface Output {
        Response result(List<DepartmentStats> stats);
    }
}
