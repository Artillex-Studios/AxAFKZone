package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.selection.Selection;
import com.artillexstudios.axafkzone.utils.NBTUtils;
import com.artillexstudios.axapi.serializers.Serializers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.WeakHashMap;

import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

public class WandListeners implements Listener {
    private static final WeakHashMap<Player, Selection> selections = new WeakHashMap<>();

    @NotNull
    public static WeakHashMap<Player, Selection> getSelections() {
        return selections;
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isValidInteraction(event)) return;

        event.setCancelled(true);
        Location clickedLocation = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSelection(player, clickedLocation, true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleSelection(player, clickedLocation, false);
        }
    }

    private boolean isValidInteraction(PlayerInteractEvent event) {
        return event.getClickedBlock() != null &&
                event.getItem() != null &&
                !event.getItem().getType().equals(Material.AIR) &&
                event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR &&
                Boolean.TRUE.equals(NBTUtils.readBooleanFromNBT(event.getPlayer().getInventory().getItemInMainHand(), "axafkzone-wand"));
    }

    private void handleSelection(@NotNull Player player, @NotNull Location location, boolean isLeftClick) {
        selections.computeIfAbsent(player, k -> new Selection());

        Selection selection = selections.get(player);
        Location currentPos = isLeftClick ? selection.getPosition1() : selection.getPosition2();

        if (Objects.equals(currentPos, location)) return;

        player.setCooldown(player.getInventory().getItemInMainHand().getType(), 5);
        if (isLeftClick) {
            selection.setPosition1(location);
            MESSAGEUTILS.sendLang(player, "selection.pos1", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
        } else {
            selection.setPosition2(location);
            MESSAGEUTILS.sendLang(player, "selection.pos2", Collections.singletonMap("%location%", Serializers.LOCATION.serialize(location)));
        }
    }
}