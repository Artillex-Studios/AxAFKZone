package com.artillexstudios.axafkzone.schedulers;

import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import com.artillexstudios.axapi.executor.ExceptionReportingScheduledThreadPool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickZones {
    private static ScheduledExecutorService service = null;

    public static void start() {
        if (service != null) service.shutdown();

        service = new ExceptionReportingScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> {
            try {
                for (Zone zone : Zones.getZones().values()) {
                    zone.tick();
                }

                WandListeners.getSelections().forEach((player, selection) -> {
                    selection.show(player);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
