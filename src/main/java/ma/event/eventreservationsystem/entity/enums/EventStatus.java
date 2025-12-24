package ma.event.eventreservationsystem.entity.enums;

public enum EventStatus {
    BROUILLON("Brouillon"),
    PUBLIE("Publié"),
    ANNULE("Annulé"),
    TERMINE("Terminé");

    private final String label;

    EventStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}