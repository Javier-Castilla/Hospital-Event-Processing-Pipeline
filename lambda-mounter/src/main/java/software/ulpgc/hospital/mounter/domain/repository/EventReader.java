package software.ulpgc.hospital.mounter.domain.repository;

import software.ulpgc.hospital.model.Event;

public interface EventReader {
    Event read(String s3Key) throws RepositoryException;
}
