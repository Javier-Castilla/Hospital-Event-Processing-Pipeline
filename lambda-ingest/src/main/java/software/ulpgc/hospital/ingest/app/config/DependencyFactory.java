package software.ulpgc.hospital.ingest.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.ulpgc.hospital.app.windows.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.app.windows.serialization.JacksonEventSerializer;
import software.ulpgc.hospital.ingest.app.storage.S3EventStorage;
import software.ulpgc.hospital.ingest.domain.processor.EventProcessor;
import software.ulpgc.hospital.ingest.domain.processor.SimpleEventProcessor;
import software.ulpgc.hospital.ingest.domain.storage.EventStorage;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.EventSerializer;

public class DependencyFactory {
    private static DependencyFactory instance;

    private final EventProcessor eventProcessor;

    private DependencyFactory() {
        String bucketName = System.getenv("BUCKET_NAME");
        String region = System.getenv("AWS_REGION");

        EventSerializer serializer = new JacksonEventSerializer();
        EventDeserializer<Event> deserializer = new JacksonEventDeserializer<>(Event.class);

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();

        EventStorage storage = new S3EventStorage(s3Client, bucketName, serializer);

        this.eventProcessor = new SimpleEventProcessor(deserializer, storage);
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }
}
