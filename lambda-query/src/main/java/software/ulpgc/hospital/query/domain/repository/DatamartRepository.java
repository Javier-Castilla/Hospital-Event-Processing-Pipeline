package software.ulpgc.hospital.query.domain.repository;

import software.ulpgc.hospital.model.DepartmentStats;
import java.util.List;
import java.util.Map;

public interface DatamartRepository {
    DepartmentStats findById(String id) throws RepositoryException;
    List<DepartmentStats> query(Map<String, String> filters) throws RepositoryException;
}
