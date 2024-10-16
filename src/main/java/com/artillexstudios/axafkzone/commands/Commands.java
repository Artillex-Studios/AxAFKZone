package com.artillexstudios.axafkzone.commands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.selection.Selection;
import com.artillexstudios.axafkzone.utils.CommandMessages;
import com.artillexstudios.axafkzone.utils.FileUtils;
import com.artillexstudios.axafkzone.utils.NBTUtils;
import com.artillexstudios.axafkzone.utils.NumberUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.LANG;
import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

@CommandPermission(value = "axafkzone.admin")
public class Commands implements OrphanCommand {

    @DefaultFor({"~"})
    public void help(@NotNull CommandSender sender) {
        for (String m : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }

    @Subcommand("wand")
    public void wand(Player sender) {
        final ItemStack it = new ItemBuilder(LANG.getSection("selection-wand")).glow(true).get();
        NBTUtils.writeToNBT(it, "axafkzone-wand", true);
        sender.getInventory().addItem(it);
    }

    @Subcommand("create")
    public void create(Player sender, String name) {
        final Zone zone = Zones.getZoneByName(name);
        if (zone != null) {
            MESSAGEUTILS.sendLang(sender, "zone.already-exists", Collections.singletonMap("%name%", name));
            return;
        }

        if (!WandListeners.getSelections().containsKey(sender)) {
            MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", name));
            return;
        }

        final Selection sel = WandListeners.getSelections().remove(sender);

        if (sel.getPosition1() == null || sel.getPosition2() == null || !Objects.equals(sel.getPosition1().getWorld(), sel.getPosition2().getWorld())) {
            MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", name));
            return;
        }

        FileUtils.create(name, sel);

        MESSAGEUTILS.sendLang(sender, "zone.created", Collections.singletonMap("%name%", name));
    }

    @Subcommand("delete")
    public void delete(CommandSender sender, Zone zone) {
        FileUtils.delete(zone);

        MESSAGEUTILS.sendLang(sender, "zone.deleted", Collections.singletonMap("%name%", zone.getName()));
    }

    @Subcommand("redefine")
    public void redefine(Player sender, Zone zone) {
        if (!WandListeners.getSelections().containsKey(sender)) {
            MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", zone.getName()));
            return;
        }

        final Selection sel = WandListeners.getSelections().remove(sender);

        if (sel.getPosition1() == null || sel.getPosition2() == null || !Objects.equals(sel.getPosition1().getWorld(), sel.getPosition2().getWorld())) {
            MESSAGEUTILS.sendLang(sender, "selection.no-selection", Collections.singletonMap("%name%", zone.getName()));
            return;
        }

        zone.setRegion(new Region(sel.getPosition1(), sel.getPosition2(), zone));
        MESSAGEUTILS.sendLang(sender, "zone.redefined", Collections.singletonMap("%name%", zone.getName()));
    }

    @Subcommand("reload")
    public void reload(@NotNull CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055[AxAFKZone] &#FF8855Reloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╠ &#FF8855Reloaded &fconfig.yml&#FF8855!"));

        if (!LANG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "lang.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╠ &#FF8855Reloaded &flang.yml&#FF8855!"));

        Commands.registerCommand();

        NumberUtils.reload();
        FileUtils.loadAll();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#CC0055╚ &#FF8855Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    private static BukkitCommandHandler handler = null;
    public static void registerCommand() {
        if (handler == null) {
            handler = BukkitCommandHandler.create(AxAFKZone.getInstance());
            handler.registerValueResolver(Zone.class, resolver -> {
                final String zoneName = resolver.popForParameter();
                final Zone zone = Zones.getZoneByName(zoneName);
                if (zone == null) {
                    resolver.actor().as(BukkitCommandActor.class).getSender().sendMessage(StringUtils.formatToString(CONFIG.getString("prefix") + LANG.getString("zone.not-found").replace("%name%", zoneName)));
                    throw new CommandErrorException();
                }

                return zone;
            });

            handler.getTranslator().add(new CommandMessages());
            handler.setLocale(new Locale("en", "US"));

            handler.getAutoCompleter().registerParameterSuggestions(Zone.class, (args, sender, command) -> Zones.getZones().values().stream().map(Zone::getName).collect(Collectors.toList()));
        }

        handler.unregisterAllCommands();

        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();
    }
}