package scutum.client;

import scutum.ProjectorMod;
import scutum.ProjectorBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class ProjectorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ProjectorMod.FORCEFIELD_BLOCK, RenderLayer.getTranslucent());
        BlockEntityRendererRegistry.register(ProjectorMod.PROJECTOR_BE_TYPE, ProjectorBlockEntityRenderer::new);
        net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry.register(ProjectorMod.PROJECTOR_SCREEN_HANDLER, ProjectorGuiScreen::new);
    }
}