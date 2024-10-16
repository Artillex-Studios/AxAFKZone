package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.NumberUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.LANG;
import static java.time.temporal.ChronoUnit.SECONDS;

public class UpdateNotifier implements Listener {
    private final int id;
    private final String currentVersion;
    private final AxPlugin instance;
    private String latestVersion = null;
    private boolean isNewest = true;

    public UpdateNotifier(AxPlugin instance, int id) {
        this.id = id;
        this.currentVersion = instance.getDescription().getVersion();
        this.instance = instance;

        instance.getServer().getPluginManager().registerEvents(this, instance);
        scheduleVersionCheck();
    }

    private void scheduleVersionCheck() {
        long time = 30L * 60L * 20L;
        Scheduler.get().runAsyncTimer(t -> {
            latestVersion = fetchLatestVersion();
            isNewest = isLatest(currentVersion);

            if (latestVersion != null && !isNewest) {
                notifyConsole();
                t.cancel();
            }
        }, 0, time);
    }

    private void notifyConsole() {
        Scheduler.get().runLaterAsync(t -> {
            Bukkit.getConsoleSender().sendMessage(getNotificationMessage());
        }, 50L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (latestVersion == null || isNewest || !CONFIG.getBoolean("update-notifier.on-join", true)
                || !event.getPlayer().hasPermission(instance.getName().toLowerCase() + ".update-notify")) {
            return;
        }

        Scheduler.get().runLaterAsync(t -> {
            event.getPlayer().sendMessage(getNotificationMessage());
        }, 50L);
    }

    private String getNotificationMessage() {
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%current%", currentVersion);
        placeholders.put("%latest%", latestVersion);
        return StringUtils.formatToString(CONFIG.getString("prefix") + LANG.getString("update-notifier"), placeholders);
    }

    @Nullable
    private String fetchLatestVersion() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=" + id + "&key=version"))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception ex) {
            return null;
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isLatest(String current) {
        return getVersionWeight(latestVersion) <= getVersionWeight(current);
    }

    private int getVersionWeight(String version) {
        if (version == null) return 0;
        String[] parts = version.split("\\.");
        if (parts.length < 3 || !NumberUtils.isInt(parts[0]) || !NumberUtils.isInt(parts[1]) || !NumberUtils.isInt(parts[2])) {
            return 0;
        }

        return Integer.parseInt(parts[0]) * 1000000 +
                Integer.parseInt(parts[1]) * 1000 +
                Integer.parseInt(parts[2]);
    }
}