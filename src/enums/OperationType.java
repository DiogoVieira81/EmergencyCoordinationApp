package enums;

public enum OperationType {
    /**
     * This operation requires approval
     * of a high-level profile, such as a regional coordinator, to be initially
     * created and executed by mid-level profiles
     */
    mass_evacuation,
    /**
     * Activation and maintenance
     * of emergency communication channels must be authorized by
     * medium level fis
     */
    emergency_communications_activation,
    /**
     * Approval for this
     * operation must be done by a low level profile
     */
    emergency_resources_distribution
}
