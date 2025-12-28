package software.ulpgc.hospital.model;

public enum Department {
    EMERGENCY("Emergency"),
    CARDIOLOGY("Cardiology"),
    TRAUMATOLOGY("Traumatology"),
    NEUROLOGY("Neurology"),
    PEDIATRICS("Pediatrics"),
    ICU("Intensive Care Unit"),
    ONCOLOGY("Oncology"),
    GYNECOLOGY("Gynecology"),
    GENERAL_SURGERY("General Surgery"),
    INTERNAL_MEDICINE("Internal Medicine"),
    RADIOLOGY("Radiology"),
    LABORATORY("Laboratory");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
