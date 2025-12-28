package software.ulpgc.hospital.feeder.publisher;

public interface MessagePublisher {
    PublishResult publish(String message) throws PublishException;
}
