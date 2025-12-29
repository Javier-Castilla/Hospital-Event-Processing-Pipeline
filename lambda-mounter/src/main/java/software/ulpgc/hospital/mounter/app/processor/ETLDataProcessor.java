package software.ulpgc.hospital.mounter.app.processor;

import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.mounter.domain.processor.ProcessException;
import software.ulpgc.hospital.mounter.domain.repository.EventReader;
import software.ulpgc.hospital.mounter.domain.repository.DatamartWriter;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryResult;

public class ETLDataProcessor extends AbstractEventProcessor {
    private final EventReader reader;
    private final DatamartWriter writer;

    public ETLDataProcessor(EventReader reader, DatamartWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    protected void validate(String s3Key) throws ProcessException {
        if (s3Key == null || s3Key.isEmpty()) {
            throw new ProcessException("S3 key cannot be null or empty");
        }
        if (!s3Key.endsWith(".json")) {
            throw new ProcessException("Only JSON files are supported");
        }
    }

    @Override
    protected Object extract(String s3Key) throws ProcessException {
        try {
            return reader.read(s3Key);
        } catch (Exception e) {
            throw new ProcessException("Failed to extract data from S3: " + e.getMessage(), e);
        }
    }

    @Override
    protected Object transform(Object data) throws ProcessException {
        // Aquí puedes agregar transformaciones adicionales
        // Por ahora solo retornamos el dato tal cual
        return data;
    }

    @Override
    protected String load(Object data) throws ProcessException {
        try {
            Event event = (Event) data;
            RepositoryResult result = writer.write(event);
            return result.location();
        } catch (Exception e) {
            throw new ProcessException("Failed to load data to datamart: " + e.getMessage(), e);
        }
    }

    @Override
    protected String extractId(Object data) {
        Event event = (Event) data;
        return event.getStreamId().toString();
    }
}
