package software.ulpgc.hospital.mounter.app.processor;

import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;
import software.ulpgc.hospital.mounter.domain.processor.ProcessException;
import software.ulpgc.hospital.mounter.domain.processor.ProcessResult;
import software.ulpgc.hospital.mounter.domain.repository.EventReader;
import software.ulpgc.hospital.mounter.domain.repository.DatamartWriter;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryResult;

public class SimpleDataProcessor implements DataProcessor {
    private final EventReader reader;
    private final DatamartWriter writer;

    public SimpleDataProcessor(EventReader reader, DatamartWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public ProcessResult process(String s3Key) throws ProcessException {
        try {
            Event event = reader.read(s3Key);
            RepositoryResult result = writer.write(event);

            return new ProcessResult(
                    event.getStreamId().toString(),
                    result.location(),
                    result.success()
            );
        } catch (Exception e) {
            throw new ProcessException("Failed to process event from S3: " + e.getMessage(), e);
        }
    }
}
