package com.mayor.donationalerts.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("donationalerts.json");

    public boolean enabled = true;
    public boolean animations = true;
    public boolean sounds = true;
    public boolean screenShake = true;
    public boolean particles = true;
    public boolean showSkins = true;
    public boolean detectionEnabled = true;
    public float volume = 0.85f;
    public int durationSeconds = 9;
    public long minimumDonation = 1_000_000L;
    public String position = "TOP_CENTER";
    // Capture groups: 1=username, 2=number, 3=K/M/B/T suffix. Keep anchored to avoid outgoing-payment matches.
    public String parserRegex = "(?i)^\\s*([A-Za-z0-9_]{1,16})\\s+(?:has\\s+)?paid you\\s+\\$?([0-9][0-9,.]*)\\s*([KMBT]?)\\s*[.!]?\\s*$";
    public boolean[] tierEnabled = {true, true, true, true, true, true};

    public static ModConfig load() {
        try {
            if (Files.exists(FILE)) {
                ModConfig loaded = GSON.fromJson(Files.readString(FILE), ModConfig.class);
                if (loaded != null) {
                    if (loaded.tierEnabled == null || loaded.tierEnabled.length != 6) loaded.tierEnabled = new boolean[]{true,true,true,true,true,true};
                    return loaded;
                }
            }
        } catch (Exception ignored) { }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            System.err.println("[DonationAlerts] Could not save config: " + e.getMessage());
        }
    }
}
