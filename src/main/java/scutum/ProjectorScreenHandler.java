package scutum;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.entity.BlockEntity;

public class ProjectorScreenHandler extends ScreenHandler {
    private final BlockPos pos;
    private final PropertyDelegate propertyDelegate;

    public ProjectorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(ProjectorMod.PROJECTOR_SCREEN_HANDLER, syncId);
        this.pos = pos;
        this.propertyDelegate = new PropertyDelegate() {
            private int[] v = new int[5];
            @Override public int get(int index) { return v[index]; }
            @Override public void set(int index, int value) { v[index] = value; }
            @Override public int size() { return v.length; }
        };
        this.addProperties(this.propertyDelegate);
        if (playerInventory.player != null && playerInventory.player.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) playerInventory.player.getWorld();
            ProjectorScreenHandlerTracker.register(this, serverWorld);
        }
    }

    public BlockPos getPos() { return pos; }
    public PropertyDelegate getPropertyDelegate() { return propertyDelegate; }

    @Override public boolean canUse(PlayerEntity player) { return true; }

    public void updateFromBE(ProjectorBlockEntity be) {
        int energyPercent = 0;
        if (be.getEnergy() > 0) energyPercent = Math.min(100, (int)((be.getEnergy()*100L)/Math.max(1, ProjectorBlockEntity.MAX_ENERGY)));
        int healthPercent = 0; if (be.getMaxFieldHealth() > 0) healthPercent = Math.min(100, (int)((be.getFieldHealth()*100L)/be.getMaxFieldHealth()));
        int energyCost = be.getEnergyCostPerTick();
        int rawHP = be.getFieldHealth();
        int maxHP = be.getMaxFieldHealth();
        propertyDelegate.set(0, energyPercent);
        propertyDelegate.set(1, healthPercent);
        propertyDelegate.set(2, energyCost);
        propertyDelegate.set(3, rawHP);
        propertyDelegate.set(4, maxHP);
    }

    @Override
    public void onClosed(PlayerEntity player) { super.onClosed(player); ProjectorScreenHandlerTracker.unregister(this); }
}