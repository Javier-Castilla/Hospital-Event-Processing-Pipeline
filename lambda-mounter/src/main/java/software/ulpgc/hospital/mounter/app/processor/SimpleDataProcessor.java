package software.ulpgc.hospital.mounter.app.processor;

import software.ulpgc.hospital.domain.model.AdmissionEvent;
import software.ulpgc.hospital.domain.model.DepartmentStats;
import software.ulpgc.hospital.domain.model.Event;
import software.ulpgc.hospital.mounter.domain.processor.DataProcessor;
import software.ulpgc.hospital.mounter.domain.processor.ProcessException;
import software.ulpgc.hospital.mounter.domain.processor.ProcessResult;
import software.ulpgc.hospital.mounter.domain.repository.EventReader;
import software.ulpgc.hospital.mounter.domain.repository.DatamartRepository;

import java.time.LocalDate;

public class SimpleDataProcessor implements DataProcessor {
    private final EventReader eventReader;
    private final DatamartRepository datamartRepository;

    public SimpleDataProcessor(EventReader eventReader, DatamartRepository datamartRepository) {
        this.eventReader = eventReader;
        this.datamartRepository = datamartRepository;
    }

    @Override
    public ProcessResult process(String s3Key) throws ProcessException {
        try {
            Event event = eventReader.read(s3Key);

            if (event instanceof AdmissionEvent admission) {
                updateDatamart(admission);
                return new ProcessResult(
                        event.getStreamId().toString(),
                        "datamart-updated",
                        true
                );
            }

            return new ProcessResult(
                    event.getStreamId().toString(),
                    "not-aggregated",
                    true
            );
        } catch (Exception e) {
            throw new ProcessException("Failed to process event: " + e.getMessage(), e);
        }
    }

    private void updateDatamart(AdmissionEvent admission) throws ProcessException {
        try {
            String department = admission.admissionDetails().department().toString();
            LocalDate date = admission.timestamp().toLocalDateTime().toLocalDate();

            DepartmentStats current = datamartRepository.getOrCreate(department, date.toString());
            DepartmentStats updated = current.incrementAdmission(admission.admissionDetails().admissionType());
            datamartRepository.save(updated);
        } catch (Exception e) {
            throw new ProcessException("Failed to update datamart", e);
        }
    }
}
