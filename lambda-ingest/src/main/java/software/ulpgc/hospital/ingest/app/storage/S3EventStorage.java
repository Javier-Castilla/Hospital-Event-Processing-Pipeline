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
import java.time.ZoneId;

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
        try {
            String eventJson = serializer.serialize(event);
            String key = buildS3Key(event);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(eventJson));

            String location = String.format("s3://%s/%s", bucketName, key);
            return new StorageResult(location, true);

        } catch (S3Exception e) {
            throw new StorageException("Failed to store event in S3: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new StorageException("Unexpected error storing event", e);
        }
    }

    private String buildS3Key(Event event) {
        LocalDate date = event.getTimestamp()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String eventType = event.getClass().getSimpleName().replace("Event", "").toUpperCase();
        String year = String.valueOf(date.getYear());
        String month = String.format("%02d", date.getMonthValue());
        String day = String.format("%02d", date.getDayOfMonth());

        return String.format("raw/%s/%s/%s/%s/%s.json",
                eventType, year, month, day, event.getStreamId());
    }
}
