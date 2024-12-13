package enums;

public enum OperationType {
    /**
     * This operation requires approval
     * of a high-level profile, such as a regional coordinator, to be initially
     * created and executed by mid-level profiles
     */
    MASS_EVACUATION,
    /**
     * Activation and maintenance
     * of emergency communication channels must be authorized by
     * medium level fis
     */
    EMERGENCY_COMMUNICATIONS_COORDINATION,
    /**
     * Approval for this
     * operation must be done by a low level profile
     */
    EMERGENCY_RESOURCES_DISTRIBUTION;

    public static OperationType toOperationType(String type) {
        switch (type.toUpperCase()) {
            case "MASS_EVACUATION":
                return MASS_EVACUATION;
            case "EMERGENCY_COMMUNICATIONS_COORDINATION":
                return EMERGENCY_COMMUNICATIONS_COORDINATION;
            case "EMERGENCY_RESOURCES_COORDINATION":
                return EMERGENCY_RESOURCES_DISTRIBUTION;
            default:
                System.out.println("Insira um tipo operação válido.");
        }
        return null;
    }
}
