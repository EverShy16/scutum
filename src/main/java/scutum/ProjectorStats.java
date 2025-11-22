package scutum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectorStats {
    public static int estimatePlacedCount(net.minecraft.block.Block block) {
        if (block == ProjectorMod.ADVANCED_PROJECTOR_BLOCK) return estimateAdvancedPlaced();
        return estimateBasicPlaced();
    }

    private static int estimateBasicPlaced() {
        final int primitiveMaxDy = 3;
        final double[] layerRadii = new double[] { 3.0, 3.0, 2.6, 2.1 };
        final double halfThickness = 0.5;
        final int maxXZ = 3;
        Set<Long> placedSet = new HashSet<>();
        for (int dy = 0; dy <= primitiveMaxDy; dy++) {
            double targetR = layerRadii[Math.min(dy, layerRadii.length - 1)];
            for (int dx = -maxXZ; dx <= maxXZ; dx++) for (int dz = -maxXZ; dz <= maxXZ; dz++) {
                double distXZ = Math.sqrt(dx * dx + dz * dz);
                boolean place;
                if (dy == 0) { place = distXZ <= targetR; if (dx == 0 && dz == 0) place = false; }
                else place = Math.abs(distXZ - targetR) <= halfThickness;
                if (!place) continue;
                long key = (((long) dx) << 32) ^ (((long) dz) & 0xffffffffL) ^ (((long) dy) << 48);
                placedSet.add(key);
            }
        }
        return placedSet.size();
    }

    private static int estimateAdvancedPlaced() {
        final int r = 6;
        final double halfThickness = 0.5;
        List<List<int[]>> layerOffsets = new ArrayList<>();
        for (int i=0;i<=r;i++) layerOffsets.add(new ArrayList<>());
        double baseR = r;
        int maxXZBase = (int) Math.ceil(baseR + 0.5);
        for (int dx=-maxXZBase; dx<=maxXZBase; dx++) for (int dz=-maxXZBase; dz<=maxXZBase; dz++) {
            double distXZ = Math.sqrt(dx*dx + dz*dz);
            if (distXZ <= baseR) { if (dx==0 && dz==0) continue; layerOffsets.get(0).add(new int[]{dx,dz}); }
        }
        double targetR1 = Math.sqrt(Math.max(0, r*r - 1*1));
        int maxXZ1 = (int) Math.ceil(targetR1 + 0.5);
        List<int[]> ring1 = new ArrayList<>();
        for (int dx=-maxXZ1; dx<=maxXZ1; dx++) for (int dz=-maxXZ1; dz<=maxXZ1; dz++) {
            double distXZ = Math.sqrt(dx*dx + dz*dz);
            if (Math.abs(distXZ - targetR1) <= halfThickness) ring1.add(new int[]{dx,dz});
        }
        layerOffsets.set(1, ring1);
        List<int[]> dup = new ArrayList<>(); for (int[] o: ring1) dup.add(new int[]{o[0], o[1]}); layerOffsets.set(2, dup);
        for (int dy=3; dy<=r; dy++) {
            double targetR = Math.sqrt(Math.max(0, r*r - dy*dy));
            int maxXZ = (int) Math.ceil(targetR + 0.5);
            List<int[]> ring = new ArrayList<>();
            for (int dx=-maxXZ; dx<=maxXZ; dx++) for (int dz=-maxXZ; dz<=maxXZ; dz++) {
                double distXZ = Math.sqrt(dx*dx + dz*dz);
                if (Math.abs(distXZ - targetR) <= halfThickness) ring.add(new int[]{dx,dz});
            }
            layerOffsets.set(dy, ring);
        }
        for (int dy=r; dy>=1; dy--) {
            List<int[]> cur = layerOffsets.get(dy);
            if (cur.size() == 1) { BlockPos single = cur.get(0); BlockPos prevPos = single.down(); if (world.isAir(prevPos) || world.getBlockState(prevPos).isOf(ProjectorMod.FORCEFIELD_BLOCK)) { world.setBlockState(prevPos, ff.getDefaultState(), 3); if (world.getBlockEntity(prevPos) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center); if (world.getBlockState(single).isOf(ProjectorMod.FORCEFIELD_BLOCK)) world.setBlockState(single, Blocks.AIR.getDefaultState(), 3); fieldBlocks.remove(single); fieldBlocks.add(prevPos); } }
        }
        int placedCount = fieldBlocks.size(); this.fieldMaxHealth = Math.max(1, (int) Math.ceil(placedCount * ADVANCED_TIER_MULTIPLIER)); this.fieldHealth = this.fieldMaxHealth; this.energyCostPerTick = Math.max(1, (int) Math.ceil(placedCount * ADVANCED_ENERGY_MULTIPLIER)); return;
        }
        // basic branch
        {
            final int primitiveMaxDy = 3; final double[] layerRadii = new double[] { 3.0, 3.0, 2.6, 2.1 }; final double thickness = 1.0; final double halfThickness = thickness / 2.0; final int maxXZ = 3;
            Set<Long> placedSet = new HashSet<>();
            for (int dy = 0; dy <= primitiveMaxDy; dy++) {
                double targetR = layerRadii[Math.min(dy, layerRadii.length - 1)];
                for (int dx = -maxXZ; dx <= maxXZ; dx++) for (int dz = -maxXZ; dz <= maxXZ; dz++) {
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    boolean place; if (dy == 0) { place = distXZ <= targetR; if (dx == 0 && dz == 0) place = false; }
                    else place = Math.abs(distXZ - targetR) <= halfThickness; if (!place) continue; BlockPos p = center.add(dx, dy, dz); long key = p.asLong(); if (placedSet.contains(key)) continue; if (world.getBlockState(p).isOf(ProjectorMod.FORCEFIELD_BLOCK)) { BlockEntity existingBe = world.getBlockEntity(p); if (existingBe instanceof ForcefieldBlockEntity ffbe) { BlockPos owner = ffbe.getOwner(); if (owner != null && !owner.equals(center)) continue; else { fieldBlocks.add(p); placedSet.add(key); continue; } } else continue; } if (dy == 0) { world.setBlockState(p, ff.getDefaultState(), 3); if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center); fieldBlocks.add(p); placedSet.add(key); } else { if (world.isAir(p)) { world.setBlockState(p, ff.getDefaultState(), 3); if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center); fieldBlocks.add(p); placedSet.add(key); } }
                }
            }
            int placedCount = fieldBlocks.size(); this.fieldMaxHealth = Math.max(1, (int) Math.ceil(placedCount * BASIC_TIER_MULTIPLIER)); this.fieldHealth = this.fieldMaxHealth; this.energyCostPerTick = Math.max(1, (int) Math.ceil(placedCount * BASIC_ENERGY_MULTIPLIER));
        }
    }
}