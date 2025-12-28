package software.ulpgc.hospital.feeder.domain.publisher;

public interface MessagePublisher {
    PublishResult publish(String message) throws PublishException;
}
