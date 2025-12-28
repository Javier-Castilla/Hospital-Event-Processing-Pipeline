package software.ulpgc.hospital.ingest.domain.processor;

public interface EventProcessor {
    ProcessResult process(String messageBody) throws ProcessException;
}
