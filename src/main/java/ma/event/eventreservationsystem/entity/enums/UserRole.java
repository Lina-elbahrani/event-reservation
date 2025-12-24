package ma.event.eventreservationsystem.entity.enums;

public enum UserRole {
    ADMIN("Administrateur"),
    ORGANIZER("Organisateur"),
    CLIENT("Client");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
