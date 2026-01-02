package software.ulpgc.hospital.ingest.domain.processor;

import java.util.Map;

public interface EventProcessor {
    ProcessResult process(String messageBody, Map<String, String> attributes) throws ProcessException;
}
