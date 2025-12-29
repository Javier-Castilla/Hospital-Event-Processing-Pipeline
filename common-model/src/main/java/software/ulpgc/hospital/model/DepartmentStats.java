package software.ulpgc.hospital.model;

import java.time.LocalDate;

public record DepartmentStats(
    String department,
    LocalDate date,
    int totalAdmissions,
    int emergencyAdmissions,
    int scheduledAdmissions,
    int transferAdmissions
) {
    public DepartmentStats incrementAdmission(AdmissionEvent.AdmissionType type) {
        return switch (type) {
            case EMERGENCY -> new DepartmentStats(department, date, totalAdmissions + 1, 
                emergencyAdmissions + 1, scheduledAdmissions, transferAdmissions);
            case SCHEDULED -> new DepartmentStats(department, date, totalAdmissions + 1, 
                emergencyAdmissions, scheduledAdmissions + 1, transferAdmissions);
            case TRANSFER -> new DepartmentStats(department, date, totalAdmissions + 1, 
                emergencyAdmissions, scheduledAdmissions, transferAdmissions + 1);
        };
    }
    
    public static DepartmentStats empty(String department, LocalDate date) {
        return new DepartmentStats(department, date, 0, 0, 0, 0);
    }
    
    public String getPartitionKey() {
        return "DEPT#" + department + "#" + date.toString();
    }
}
