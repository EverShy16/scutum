package scutum;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.entity.BlockEntity;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public final class ConfigCommand {
    private ConfigCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("scutum")
            .then(literal("reload-config")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> {
                    ServerCommandSource src = ctx.getSource();
                    MinecraftServer server = src.getServer();
                    server.execute(() -> {
                        ConfigManager.load();
                        applyConfigToActiveProjectors(server);
                        src.sendFeedback(new net.minecraft.text.LiteralText("Scutum config reloaded."), true);
                    });
                    return 1;
                })
            )
        );
    }

    private static void applyConfigToActiveProjectors(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (BlockEntity be : world.blockEntities) {
                if (be instanceof ProjectorBlockEntity pbe) {
                    int placed = pbe.getPlacedCount();
                    boolean isAdvanced = pbe.getCachedState().isOf(ProjectorMod.ADVANCED_PROJECTOR_BLOCK);
                    int oldMax = Math.max(1, pbe.getMaxFieldHealth());
                    int oldCurrent = pbe.getFieldHealth();

                    int newMax = (int) Math.ceil(placed * (isAdvanced ? ProjectorConfig.getAdvancedDurabilityMultiplier() : ProjectorConfig.getBasicDurabilityMultiplier()));
                    newMax = Math.max(1, newMax);

                    int newEnergy = (int) Math.ceil(placed * (isAdvanced ? ProjectorConfig.getAdvancedEnergyMultiplier() : ProjectorConfig.getBasicEnergyMultiplier()));
                    newEnergy = Math.max(ProjectorConfig.getMinEnergyCostPerTick(), newEnergy);

                    int newCurrent = (int) Math.round((double) oldCurrent * (double) newMax / Math.max(1, oldMax));
                    if (newCurrent < 0) newCurrent = 0;
                    if (newCurrent > newMax) newCurrent = newMax;

                    pbe.setMaxFieldHealth(newMax);
                    pbe.setFieldHealth(newCurrent);
                    pbe.setEnergyCostPerTick(newEnergy);

                    pbe.markDirty();
                    world.updateListeners(pbe.getPos(), world.getBlockState(pbe.getPos()), world.getBlockState(pbe.getPos()), 3);
                }
            }
        }
    }
}