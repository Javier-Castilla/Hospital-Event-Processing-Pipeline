package software.ulpgc.hospital.feeder.app.publisher;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.domain.publisher.PublishException;
import software.ulpgc.hospital.feeder.domain.publisher.PublishResult;

public class SQSMessagePublisher implements MessagePublisher {
    private final SqsClient sqsClient;
    private final String queueUrl;

    public SQSMessagePublisher(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public PublishResult publish(String message) throws PublishException {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build();
            SendMessageResponse response = sqsClient.sendMessage(request);
            return new PublishResult(response.messageId(), true);
        } catch (SqsException e) {
            throw new PublishException("Failed to publish message to SQS: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PublishException("Unexpected error publishing message", e);
        }
    }
}
