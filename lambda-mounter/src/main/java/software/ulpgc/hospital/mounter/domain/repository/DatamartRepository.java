package software.ulpgc.hospital.mounter.domain.repository;

import software.ulpgc.hospital.domain.model.DepartmentStats;

public interface DatamartRepository {
    DepartmentStats getOrCreate(String department, String date) throws RepositoryException;
    void save(DepartmentStats stats) throws RepositoryException;
}
