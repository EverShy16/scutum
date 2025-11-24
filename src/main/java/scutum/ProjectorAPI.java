package scutum;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ProjectorAPI {
    private ProjectorAPI() {}

    public static void applyFieldDamage(BlockPos ownerPos, ServerWorld world, int amount) {
        if (ownerPos == null || world == null || amount <= 0) return;
        var be = world.getBlockEntity(ownerPos);
        if (be instanceof ProjectorBlockEntity pbe) pbe.applyFieldDamage(amount);
    }

    public static ProjectorBlockEntity getProjectorAt(BlockPos ownerPos, ServerWorld world) {
        if (ownerPos == null || world == null) return null;
        var be = world.getBlockEntity(ownerPos);
        if (be instanceof ProjectorBlockEntity pbe) return pbe;
        return null;
    }

    public static boolean tryClaimForcefield(BlockPos claimantPos, BlockPos fieldPos, ServerWorld world) { return false; }

    public static int getPerBreakDamage() { return ProjectorConfig.getPerBreakDamage(); }
    public static void setPerBreakDamage(int v) { ConfigManager.setPerBreakDamage(v); }

    public static double getBasicDurabilityMultiplier() { return ProjectorConfig.getBasicDurabilityMultiplier(); }
    public static void setBasicDurabilityMultiplier(double v) { ConfigManager.setBasicDurabilityMultiplier(v); }

    public static double getAdvancedDurabilityMultiplier() { return ProjectorConfig.getAdvancedDurabilityMultiplier(); }
    public static void setAdvancedDurabilityMultiplier(double v) { ConfigManager.setAdvancedDurabilityMultiplier(v); }

    public static double getBasicEnergyMultiplier() { return ProjectorConfig.getBasicEnergyMultiplier(); }
    public static void setBasicEnergyMultiplier(double v) { ConfigManager.setBasicEnergyMultiplier(v); }

    public static double getAdvancedEnergyMultiplier() { return ProjectorConfig.getAdvancedEnergyMultiplier(); }
    public static void setAdvancedEnergyMultiplier(double v) { ConfigManager.setAdvancedEnergyMultiplier(v); }

    public static int getMinEnergyCostPerTick() { return ProjectorConfig.getMinEnergyCostPerTick(); }
    public static void setMinEnergyCostPerTick(int v) { ConfigManager.setMinEnergyCostPerTick(v); }

    public static double getExplosionDamageScale() { return ProjectorConfig.getExplosionDamageScale(); }
    public static void setExplosionDamageScale(double v) { ConfigManager.setExplosionDamageScale(v); }
}