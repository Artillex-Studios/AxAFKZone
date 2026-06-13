package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.selection.Selection;
import com.artillexstudios.axapi.items.NBTWrapper;
import com.artillexstudios.axapi.serializers.Serializers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

public class WandListeners implements Listener {
    private static final Map<Player, Selection> selections = new HashMap<>();

    @NotNull
    public static Map<Player, Selection> getSelections() {
        return selections;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        selections.remove(event.getPlayer());
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand.getType().equals(Material.AIR)) return;

        NBTWrapper wrapper = new NBTWrapper(itemInHand);
        if (!wrapper.getBooleanOr("axafkzone-wand", false)) return;
        event.setCancelled(true);

        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            onLeftClick(event.getPlayer(), event.getClickedBlock().getLocation());
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            onRightClick(event.getPlayer(), event.getClickedBlock().getLocation());
        }
    }

    private void onLeftClick(@NotNull Player player, @NotNull Location location) {
        if (!selections.containsKey(player)) selections.put(player, new Selection(player));
        if (Objects.equals(selections.get(player).getPosition1(), location)) return;

        player.setCooldown(player.getInventory().getItemInMainHand().getType(), 5);
        selections.get(player).setPosition1(location);
        MESSAGEUTILS.sendLang(player, "selection.pos1", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
    }

    private void onRightClick(@NotNull Player player, @NotNull Location location) {
        if (!selections.containsKey(player)) selections.put(player, new Selection(player));
        if (Objects.equals(selections.get(player).getPosition2(), location)) return;

        player.setCooldown(player.getInventory().getItemInMainHand().getType(), 5);
        selections.get(player).setPosition2(location);
        MESSAGEUTILS.sendLang(player, "selection.pos2", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
    }
}
