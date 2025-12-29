package software.ulpgc.hospital.mounter.domain.processor;

public interface DataProcessor {
    ProcessResult process(String s3Key) throws ProcessException;
}
