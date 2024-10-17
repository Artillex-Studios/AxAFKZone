package com.artillexstudios.axafkzone.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.LANG;

public class TimeUtils {
    public static @NotNull String fancyTime(long time) {
        if (time < 0) return "---";

        long totalSeconds = Duration.ofMillis(time).getSeconds();
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return formatTime(days, hours, minutes, seconds);
    }

    private static @NotNull String formatTime(long days, long hours, long minutes, long seconds) {
        int formatType = CONFIG.getInt("timer-format", 1);

        switch (formatType) {
            case 1:
                return formatStandard(days, hours, minutes, seconds);
            case 2:
                return formatShort(days, hours, minutes, seconds);
            default:
                return formatVerbose(days, hours, minutes, seconds);
        }
    }

    private static @NotNull String formatStandard(long days, long hours, long minutes, long seconds) {
        if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static @NotNull String formatShort(long days, long hours, long minutes, long seconds) {
        if (days > 0) return days + LANG.getString("time.day", "d");
        if (hours > 0) return hours + LANG.getString("time.hour", "h");
        if (minutes > 0) return minutes + LANG.getString("time.minute", "m");
        return seconds + LANG.getString("time.second", "s");
    }

    private static @NotNull String formatVerbose(long days, long hours, long minutes, long seconds) {
        if (days > 0) {
            return String.format("%02d" + LANG.getString("time.day", "d") + " %02d" + LANG.getString("time.hour", "h") + " %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return String.format("%02d" + LANG.getString("time.hour", "h") + " %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), hours, minutes, seconds);
        }
        return String.format("%02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), minutes, seconds);
    }
}