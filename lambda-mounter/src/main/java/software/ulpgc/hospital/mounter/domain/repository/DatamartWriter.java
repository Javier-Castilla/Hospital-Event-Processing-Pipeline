package software.ulpgc.hospital.mounter.domain.repository;

import software.ulpgc.hospital.model.Event;

public interface DatamartWriter {
    RepositoryResult write(Event event) throws RepositoryException;
}
