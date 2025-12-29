package software.ulpgc.hospital.mounter.app.repository;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.mounter.domain.repository.EventReader;
import software.ulpgc.hospital.mounter.domain.repository.RepositoryException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class S3EventReader implements EventReader {

    private final S3Client s3Client;
    private final String bucketName;
    private final EventDeserializer deserializer;
    private LambdaLogger logger;

    public S3EventReader(S3Client s3Client, String bucketName, EventDeserializer deserializer) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.deserializer = deserializer;
    }

    public void setLogger(LambdaLogger logger) {
        this.logger = logger;
    }

    @Override
    public Event read(String s3Key) throws RepositoryException {
        try {
            if (logger != null) {
                logger.log("Reading from S3: bucket=" + bucketName + ", key=" + s3Key);
            }

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest);

            String jsonContent = new BufferedReader(
                    new InputStreamReader(response, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            if (logger != null) {
                logger.log("JSON content read: " + jsonContent);
            }

            Event event = deserializer.deserialize(jsonContent);

            if (logger != null) {
                logger.log("Event deserialized successfully: " + event.getStreamId());
            }

            return event;

        } catch (S3Exception e) {
            String errorMsg = "S3 Exception: " + e.awsErrorDetails().errorMessage();
            if (logger != null) {
                logger.log(errorMsg);
                logger.log(getStackTrace(e));
            }
            throw new RepositoryException("Failed to read event from S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            String errorMsg = "Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage();
            if (logger != null) {
                logger.log(errorMsg);
                logger.log(getStackTrace(e));
            }
            throw new RepositoryException("Unexpected error reading from S3: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
