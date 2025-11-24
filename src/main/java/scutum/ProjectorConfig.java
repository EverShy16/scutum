package scutum;

/**
 * Runtime holder for configurable values. All getters/setters are used on the server thread.
 */
public final class ProjectorConfig {
    private ProjectorConfig() {}

    private static volatile int perBreakDamage = 1;

    private static volatile double basicDurabilityMultiplier = 1.23;
    private static volatile double advancedDurabilityMultiplier = 0.85;

    private static volatile double basicEnergyMultiplier = 0.05;
    private static volatile double advancedEnergyMultiplier = 0.05;

    private static volatile int minEnergyCostPerTick = 1;

    private static volatile double explosionDamageScale = 2.0;

    public static int getPerBreakDamage() { return perBreakDamage; }
    public static void setPerBreakDamage(int v) { perBreakDamage = Math.max(0, v); }

    public static double getBasicDurabilityMultiplier() { return basicDurabilityMultiplier; }
    public static void setBasicDurabilityMultiplier(double v) { basicDurabilityMultiplier = Math.max(0.0, v); }

    public static double getAdvancedDurabilityMultiplier() { return advancedDurabilityMultiplier; }
    public static void setAdvancedDurabilityMultiplier(double v) { advancedDurabilityMultiplier = Math.max(0.0, v); }

    public static double getBasicEnergyMultiplier() { return basicEnergyMultiplier; }
    public static void setBasicEnergyMultiplier(double v) { basicEnergyMultiplier = Math.max(0.0, v); }

    public static double getAdvancedEnergyMultiplier() { return advancedEnergyMultiplier; }
    public static void setAdvancedEnergyMultiplier(double v) { advancedEnergyMultiplier = Math.max(0.0, v); }

    public static int getMinEnergyCostPerTick() { return minEnergyCostPerTick; }
    public static void setMinEnergyCostPerTick(int v) { minEnergyCostPerTick = Math.max(0, v); }

    public static double getExplosionDamageScale() { return explosionDamageScale; }
    public static void setExplosionDamageScale(double v) { explosionDamageScale = Math.max(0.0, v); }
}