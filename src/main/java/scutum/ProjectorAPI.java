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

    public static boolean tryClaimForcefield(BlockPos claimantPos, BlockPos fieldPos, ServerWorld world) {
        // Not implemented: placeholder for takeover logic
        return false;
    }

    public static int getPerBreakDamage() { return ProjectorConfig.getPerBreakDamage(); }
    public static void setPerBreakDamage(int v) { ProjectorConfig.setPerBreakDamage(v); }
}