package scutum;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.tag.TagKey;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectorBlockEntity extends BlockEntity {
    public static final int MAX_ENERGY = 20000;

    private boolean active = false;
    private final List<BlockPos> fieldBlocks = new ArrayList<>();
    private int energy = 0;

    public static final int BASIC_RADIUS = 3;
    public static final int ADVANCED_RADIUS = 6;

    private boolean preview = false;
    private int fieldHealth = 0;
    private int fieldMaxHealth = 0;

    // energy cost per tick computed at field creation
    private int energyCostPerTick = 0;

    private static final TagKey<Block> GLASS_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier("scutum", "glass"));

    public ProjectorBlockEntity(BlockPos pos, BlockState state) { super(ProjectorMod.PROJECTOR_BE_TYPE, pos, state); }

    private boolean isAdvanced() { return this.getCachedState().isOf(ProjectorMod.ADVANCED_PROJECTOR_BLOCK); }
    private int getRadius() { return isAdvanced() ? ADVANCED_RADIUS : BASIC_RADIUS; }

    public void toggle(ServerWorld world) {
        if (active) { removeField(world); active = false; markDirty(); }
        else { if (energy > 0) { createField(world); active = true; markDirty(); } }
    }

    public void onBroken() { if (this.world instanceof ServerWorld sw && active) removeField(sw); }

    public void applyFieldFromGui(ServerWorld world, ServerPlayerEntity player) {
        if (!active && energy > 0) { createField(world); active = true; world.setBlockState(pos, world.getBlockState(pos).with(ProjectorBlock.LIT, true), 3); markDirty(); }
        setPreview(false);
    }

    public void setPreview(boolean on) { this.preview = on; if (world instanceof ServerWorld sw) { markDirty(); sw.getChunkManager().markForUpdate(pos); } }
    public boolean isPreview() { return preview; }

    private void createField(ServerWorld world) {
        fieldBlocks.clear();
        BlockPos center = this.pos;
        Block ff = ProjectorMod.FORCEFIELD_BLOCK;

        if (isAdvanced()) {
            final int r = getRadius();
            final double thickness = 1.0;
            final double halfThickness = thickness / 2.0;

            List<List<int[]>> layerOffsets = new ArrayList<>(r + 1);
            for (int i = 0; i <= r; i++) layerOffsets.add(new ArrayList<>());

            double baseR = r;
            int maxXZBase = (int) Math.ceil(baseR + 0.5);
            for (int dx = -maxXZBase; dx <= maxXZBase; dx++) for (int dz = -maxXZBase; dz <= maxXZBase; dz++) {
                double distXZ = Math.sqrt(dx * dx + dz * dz);
                if (distXZ <= baseR) { if (dx == 0 && dz == 0) continue; layerOffsets.get(0).add(new int[]{dx, dz}); }
            }

            double targetR1 = Math.sqrt(Math.max(0, r * r - 1 * 1));
            int maxXZ1 = (int) Math.ceil(targetR1 + 0.5);
            List<int[]> ring1 = new ArrayList<>();
            for (int dx = -maxXZ1; dx <= maxXZ1; dx++) for (int dz = -maxXZ1; dz <= maxXZ1; dz++) {
                double distXZ = Math.sqrt(dx * dx + dz * dz);
                if (Math.abs(distXZ - targetR1) <= halfThickness) ring1.add(new int[]{dx, dz});
            }
            layerOffsets.set(1, ring1);
            layerOffsets.set(2, new ArrayList<>());
            for (int[] o : ring1) layerOffsets.get(2).add(new int[]{o[0], o[1]});

            for (int dy = 3; dy <= r; dy++) {
                double targetR = Math.sqrt(Math.max(0, r * r - dy * dy));
                int maxXZ = (int) Math.ceil(targetR + 0.5);
                List<int[]> ring = new ArrayList<>();
                for (int dx = -maxXZ; dx <= maxXZ; dx++) for (int dz = -maxXZ; dz <= maxXZ; dz++) {
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    if (Math.abs(distXZ - targetR) <= halfThickness) ring.add(new int[]{dx, dz});
                }
                layerOffsets.set(dy, ring);
            }

            for (int dy = r; dy >= 1; dy--) {
                List<int[]> current = layerOffsets.get(dy);
                if (current.size() == 1) {
                    List<int[]> prev = layerOffsets.get(Math.max(0, dy - 1));
                    prev.add(current.get(0));
                    current.clear();
                }
            }

            Set<Long> placedSet = new HashSet<>();
            for (int dy = 0; dy <= r; dy++) {
                List<int[]> offsets = layerOffsets.get(dy);
                for (int[] off : offsets) {
                    int dx = off[0], dz = off[1];
                    BlockPos p = center.add(dx, dy, dz);
                    long key = p.asLong();
                    if (placedSet.contains(key)) continue;

                    if (world.getBlockState(p).isOf(ProjectorMod.FORCEFIELD_BLOCK)) {
                        BlockEntity existingBe = world.getBlockEntity(p);
                        if (existingBe instanceof ForcefieldBlockEntity ffbe) {
                            BlockPos owner = ffbe.getOwner();
                            if (owner != null && !owner.equals(center)) continue;
                            else { fieldBlocks.add(p); placedSet.add(key); continue; }
                        } else continue;
                    }

                    if (dy == 0) {
                        world.setBlockState(p, ff.getDefaultState(), 3);
                        if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center);
                        fieldBlocks.add(p);
                        placedSet.add(key);
                    } else {
                        if (world.isAir(p)) {
                            world.setBlockState(p, ff.getDefaultState(), 3);
                            if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center);
                            fieldBlocks.add(p);
                            placedSet.add(key);
                        }
                    }
                }
            }

            // defensive singleton merge after actual placement
            List<List<BlockPos>> placedPerLayer = new ArrayList<>(); for (int i = 0; i <= r; i++) placedPerLayer.add(new ArrayList<>());
            for (BlockPos p : fieldBlocks) placedPerLayer.get(p.getY() - center.getY()).add(p);
            for (int dy = r; dy >= 1; dy--) {
                List<BlockPos> cur = placedPerLayer.get(dy);
                if (cur.size() == 1) {
                    BlockPos single = cur.get(0);
                    BlockPos prevPos = single.down();
                    if (world.isAir(prevPos) || world.getBlockState(prevPos).isOf(ProjectorMod.FORCEFIELD_BLOCK)) {
                        world.setBlockState(prevPos, ff.getDefaultState(), 3);
                        if (world.getBlockEntity(prevPos) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center);
                        if (world.getBlockState(single).isOf(ProjectorMod.FORCEFIELD_BLOCK)) {
                            world.setBlockState(single, Blocks.AIR.getDefaultState(), 3);
                        }
                        fieldBlocks.remove(single);
                        fieldBlocks.add(prevPos);
                    }
                }
            }

            int placedCount = fieldBlocks.size();
            int computedMaxHP = (int) Math.ceil(placedCount * ProjectorConfig.getAdvancedDurabilityMultiplier());
            this.fieldMaxHealth = Math.max(1, computedMaxHP);
            this.fieldHealth = this.fieldMaxHealth;
            this.energyCostPerTick = Math.max(ProjectorConfig.getMinEnergyCostPerTick(), (int) Math.ceil(placedCount * ProjectorConfig.getAdvancedEnergyMultiplier()));
            return;
        }

        // basic branch
        final int primitiveMaxDy = 3;
        final double[] layerRadii = new double[] { 3.0, 3.0, 2.6, 2.1 };
        final double thickness = 1.0;
        final double halfThickness = thickness / 2.0;
        final int maxXZ = 3;

        for (int dy = 0; dy <= primitiveMaxDy; dy++) {
            double targetR = layerRadii[Math.min(dy, layerRadii.length - 1)];
            for (int dx = -maxXZ; dx <= maxXZ; dx++) {
                for (int dz = -maxXZ; dz <= maxXZ; dz++) {
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    boolean place;
                    if (dy == 0) {
                        place = distXZ <= targetR;
                        if (dx == 0 && dz == 0) place = false;
                    } else {
                        place = Math.abs(distXZ - targetR) <= halfThickness;
                    }
                    if (!place) continue;
                    BlockPos p = center.add(dx, dy, dz);
                    long key = p.asLong();
                    if (world.getBlockState(p).isOf(ProjectorMod.FORCEFIELD_BLOCK)) {
                        BlockEntity existingBe = world.getBlockEntity(p);
                        if (existingBe instanceof ForcefieldBlockEntity ffbe) {
                            BlockPos owner = ffbe.getOwner();
                            if (owner != null && !owner.equals(center)) continue; else { fieldBlocks.add(p); continue; }
                        } else continue;
                    }
                    if (dy == 0) {
                        world.setBlockState(p, ff.getDefaultState(), 3);
                        if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center);
                        fieldBlocks.add(p);
                    } else {
                        if (world.isAir(p)) {
                            world.setBlockState(p, ff.getDefaultState(), 3);
                            if (world.getBlockEntity(p) instanceof ForcefieldBlockEntity ffbe) ffbe.setOwner(center);
                            fieldBlocks.add(p);
                        }
                    }
                }
            }
        }

        int placedCount = fieldBlocks.size();
        int computedMaxHP = (int) Math.ceil(placedCount * ProjectorConfig.getBasicDurabilityMultiplier());
        this.fieldMaxHealth = Math.max(1, computedMaxHP);
        this.fieldHealth = this.fieldMaxHealth;
        this.energyCostPerTick = Math.max(ProjectorConfig.getMinEnergyCostPerTick(), (int) Math.ceil(placedCount * ProjectorConfig.getBasicEnergyMultiplier()));
    }

    private boolean isForcefieldBlock(World world, BlockPos p) { return world.getBlockState(p).isOf(ProjectorMod.FORCEFIELD_BLOCK); }

    private void removeField(ServerWorld world) {
        for (BlockPos p : new ArrayList<>(fieldBlocks)) {
            if (world.getBlockState(p).isAir()) continue;
            if (isForcefieldBlock(world, p)) {
                BlockEntity be = world.getBlockEntity(p);
                if (be instanceof ForcefieldBlockEntity ffbe) {
                    BlockPos owner = ffbe.getOwner(); if (owner != null && owner.equals(this.pos)) world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
        fieldBlocks.clear();
    }

    public int receiveEnergyFromAdjacent() {
        if (world == null || world.isClient) return 0; int received = 0; for (var dir : net.minecraft.util.math.Direction.values()) { var npos = pos.offset(dir); var neighbor = world.getBlockEntity(npos); if (neighbor == null) continue; try { Method extractEnergy = neighbor.getClass().getMethod("extractEnergy", int.class, boolean.class); Object out = extractEnergy.invoke(neighbor, Math.min(1000, MAX_ENERGY - energy), false); if (out instanceof Integer) { int amt = (Integer) out; energy = Math.min(MAX_ENERGY, energy + amt); received += amt; } } catch (NoSuchMethodException ignore) {} catch (Exception ignored) {} } return received; }

    public void serverTick(ServerWorld world) {
        if (energy < MAX_ENERGY) receiveEnergyFromAdjacent(); if (active) { int cost = energyCostPerTick > 0 ? energyCostPerTick : ProjectorConfig.getMinEnergyCostPerTick(); if (energy >= cost) { energy -= cost; applyFieldEffects(world); markDirty(); } else { removeField(world); active = false; world.setBlockState(pos, getCachedState().with(ProjectorBlock.LIT, false), 3); markDirty(); } }
    }

    private void applyFieldEffects(ServerWorld world) {
        int r = getRadius(); Box box = new Box(pos.add(-r, 0, -r), pos.add(r + 1, r + 1, r + 1)); List<Entity> entities = world.getOtherEntities(null, box, e -> true);
        for (Entity e : entities) {
            double dx = e.getX() - (pos.getX() + 0.5);
            double dy = e.getY() - (pos.getY() + 0.5);
            double dz = e.getZ() - (pos.getZ() + 0.5);
            if (dy < 0) continue; double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= r + 0.9 && e instanceof HostileEntity) e.damage(DamageSource.MAGIC, 2.0F);
            if (e instanceof PersistentProjectileEntity) { e.remove(); continue; }
        }
    }

    public void applyFieldDamage(int amount) {
        if (amount <= 0) return; fieldHealth -= amount; if (fieldHealth <= 0) {
            if (this.world instanceof ServerWorld sw) {
                BlockState oldState = sw.getBlockState(pos);
                removeField(sw);
                active = false;
                if (sw.getBlockState(pos).isOf(ProjectorMod.BASIC_PROJECTOR_BLOCK) || sw.getBlockState(pos).isOf(ProjectorMod.ADVANCED_PROJECTOR_BLOCK)) sw.setBlockState(pos, getCachedState().with(ProjectorBlock.LIT, false), 3);
                sw.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                sw.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_ITEM_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                try { int rawId = net.minecraft.block.Block.getRawIdFromState(oldState); sw.syncWorldEvent(2001, pos, rawId); } catch (Exception ignored) {}
            }
        }
        markDirty();
    }

    @Override public void writeNbt(NbtCompound nbt) { super.writeNbt(nbt); nbt.putBoolean("Active", active); nbt.putInt("Energy", energy); nbt.putInt("FieldHealth", fieldHealth); nbt.putInt("FieldMaxHealth", fieldMaxHealth); nbt.putInt("EnergyCostPerTick", energyCostPerTick); nbt.putBoolean("Preview", preview); NbtList list = new NbtList(); for (BlockPos p : fieldBlocks) list.add(NbtLong.of(p.asLong())); nbt.put("FieldPositions", list); }
    @Override public void readNbt(NbtCompound nbt) { super.readNbt(nbt); active = nbt.getBoolean("Active"); energy = nbt.getInt("Energy"); fieldHealth = nbt.getInt("FieldHealth"); fieldMaxHealth = nbt.contains("FieldMaxHealth") ? nbt.getInt("FieldMaxHealth") : fieldHealth; energyCostPerTick = nbt.contains("EnergyCostPerTick") ? nbt.getInt("EnergyCostPerTick") : 0; preview = nbt.getBoolean("Preview"); fieldBlocks.clear(); if (nbt.contains("FieldPositions")) { NbtList list = nbt.getList("FieldPositions", 4); for (int i = 0; i < list.size(); i++) { long longPos = list.getLong(i); fieldBlocks.add(BlockPos.fromLong(longPos)); } } }

    // Accessors used by GUI/commands
    public int getEnergy() { return energy; }
    public int getFieldHealth() { return fieldHealth; }
    public int getPlacedCount() { return fieldBlocks.size(); }
    public int getEnergyCostPerTick() { return energyCostPerTick; }
    public int getMaxFieldHealth() { return fieldMaxHealth; }

    public void setMaxFieldHealth(int v) { this.fieldMaxHealth = Math.max(0, v); }
    public void setFieldHealth(int v) { this.fieldHealth = Math.max(0, Math.min(this.fieldMaxHealth, v)); }
    public void setEnergyCostPerTick(int v) { this.energyCostPerTick = Math.max(1, v); }
}