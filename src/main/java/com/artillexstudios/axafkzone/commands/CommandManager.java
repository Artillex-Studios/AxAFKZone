package com.artillexstudios.axafkzone.commands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.utils.CommandMessages;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import com.artillexstudios.axapi.utils.StringUtils;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.Orphans;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.LANG;

public class CommandManager {
    private static BukkitCommandHandler handler = null;

    public static void load() {
        handler = BukkitCommandHandler.create(AxAFKZone.getInstance());

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(Locale.of("en", "US"));

        handler.registerValueResolver(Zone.class, resolver -> {
            final String zoneName = resolver.popForParameter();
            final Zone zone = Zones.getZoneByName(zoneName);
            if (zone == null) {
                resolver.actor().as(BukkitCommandActor.class).getSender().sendMessage(StringUtils.formatToString(CONFIG.getString("prefix") + LANG.getString("zone.not-found").replace("%name%", zoneName)));
                throw new CommandErrorException();
            }

            return zone;
        });

        handler.getAutoCompleter().registerParameterSuggestions(Zone.class, (args, sender, command) -> Zones.getZones().values().stream().map(Zone::getName).collect(Collectors.toList()));

        reload();
    }

    public static void reload() {
        handler.unregisterAllCommands();

        List<String> aliases = CONFIG.getStringList("command-aliases");
        if (!aliases.isEmpty()) {
            handler.register(Orphans.path(aliases.toArray(String[]::new)).handler(new Commands()));
        }

        handler.registerBrigadier();
    }
}
