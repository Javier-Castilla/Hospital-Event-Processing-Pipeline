package software.ulpgc.hospital.feeder.app.publisher;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.domain.publisher.PublishException;
import software.ulpgc.hospital.feeder.domain.publisher.PublishResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SQSMessagePublisher implements MessagePublisher {
    private final SqsClient sqsClient;
    private final String queueUrl;

    public SQSMessagePublisher(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public PublishResult publish(String message, Map<String, String> attributes) throws PublishException {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .messageAttributes(buildMessageAttributesFrom(attributes))
                    .build();
            SendMessageResponse response = sqsClient.sendMessage(request);
            return new PublishResult(response.messageId(), true);
        } catch (SqsException e) {
            throw new PublishException("Failed to publish message to SQS: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PublishException("Unexpected error publishing message", e);
        }
    }

    private Map<String, MessageAttributeValue> buildMessageAttributesFrom(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) return Collections.emptyMap();
        return attributes.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(e.getValue())
                                .build()
                ));
    }
}
