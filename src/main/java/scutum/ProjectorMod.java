package scutum;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ProjectorMod implements ModInitializer {
    public static final String MODID = "scutum";

    public static final Block FORCEFIELD_BLOCK = new ForcefieldBlock(AbstractBlock.Settings.of(Material.GLASS)
            .noCollision()
            .strength(-1.0F, 3600000.0F)
            .luminance(7)
    );

    public static final Block BASIC_PROJECTOR_BLOCK = new ProjectorBlock(AbstractBlock.Settings.of(Material.METAL).strength(2.0f));
    public static final Block ADVANCED_PROJECTOR_BLOCK = new ProjectorBlock(AbstractBlock.Settings.of(Material.METAL).strength(2.5f));

    public static BlockEntityType<ProjectorBlockEntity> PROJECTOR_BE_TYPE;
    public static BlockEntityType<ForcefieldBlockEntity> FORCEFIELD_BE_TYPE;
    public static ScreenHandlerType<ProjectorScreenHandler> PROJECTOR_SCREEN_HANDLER;

    public static final Identifier TOGGLE_PREVIEW_PACKET = id("toggle_preview");
    public static final Identifier APPLY_FIELD_PACKET = id("apply_field");

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, id("forcefield"), FORCEFIELD_BLOCK);
        Registry.register(Registry.ITEM, id("forcefield"), new BlockItem(FORCEFIELD_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)));

        Registry.register(Registry.BLOCK, id("basic_projector"), BASIC_PROJECTOR_BLOCK);
        Registry.register(Registry.ITEM, id("basic_projector"), new ProjectorBlockItem(BASIC_PROJECTOR_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE), "tooltip.scutum.basic"));

        Registry.register(Registry.BLOCK, id("advanced_projector"), ADVANCED_PROJECTOR_BLOCK);
        Registry.register(Registry.ITEM, id("advanced_projector"), new ProjectorBlockItem(ADVANCED_PROJECTOR_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE), "tooltip.scutum.advanced"));

        PROJECTOR_BE_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("projector_be"),
                BlockEntityType.Builder.create(ProjectorBlockEntity::new, BASIC_PROJECTOR_BLOCK, ADVANCED_PROJECTOR_BLOCK).build(null));

        FORCEFIELD_BE_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("forcefield_be"),
                BlockEntityType.Builder.create(ForcefieldBlockEntity::new, FORCEFIELD_BLOCK).build(null));

        PROJECTOR_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, id("projector_screen"),
                new ScreenHandlerType<>(ProjectorScreenHandler::new));

        // Init tracker for GUI sync
        ProjectorScreenHandlerTracker.init();

        // Networking receivers
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_PREVIEW_PACKET, (server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            boolean preview = buf.readBoolean();
            server.execute(() -> {
                var be = player.world.getBlockEntity(pos);
                if (be instanceof ProjectorBlockEntity pbe) pbe.setPreview(preview);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(APPLY_FIELD_PACKET, (server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            server.execute(() -> {
                var be = player.world.getBlockEntity(pos);
                if (be instanceof ProjectorBlockEntity pbe && player.world instanceof net.minecraft.server.world.ServerWorld sw && player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                    pbe.applyFieldFromGui(sw, sp);
                }
            });
        });
    }

    public static Identifier id(String path) { return new Identifier(MODID, path); }
}