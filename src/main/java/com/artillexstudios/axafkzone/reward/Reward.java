package com.artillexstudios.axafkzone.reward;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Reward {
    private final List<String> commands;
    private final List<ItemStack> items;
    private final double chance;
    private final String display;

    public Reward(Map<Object, Object> data) {
        this.commands = (List<String>) data.getOrDefault("commands", Collections.emptyList());
        this.items = buildItems(data);
        this.chance = (double) data.getOrDefault("chance", 0.0);
        this.display = (String) data.getOrDefault("display", null);
    }

    private List<ItemStack> buildItems(Map<Object, Object> data) {
        List<Map<Object, Object>> itemData = (List<Map<Object, Object>>) data.get("items");
        if (itemData == null) return Collections.emptyList();

        List<ItemStack> builtItems = new ArrayList<>(itemData.size());
        for (Map<Object, Object> itemMap : itemData) {
            builtItems.add(new ItemBuilder(itemMap).get());
        }
        return builtItems;
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

    public void run(Player player) {
        Scheduler.get().runAt(player.getLocation(), scheduledTask -> {
            commands.forEach(cmd ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()))
            );
        });
        ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), items, player.getLocation());
    }

    @Override
    public String toString() {
        return String.format("Reward{commands=%s, items=%s, chance=%.2f, display=%s}",
                commands, items, chance, display != null ? display : "none");
    }
}
