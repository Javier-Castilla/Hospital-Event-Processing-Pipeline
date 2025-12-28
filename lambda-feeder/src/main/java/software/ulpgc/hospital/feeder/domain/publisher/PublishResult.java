package software.ulpgc.hospital.feeder.domain.publisher;

public class PublishResult {
    private final String messageId;
    private final boolean success;

    public PublishResult(String messageId, boolean success) {
        this.messageId = messageId;
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public boolean isSuccess() {
        return success;
    }
}
