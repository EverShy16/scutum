package scutum;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.entity.BlockEntity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class ProjectorScreenHandlerTracker {
    private static final Map<ProjectorScreenHandler, ServerWorld> handlers = Collections.synchronizedMap(new IdentityHashMap<>());

    public static void init() { ServerTickEvents.END_SERVER_TICK.register(ProjectorScreenHandlerTracker::tick); }

    public static void register(ProjectorScreenHandler handler, ServerWorld world) { handlers.put(handler, world); }
    public static void unregister(ProjectorScreenHandler handler) { handlers.remove(handler); }

    private static void tick(MinecraftServer server) {
        if (handlers.isEmpty()) return;
        ProjectorScreenHandler[] keys;
        synchronized (handlers) { keys = handlers.keySet().toArray(new ProjectorScreenHandler[0]); }
        for (ProjectorScreenHandler handler : keys) {
            ServerWorld world = handlers.get(handler);
            if (world == null || handler == null) { unregister(handler); continue; }
            try {
                BlockEntity beRaw = world.getBlockEntity(handler.getPos());
                if (!(beRaw instanceof ProjectorBlockEntity)) { unregister(handler); continue; }
                ProjectorBlockEntity be = (ProjectorBlockEntity) beRaw;
                handler.updateFromBE(be);
            } catch (Exception e) { unregister(handler); }
        }
    }
}