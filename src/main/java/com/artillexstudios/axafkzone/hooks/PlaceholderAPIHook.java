package com.artillexstudios.axafkzone.hooks;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "axafkzone";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Artillex-Studios";
    }

    @Override
    public @NotNull String getVersion() {
        return AxAFKZone.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";
        Player p = player.getPlayer();
        if (p == null) return "";

        Zone currentZone = null;
        for (Zone zone : Zones.getZones().values()) {
            if (zone.getPlayerTime(p) != null) {
                currentZone = zone;
                break;
            }
        }

        switch (params.toLowerCase()) {
            case "in_zone":
                return currentZone != null ? "true" : "false";

            case "current_zone":
                return currentZone != null ? currentZone.getName() : "";

            case "time_spent":
                if (currentZone != null) {
                    Integer timeSpent = currentZone.getPlayerTime(p);
                    if (timeSpent != null) {
                        return TimeUtils.fancyTime(timeSpent * 1000L);
                    }
                }
                return "0s";

            case "time_left":
                if (currentZone != null) {
                    long timeLeft = currentZone.timeUntilNext(p);
                    if (timeLeft != -1) {
                        return TimeUtils.fancyTime(timeLeft);
                    }
                }
                return "0s";

            case "progress_bar":
                String symbol = AxAFKZone.CONFIG.getString("placeholder-progress-bar.symbol", "■");
                int length = AxAFKZone.CONFIG.getInt("placeholder-progress-bar.length", 10);
                String filledColor = AxAFKZone.CONFIG.getString("placeholder-progress-bar.filled-color", "&#00AAFF");
                String unfilledColor = AxAFKZone.CONFIG.getString("placeholder-progress-bar.unfilled-color", "&7");
                String format = AxAFKZone.CONFIG.getString("placeholder-progress-bar.format", "&a[ {bar} &a]");

                StringBuilder bar = new StringBuilder();

                if (currentZone != null) {
                    Integer timeSpent = currentZone.getPlayerTime(p);
                    int rewardTime = currentZone.getRewardSeconds();
                    
                    if (timeSpent != null && rewardTime > 0) {
                        int currentCycleTime = timeSpent % rewardTime;
                        float progress = (float) currentCycleTime / rewardTime;
                        int filledAmount = Math.max(0, Math.min(length, Math.round(progress * length)));
                        int unfilledAmount = length - filledAmount;
                        
                        bar.append(filledColor);
                        for (int i = 0; i < filledAmount; i++) bar.append(symbol);
                        bar.append(unfilledColor);
                        for (int i = 0; i < unfilledAmount; i++) bar.append(symbol);
                    }
                }

                if (bar.length() == 0) {
                    bar.append(unfilledColor);
                    for (int i = 0; i < length; i++) bar.append(symbol);
                }

                return com.artillexstudios.axapi.utils.StringUtils.formatToString(format.replace("{bar}", bar.toString()));
        }

        return null; // Variable not recognized
    }
}
