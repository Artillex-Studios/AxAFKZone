package com.artillexstudios.axafkzone.reward;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Reward {
    private final List<String> commands;
    private final List<ItemStack> items;
    private final double chance;
    private final String display;
    private final String permission;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final boolean soundEnabled;
    private final Particle particle;
    private final boolean particleEnabled;
    private final int particleCount;
    private final double particleOffsetX;
    private final double particleOffsetY;
    private final double particleOffsetZ;
    private final boolean broadcastEnabled;

    public Reward(Map<Object, Object> str) {
        final List<String> commands = (List<String>) str.getOrDefault("commands", new ArrayList<>());
        final ArrayList<ItemStack> items = new ArrayList<>();
        Number chance = (Number) str.get("chance");

        var map = (List<Map<Object, Object>>) str.get("items");
        if (map != null) {
            final LinkedList<Map<Object, Object>> map2 = new LinkedList<>(map);
            for (Map<Object, Object> it : map2) {
                items.add(new ItemBuilder(it).get());
            }
        }

        String display = null;
        if (str.containsKey("display")) display = (String) str.get("display");

        Sound sound = null;
        float volume = 1.0f;
        float pitch = 1.0f;
        boolean soundEnabled = false;
        if (str.containsKey("sound")) {
            Object soundConfig = str.get("sound");
            if (soundConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> soundMap = (Map<Object, Object>) soundConfig;

                if (soundMap.containsKey("enabled")) {
                    try {
                        soundEnabled = (Boolean) soundMap.get("enabled");
                    } catch (ClassCastException e) {
                        Bukkit.getLogger().warning("Invalid sound.enabled value: " + soundMap.get("enabled") + ", defaulting to false");
                    }
                }

                if (soundMap.containsKey("type")) {
                    try {
                        sound = Sound.valueOf(((String) soundMap.get("type")).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning("Invalid sound type: " + soundMap.get("type"));
                    }
                }

                if (soundMap.containsKey("volume")) {
                    try {
                        volume = ((Number) soundMap.get("volume")).floatValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid volume value: " + soundMap.get("volume") + ", defaulting to 1.0");
                    }
                }

                if (soundMap.containsKey("pitch")) {
                    try {
                        pitch = ((Number) soundMap.get("pitch")).floatValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid pitch value: " + soundMap.get("pitch") + ", defaulting to 1.0");
                    }
                }
            } else {
                Bukkit.getLogger().warning("Sound configuration is not a map, skipping sound parsing.");
            }
        }
        Particle particle = null;
        boolean particleEnabled = false;
        int particleCount = 10;
        double particleOffsetX = 0.5;
        double particleOffsetY = 0.5;
        double particleOffsetZ = 0.5;

        if (str.containsKey("particle")) {
            Object particleConfig = str.get("particle");
            if (particleConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> particleMap = (Map<Object, Object>) particleConfig;

                if (particleMap.containsKey("enabled")) {
                    try {
                        particleEnabled = (Boolean) particleMap.get("enabled");
                    } catch (ClassCastException e) {
                        Bukkit.getLogger().warning("Invalid particle.enabled value: " + particleMap.get("enabled") + ", defaulting to false");
                    }
                }

                if (particleMap.containsKey("type")) {
                    try {
                        particle = Particle.valueOf(((String) particleMap.get("type")).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning("Invalid particle type: " + particleMap.get("type"));
                    }
                }

                if (particleMap.containsKey("count")) {
                    try {
                        particleCount = ((Number) particleMap.get("count")).intValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid particle count: " + particleMap.get("count") + ", defaulting to 10");
                    }
                }

                if (particleMap.containsKey("offset-x")) {
                    try {
                        particleOffsetX = ((Number) particleMap.get("offset-x")).doubleValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid particle offset-x: " + particleMap.get("offset-x") + ", defaulting to 0.5");
                    }
                }

                if (particleMap.containsKey("offset-y")) {
                    try {
                        particleOffsetY = ((Number) particleMap.get("offset-y")).doubleValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid particle offset-y: " + particleMap.get("offset-y") + ", defaulting to 0.5");
                    }
                }

                if (particleMap.containsKey("offset-z")) {
                    try {
                        particleOffsetZ = ((Number) particleMap.get("offset-z")).doubleValue();
                    } catch (ClassCastException | NullPointerException e) {
                        Bukkit.getLogger().warning("Invalid particle offset-z: " + particleMap.get("offset-z") + ", defaulting to 0.5");
                    }
                }
            } else {
                Bukkit.getLogger().warning("Particle configuration is not a map, skipping particle parsing.");
            }
        }

        this.chance = chance.doubleValue();
        this.items = items;
        this.commands = commands;
        this.display = display;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.soundEnabled = soundEnabled;
        this.particle = particle;
        this.particleEnabled = particleEnabled;
        this.particleCount = particleCount;
        this.particleOffsetX = particleOffsetX;
        this.particleOffsetY = particleOffsetY;
        this.particleOffsetZ = particleOffsetZ;
        this.broadcastEnabled = (Boolean) str.getOrDefault("broadcast", false);
        this.permission = (String) str.getOrDefault("permission", "");
    }


    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public double getChance() {
        return chance;
    }

    public String getDisplay() {
        return display;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public Particle getParticle() {
        return particle;
    }

    public boolean isParticleEnabled() {
        return particleEnabled;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public double getParticleOffsetX() {
        return particleOffsetX;
    }

    public double getParticleOffsetY() {
        return particleOffsetY;
    }

    public double getParticleOffsetZ() {
        return particleOffsetZ;
    }

    public void run(Player player) {
        Scheduler.get().run(scheduledTask -> {
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        });
        ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), items, player.getLocation());
    }

    @Override
    public String toString() {
        return "Reward{" +
                "commands=" + commands +
                ", items=" + items +
                ", chance=" + chance +
                ", display='" + display + '\'' +
                ", permission='" + permission + '\'' +
                ", sound=" + (sound != null ? sound.name() : "") +
                ", volume=" + volume +
                ", pitch=" + pitch +
                ", soundEnabled=" + soundEnabled +
                ", particle=" + (particle != null ? particle.name() : "") +
                ", particleEnabled=" + particleEnabled +
                ", particleCount=" + particleCount +
                ", particleOffsetX=" + particleOffsetX +
                ", particleOffsetY=" + particleOffsetY +
                ", particleOffsetZ=" + particleOffsetZ +
                ", broadcastEnabled=" + broadcastEnabled +
                '}';
    }
}
