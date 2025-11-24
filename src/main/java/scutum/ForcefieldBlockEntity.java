package scutum;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;

public class ForcefieldBlockEntity extends BlockEntity {
    private BlockPos ownerPos = null;

    public ForcefieldBlockEntity(BlockPos pos, BlockState state) { super(ProjectorMod.FORCEFIELD_BE_TYPE, pos, state); }

    public void setOwner(BlockPos owner) { this.ownerPos = owner; markDirty(); }
    public BlockPos getOwner() { return ownerPos; }

    public void notifyOwnerOfExplosion(Explosion explosion) {
        if (world == null || world.isClient) return;
        if (ownerPos == null) return;
        float power = 4.0f;
        try {
            java.lang.reflect.Method m = Explosion.class.getMethod("getRadius");
            Object val = m.invoke(explosion);
            if (val instanceof Number) power = ((Number) val).floatValue();
        } catch (Exception ignored) {}
        int damage = Math.max(1, (int) Math.ceil(power * ProjectorConfig.getExplosionDamageScale()));
        if (world instanceof ServerWorld sw) {
            var be = sw.getBlockEntity(ownerPos);
            if (be instanceof ProjectorBlockEntity pbe) pbe.applyFieldDamage(damage);
        }
    }

    public void notifyOwnerOfBreak() {
        if (world == null || world.isClient) return;
        if (ownerPos == null) return;
        int damage = ProjectorAPI.getPerBreakDamage();
        if (world instanceof ServerWorld sw) {
            var be = sw.getBlockEntity(ownerPos);
            if (be instanceof ProjectorBlockEntity pbe) pbe.applyFieldDamage(damage);
        }
    }

    @Override public void writeNbt(NbtCompound nbt) { super.writeNbt(nbt); if (ownerPos != null) nbt.putLong("OwnerPos", ownerPos.asLong()); }
    @Override public void readNbt(NbtCompound nbt) { super.readNbt(nbt); if (nbt.contains("OwnerPos")) ownerPos = BlockPos.fromLong(nbt.getLong("OwnerPos")); }
}