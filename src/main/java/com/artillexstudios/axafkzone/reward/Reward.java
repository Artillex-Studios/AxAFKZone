package com.artillexstudios.axafkzone.reward;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
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

    public Reward(Map<Object, Object> str) {
        final List<String> commands = (List<String>) str.getOrDefault("commands", new ArrayList<>());
        final ArrayList<ItemStack> items = new ArrayList<>();
        Number chance = (Number) str.get("chance");

        var map = (List<Map<Object, Object>>) str.get("items");
        if (map != null) {
            final LinkedList<Map<Object, Object>> map2 = new LinkedList<>(map);
            for (Map<Object, Object> it : map2) {
                items.add(ItemBuilder.create(it).get());
            }
        }

        String display = null;
        if (str.containsKey("display")) display = (String) str.get("display");

        this.chance = chance.doubleValue();
        this.items = items;
        this.commands = commands;
        this.display = display;
        this.permission = (String) str.getOrDefault("permission", null);
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

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isBlank();
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
        return "Reward{" + "commands=" + commands + ", items=" + items + ", chance=" + chance + '}';
    }
}