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
    private final String permission;
    private final int minimumTime;
    private final double chance;
    private final String display;

    public Reward(Map<Object, Object> str) {
        this.items = new ArrayList<>();
        List<Map<Object, Object>> items = (List<Map<Object, Object>>) str.get("items");
        if (items != null) {
            LinkedList<Map<Object, Object>> map2 = new LinkedList<>(items);
            for (Map<Object, Object> it : map2) {
                this.items.add(ItemBuilder.create(it).get());
            }
        }
        this.display = (String) str.getOrDefault("display", null);
        this.permission = (String) str.getOrDefault("permission", null);
        this.minimumTime = ((Number) str.getOrDefault("minimum-time", 0)).intValue();
        this.chance = ((Number) str.getOrDefault("chance", 10D)).doubleValue();
        this.commands = (List<String>) str.getOrDefault("commands", new ArrayList<>());
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String getPermission() {
        return permission;
    }

    public int getMinimumTime() {
        return minimumTime;
    }

    public double getChance() {
        return chance;
    }

    public String getDisplay() {
        return display;
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
