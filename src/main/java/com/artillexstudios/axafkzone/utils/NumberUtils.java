package com.artillexstudios.axafkzone.utils;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;

public class NumberUtils {
    private static NumberFormat formatter;
    private static NumberFormat shortFormat;

    public static void reload() {
        String[] lang = CONFIG.getString("number-formatting.short", "en_US").split("_");
        shortFormat = DecimalFormat.getCompactNumberInstance(new Locale(lang[0], lang[1]), NumberFormat.Style.SHORT);
        updateFormatter(CONFIG.getInt("number-formatting.mode", 0));
    }

    private static void updateFormatter(int mode) {
        switch (mode) {
            case 0:
                formatter = new DecimalFormat(CONFIG.getString("number-formatting.formatted", "#,###.##"));
                break;
            case 1:
                formatter = shortFormat;
                break;
            case 2:
                formatter = null;
                break;
            default:
                formatter = new DecimalFormat(CONFIG.getString("number-formatting.formatted", "#,###.##"));
                break;
        }
    }

    public static String formatNumber(double number) {
        return formatter == null ? String.valueOf(number) : formatter.format(number);
    }

    @Nullable
    public static Double parseNumber(String number) {
        try {
            return shortFormat.parse(number.toUpperCase()).doubleValue();
        } catch (ParseException ex) {
            return null;
        }
    }
}
