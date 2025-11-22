package scutum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private ConfigManager() {}

    public static class Data {
        public int perBreakDamage = 1;
        // future config fields can be added here
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data config = new Data();

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("scutum_config.json");
    }

    public static Data getConfig() {
        return config;
    }

    public static void load() {
        Path cfg = getConfigPath();
        try {
            if (Files.exists(cfg)) {
                String txt = Files.readString(cfg, StandardCharsets.UTF_8);
                Data d = GSON.fromJson(txt, Data.class);
                if (d != null) config = d;
            } else {
                // create default config file
                save();
            }
        } catch (IOException e) {
            System.err.println("[Scutum] Failed to read config: " + e.getMessage());
            // keep defaults
        }

        // apply loaded values to runtime config holder
        ProjectorConfig.setPerBreakDamage(config.perBreakDamage);
    }

    public static void save() {
        Path cfg = getConfigPath();
        try {
            if (!Files.exists(cfg.getParent())) Files.createDirectories(cfg.getParent());
            String out = GSON.toJson(config);
            Files.writeString(cfg, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[Scutum] Failed to write config: " + e.getMessage());
        }
    }

    public static void setPerBreakDamage(int v) {
        config.perBreakDamage = Math.max(0, v);
        ProjectorConfig.setPerBreakDamage(config.perBreakDamage);
        save();
    }
}