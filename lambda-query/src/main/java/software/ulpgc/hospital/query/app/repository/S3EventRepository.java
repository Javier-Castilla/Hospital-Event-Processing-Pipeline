package software.ulpgc.hospital.query.app.repository;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class S3EventRepository implements EventRepository {
    private final S3Client s3Client;
    private final String bucketName;
    private final EventDeserializer deserializer;

    public S3EventRepository(S3Client s3Client, String bucketName, EventDeserializer deserializer) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.deserializer = deserializer;
    }

    @Override
    public List<Event> query(Map<String, String> filters) throws RepositoryException {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("raw/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<Event> events = new ArrayList<>();

            for (S3Object s3Object : listResponse.contents()) {
                try {
                    Event event = readEventFromS3(s3Object.key());

                    if (matchesFilters(event, filters)) {
                        events.add(event);
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            return events;

        } catch (Exception e) {
            throw new RepositoryException("Failed to query raw events from S3", e);
        }
    }

    private Event readEventFromS3(String key) throws Exception {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getRequest);

        String json = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        return deserializer.deserialize(json);
    }

    private boolean matchesFilters(Event event, Map<String, String> filters) {
        if (filters.containsKey("eventType")) {
            if (!event.getEventType().name().equalsIgnoreCase(filters.get("eventType"))) {
                return false;
            }
        }

        if (filters.containsKey("date")) {
            String eventDate = event.getTimestamp().toLocalDateTime().toLocalDate().toString();
            if (!eventDate.equals(filters.get("date"))) {
                return false;
            }
        }

        if (filters.containsKey("department") && event instanceof AdmissionEvent admission) {
            return admission.department().name().equalsIgnoreCase(filters.get("department"));
        }

        return true;
    }
}
