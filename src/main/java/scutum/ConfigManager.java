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
        public double basicDurabilityMultiplier = 1.23;
        public double advancedDurabilityMultiplier = 0.85;
        public double basicEnergyMultiplier = 0.05;
        public double advancedEnergyMultiplier = 0.05;
        public int minEnergyCostPerTick = 1;
        public double explosionDamageScale = 2.0;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data config = new Data();

    public static Path getConfigPath() { return FabricLoader.getInstance().getConfigDir().resolve("scutum_config.json"); }
    public static Data getConfig() { return config; }

    public static void load() {
        Path cfg = getConfigPath();
        try {
            if (Files.exists(cfg)) {
                String txt = Files.readString(cfg, StandardCharsets.UTF_8);
                Data d = GSON.fromJson(txt, Data.class);
                if (d != null) config = d;
            } else {
                save();
            }
        } catch (IOException e) {
            System.err.println("[Scutum] Failed to read config: " + e.getMessage());
        }

        // Apply to runtime
        ProjectorConfig.setPerBreakDamage(config.perBreakDamage);
        ProjectorConfig.setBasicDurabilityMultiplier(config.basicDurabilityMultiplier);
        ProjectorConfig.setAdvancedDurabilityMultiplier(config.advancedDurabilityMultiplier);
        ProjectorConfig.setBasicEnergyMultiplier(config.basicEnergyMultiplier);
        ProjectorConfig.setAdvancedEnergyMultiplier(config.advancedEnergyMultiplier);
        ProjectorConfig.setMinEnergyCostPerTick(config.minEnergyCostPerTick);
        ProjectorConfig.setExplosionDamageScale(config.explosionDamageScale);
    }

    public static void save() {
        Path cfg = getConfigPath();
        try {
            if (cfg.getParent() != null && !Files.exists(cfg.getParent())) Files.createDirectories(cfg.getParent());
            String out = GSON.toJson(config);
            Files.writeString(cfg, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[Scutum] Failed to write config: " + e.getMessage());
        }
    }

    public static void setPerBreakDamage(int v) { config.perBreakDamage = Math.max(0, v); ProjectorConfig.setPerBreakDamage(config.perBreakDamage); save(); }
    public static void setBasicDurabilityMultiplier(double v) { config.basicDurabilityMultiplier = Math.max(0.0, v); ProjectorConfig.setBasicDurabilityMultiplier(config.basicDurabilityMultiplier); save(); }
    public static void setAdvancedDurabilityMultiplier(double v) { config.advancedDurabilityMultiplier = Math.max(0.0, v); ProjectorConfig.setAdvancedDurabilityMultiplier(config.advancedDurabilityMultiplier); save(); }
    public static void setBasicEnergyMultiplier(double v) { config.basicEnergyMultiplier = Math.max(0.0, v); ProjectorConfig.setBasicEnergyMultiplier(config.basicEnergyMultiplier); save(); }
    public static void setAdvancedEnergyMultiplier(double v) { config.advancedEnergyMultiplier = Math.max(0.0, v); ProjectorConfig.setAdvancedEnergyMultiplier(config.advancedEnergyMultiplier); save(); }
    public static void setMinEnergyCostPerTick(int v) { config.minEnergyCostPerTick = Math.max(0, v); ProjectorConfig.setMinEnergyCostPerTick(config.minEnergyCostPerTick); save(); }
    public static void setExplosionDamageScale(double v) { config.explosionDamageScale = Math.max(0.0, v); ProjectorConfig.setExplosionDamageScale(config.explosionDamageScale); save(); }
}