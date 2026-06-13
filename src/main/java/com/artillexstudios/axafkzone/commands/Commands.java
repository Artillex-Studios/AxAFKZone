package com.artillexstudios.axafkzone.commands;

import com.artillexstudios.axafkzone.commands.subcommands.Create;
import com.artillexstudios.axafkzone.commands.subcommands.Delete;
import com.artillexstudios.axafkzone.commands.subcommands.Help;
import com.artillexstudios.axafkzone.commands.subcommands.Redefine;
import com.artillexstudios.axafkzone.commands.subcommands.Reload;
import com.artillexstudios.axafkzone.commands.subcommands.Teleport;
import com.artillexstudios.axafkzone.commands.subcommands.Wand;
import com.artillexstudios.axafkzone.zones.Zone;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class Commands implements OrphanCommand {

    @DefaultFor({"~", "~ help"})
    @CommandPermission(value = "axafkzone.help", defaultAccess = PermissionDefault.OP)
    public void help(@NotNull CommandSender sender) {
        Help.INSTANCE.execute(sender);
    }

    @Subcommand("wand")
    @CommandPermission(value = "axafkzone.wand", defaultAccess = PermissionDefault.OP)
    public void wand(Player sender) {
        Wand.INSTANCE.execute(sender);
    }

    @Subcommand("create")
    @CommandPermission(value = "axafkzone.create", defaultAccess = PermissionDefault.OP)
    public void create(Player sender, String name) {
        Create.INSTANCE.execute(sender, name);
    }

    @Subcommand("delete")
    @CommandPermission(value = "axafkzone.delete", defaultAccess = PermissionDefault.OP)
    public void delete(CommandSender sender, Zone zone) {
        Delete.INSTANCE.execute(sender, zone);
    }

    @Subcommand("redefine")
    @CommandPermission(value = "axafkzone.redefine", defaultAccess = PermissionDefault.OP)
    public void redefine(Player sender, Zone zone) {
        Redefine.INSTANCE.execute(sender, zone);
    }

    @Subcommand("reload")
    @CommandPermission(value = "axafkzone.reload", defaultAccess = PermissionDefault.OP)
    public void reload(CommandSender sender) {
        Reload.INSTANCE.execute(sender);
    }

    @Subcommand("tp")
    @CommandPermission(value = "axafkzone.tp", defaultAccess = PermissionDefault.OP)
    public void tp(Player sender, Zone zone) {
        Teleport.INSTANCE.execute(sender, zone);
    }
}
