package scutum.client;

import scutum.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {
    public ProjectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) { }

    @Override
    public void render(ProjectorBlockEntity be, float tickDelta, net.minecraft.client.util.math.MatrixStack ms, net.minecraft.client.render.VertexConsumerProvider vcp, int light, int overlay) {
        if (!be.isPreview()) return;
        if (!(be.getWorld() instanceof ClientWorld world)) return;
        int r = be.getCachedState().isOf(scutum.ProjectorMod.ADVANCED_PROJECTOR_BLOCK) ? 6 : 3;
        BlockPos center = be.getPos();
        for (int dx=-r; dx<=r; dx++) for (int dy=0; dy<=r; dy++) for (int dz=-r; dz<=r; dz++) {
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (Math.abs(dist - r) <= 0.75) { double px = center.getX() + dx + 0.5; double py = center.getY() + dy + 0.5; double pz = center.getZ() + dz + 0.5; if (world.random.nextFloat() < 0.06f) world.addImportantParticle(ParticleTypes.END_ROD, true, px, py, pz, 0.0, 0.0, 0.0); }
        }
    }
}