package software.ulpgc.hospital.mounter.app.repository;

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
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class S3EventReader implements EventReader {
    private final S3Client s3Client;
    private final String bucketName;
    private final EventDeserializer deserializer;

    public S3EventReader(S3Client s3Client, String bucketName, EventDeserializer deserializer) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.deserializer = deserializer;
    }

    @Override
    public Event read(String s3Key) throws RepositoryException {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest);

            String jsonContent = new BufferedReader(
                    new InputStreamReader(response, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return deserializer.deserialize(jsonContent);
        } catch (S3Exception e) {
            throw new RepositoryException("Failed to read event from S3: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RepositoryException("Unexpected error reading from S3", e);
        }
    }
}
