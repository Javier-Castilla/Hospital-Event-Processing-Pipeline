package software.ulpgc.hospital.feeder.domain.publisher;

import java.util.List;
import java.util.Map;

public interface MessagePublisher {
    PublishResult publish(String message, Map<String, String> attributes) throws PublishException;
}
