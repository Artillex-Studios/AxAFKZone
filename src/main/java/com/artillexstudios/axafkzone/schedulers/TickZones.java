package com.artillexstudios.axafkzone.schedulers;

import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickZones {
    private static ScheduledExecutorService service;

    public static void start() {
        if (service != null && !service.isShutdown()) {
            return;
        }

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                Zones.getZones().forEach((id, zone) -> zone.tick());

                WandListeners.getSelections().forEach((player, selection) -> selection.show(player));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        if (service != null && !service.isShutdown()) {
            service.shutdown();
        }
    }
}
