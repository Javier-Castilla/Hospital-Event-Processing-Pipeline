package software.ulpgc.hospital.mounter.app.processor;

import software.ulpgc.hospital.mounter.domain.processor.ProcessException;
import software.ulpgc.hospital.mounter.domain.processor.ProcessResult;

public abstract class AbstractEventProcessor {

    public final ProcessResult processEvent(String input) throws ProcessException {
        validate(input);
        Object data = extract(input);
        Object transformed = transform(data);
        String location = load(transformed);
        return createResult(extractId(data), location);
    }

    protected abstract void validate(String input) throws ProcessException;
    protected abstract Object extract(String input) throws ProcessException;
    protected abstract Object transform(Object data) throws ProcessException;
    protected abstract String load(Object data) throws ProcessException;
    protected abstract String extractId(Object data);

    private ProcessResult createResult(String id, String location) {
        return new ProcessResult(id, location, true);
    }
}
