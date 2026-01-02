package software.ulpgc.hospital.feeder.app.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventDeserializer;
import software.ulpgc.hospital.app.stage.repository.DynamoDBEventCreationStatusRepository;
import software.ulpgc.hospital.app.stage.repository.EventCreationStatusItemMapper;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.feeder.domain.validation.admissionRules.AdmissionBedNumberNotEmptyRule;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.app.publisher.SQSMessagePublisher;
import software.ulpgc.hospital.feeder.domain.validation.EventValidation;
import software.ulpgc.hospital.feeder.domain.validation.admissionRules.AdmissionDepartmentNotNullRule;
import software.ulpgc.hospital.feeder.domain.validation.admissionRules.AdmissionDetailsNotNullRule;
import software.ulpgc.hospital.feeder.domain.validation.admissionRules.AdmissionPatientNotNullRule;
import software.ulpgc.hospital.feeder.domain.validation.consultationRules.ConsultationDetailsNotNullRule;
import software.ulpgc.hospital.feeder.domain.validation.consultationRules.ConsultationDoctorNotNullRule;
import software.ulpgc.hospital.feeder.domain.validation.consultationRules.ConsultationDurationPositiveRule;
import software.ulpgc.hospital.domain.model.AdmissionEvent;
import software.ulpgc.hospital.domain.model.ConsultationEvent;
import software.ulpgc.hospital.domain.model.EventType;
import software.ulpgc.hospital.domain.model.serialization.EventDeserializer;
import software.ulpgc.hospital.domain.model.serialization.EventSerializer;
import software.ulpgc.hospital.app.implementation.serialization.JacksonEventSerializer;

public class DependencyFactory {
    private static DependencyFactory instance;

    private final EventDeserializer eventDeserializer;
    private final ValidationFactory validationFactory;
    private final EventSerializer eventSerializer;
    private final MessagePublisher messagePublisher;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    private DependencyFactory() {
        String queueUrl = System.getenv("QUEUE_URL");
        String region = System.getenv("AWS_REGION");
        String statusTableName = System.getenv("STATUS_TABLE_NAME");

        this.eventDeserializer = new JacksonEventDeserializer();
        this.validationFactory = ValidationFactory.create()
                .register(EventType.ADMISSION, EventValidation.of(AdmissionEvent.class)
                    .next(AdmissionBedNumberNotEmptyRule.create())
                    .next(AdmissionDepartmentNotNullRule.create())
                    .next(AdmissionDetailsNotNullRule.create())
                    .next(AdmissionPatientNotNullRule.create())
                )
                .register(EventType.CONSULTATION, EventValidation.of(ConsultationEvent.class)
                    .next(ConsultationDoctorNotNullRule.create())
                    .next(ConsultationDetailsNotNullRule.create())
                    .next(ConsultationDurationPositiveRule.create())
                );
        this.eventSerializer = new JacksonEventSerializer();

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();

        this.messagePublisher = new SQSMessagePublisher(sqsClient, queueUrl);
        this.eventCreationStatusRepository = new DynamoDBEventCreationStatusRepository(dynamoDbClient, statusTableName, new EventCreationStatusItemMapper());
    }

    public static synchronized DependencyFactory getInstance() {
        if (instance == null) {
            instance = new DependencyFactory();
        }
        return instance;
    }

    public EventDeserializer getEventDeserializer() {
        return eventDeserializer;
    }

    public ValidationFactory getValidationFactory() {
        return validationFactory;
    }

    public EventSerializer getEventSerializer() {
        return eventSerializer;
    }

    public MessagePublisher getMessagePublisher() {
        return messagePublisher;
    }

    public EventCreationStatusRepository getEventCreationStatusRepository() {
        return eventCreationStatusRepository;
    }
}
