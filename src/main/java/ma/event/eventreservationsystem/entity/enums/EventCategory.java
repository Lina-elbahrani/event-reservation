package ma.event.eventreservationsystem.entity.enums;

public enum EventCategory {
    CONCERT("Concert"),
    THEATRE("Théâtre"),
    CONFERENCE("Conférence"),
    SPORT("Sport"),
    AUTRE("Autre");

    private final String label;

    EventCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}