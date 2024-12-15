package enums;

public enum UserRole {
    /**
     * low level personnel
     */
    LOW_LEVEL,
    /**
     * medium level personnel
     */
    MID_LEVEL,
    /**
     * high level personnel
     */
    HIGH_LEVEL,
    /**
     * the admin
     */
    ADMIN;

    public static UserRole toUserRole(String userRole) {
        switch (userRole.toUpperCase()) {
            case "LOW_LEVEL":
                return LOW_LEVEL;
            case "MID_LEVEL":
                return MID_LEVEL;
            case "HIGH_LEVEL":
                return HIGH_LEVEL;
            default:
                System.out.println("Insira um tipo operação válido.");
                break;
        }
        return null;
    }
}
