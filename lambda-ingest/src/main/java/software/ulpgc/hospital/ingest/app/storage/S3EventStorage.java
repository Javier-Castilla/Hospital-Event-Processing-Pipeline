package software.ulpgc.hospital.ingest.app.storage;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.ulpgc.hospital.ingest.domain.storage.EventStorage;
import software.ulpgc.hospital.ingest.domain.storage.StorageException;
import software.ulpgc.hospital.ingest.domain.storage.StorageResult;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventSerializer;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class S3EventStorage implements EventStorage {
    private final S3Client s3Client;
    private final String bucketName;
    private final EventSerializer serializer;

    public S3EventStorage(S3Client s3Client, String bucketName, EventSerializer serializer) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.serializer = serializer;
    }

    @Override
    public StorageResult store(Event event) throws StorageException {
        String key = buildS3Key(event);
        String location = String.format("s3://%s/%s", bucketName, key);

        try {
            String eventJson = serializer.serialize(event);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .ifNoneMatch("*")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(eventJson));
            return new StorageResult(location, true);

        } catch (S3Exception e) {
            if (e.statusCode() == 412) {
                return new StorageResult(location, false);
            }
            throw new StorageException("Failed to store event in S3: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new StorageException("Unexpected error storing event", e);
        }
    }

    private String buildS3Key(Event event) {
        LocalDate date = event.getTimestamp()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        String eventType = event.getClass().getSimpleName().replace("Event", "").toUpperCase();

        return String.format("raw/eventType=%s/date=%s/%s.json",
                eventType, date, event.getStreamId());
    }
}
