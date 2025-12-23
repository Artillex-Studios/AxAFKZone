package com.artillexstudios.axafkzone.zones;

import com.artillexstudios.axafkzone.reward.Reward;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.utils.RandomUtils;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundClearTitlesWrapper;
import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.ActionBar;
import com.artillexstudios.axapi.utils.BossBar;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

public class Zone {
    private final ConcurrentHashMap<Player, Integer> zonePlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, BossBar> bossbars = new ConcurrentHashMap<>();
    private final LinkedList<Reward> rewards = new LinkedList<>();
    private final Cooldown<Player> cooldown = Cooldown.createSynchronized();
    private final MessageUtils msg;
    private final String name;
    private final Config settings;
    private Region region;
    private int ticks = 0;
    private int rewardSeconds;
    private int rollAmount;

    public Zone(String name, Config settings) {
        this.name = name;
        this.settings = settings;
        this.msg = new MessageUtils(settings.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
        reload();
    }

    public void tick() {
        boolean runChecks = ++ticks % 20 == 0;

        final Set<Player> players = region.getPlayersInZone();
        for (Iterator<Map.Entry<Player, Integer>> it = zonePlayers.entrySet().iterator(); it.hasNext(); ) {
            Player player = it.next().getKey();
            if (!player.isOnline()) {
                players.remove(player);
                leave(player, it);
                continue;
            }

            // player left
            if (!players.contains(player)) {
                leave(player, it);
                continue;
            }

            if (runChecks) {
                int newTime = zonePlayers.get(player) + 1;
                zonePlayers.put(player, newTime);

                if (newTime != 0 && newTime % rewardSeconds == 0) {
                    giveRewards(player, newTime);
                    if (CONFIG.getBoolean("reset-after-reward", false)) zonePlayers.put(player, 0);
                }

                sendTitle(player);
                sendActionbar(player);
                updateBossbar(player);
            }
            players.remove(player);
        }

        int ipLimit = CONFIG.getInt("zone-per-ip-limit", -1);
        // player entered
        for (Player player : players) {
            if (cooldown.hasCooldown(player)) continue;
            if (ipLimit != -1 && countIPAccounts(player) >= ipLimit) {
                MESSAGEUTILS.sendLang(player, "zone.ip-limit");
                cooldown.addCooldown(player, 3_000L);
                continue;
            }

            enter(player);
        }
    }

    private int countIPAccounts(Player player) {
        InetSocketAddress address = player.getAddress();
        int count = 0;
        if (address == null) return count;
        if (player.hasPermission("axafkzone.bypass.iplimit")) return count;
        for (Player pl : zonePlayers.keySet()) {
            if (pl.hasPermission("axafkzone.bypass.iplimit")) continue;
            InetSocketAddress address2 = pl.getAddress();
            if (address2 == null) continue;
            if (!address2.getAddress().equals(address.getAddress())) continue;
            count++;
        }
        return count;
    }

    private void enter(Player player) {
        BossBar bossBar = bossbars.remove(player);
        if (bossBar != null) bossBar.remove();

        msg.sendLang(player, "messages.entered", Map.of("%time%", TimeUtils.fancyTime(rewardSeconds * 1_000L)));
        zonePlayers.put(player, 0);

        Section section;
        if ((section = settings.getSection("in-zone.bossbar")) != null) {
            bossBar = BossBar.create(
                    StringUtils.format(section.getString("name").replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))),
                    1,
                    BossBar.Color.valueOf(section.getString("color").toUpperCase()),
                    BossBar.Style.parse(section.getString("style"))
            );
            bossBar.show(player);
            bossbars.put(player, bossBar);
        }
        sendTitle(player);
        sendActionbar(player);
    }

    private void leave(Player player, Iterator<Map.Entry<Player, Integer>> it) {
        if (player.isOnline()) {
            msg.sendLang(player, "messages.left", Map.of("%time%", TimeUtils.fancyTime(zonePlayers.get(player) * 1_000L)));
        }
        it.remove();
        BossBar bossBar = bossbars.remove(player);
        if (bossBar != null) bossBar.remove();
        removeTitle(player);
    }

    private void sendTitle(Player player) {
        String zoneTitle = settings.getString("in-zone.title", null);
        String zoneSubTitle = settings.getString("in-zone.subtitle", null);
        if (zoneTitle != null && !zoneTitle.isBlank() || zoneSubTitle != null && !zoneSubTitle.isBlank()) {
            Title title = Title.create(
                    zoneTitle == null ? Component.empty() : StringUtils.format(zoneTitle.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))),
                    zoneSubTitle == null ? Component.empty() : StringUtils.format(zoneSubTitle.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))),
                    0, 25, 0
            );
            title.send(player);
        }
    }

    private void removeTitle(Player player) {
        ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
        wrapper.sendPacket(new ClientboundClearTitlesWrapper(true));
    }

    private void sendActionbar(Player player) {
        String zoneActionbar = settings.getString("in-zone.actionbar", null);
        if (zoneActionbar != null && !zoneActionbar.isBlank()) {
            ActionBar.send(player, StringUtils.format(zoneActionbar.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))));
        }
    }

    private void updateBossbar(Player player) {
        BossBar bossBar = bossbars.get(player);
        if (bossBar == null) return;
        Integer time = zonePlayers.get(player);
        if (time == null) return;

        int barDirection = CONFIG.getInt("bossbar-direction", 0);
        float calculated = (float) (time % rewardSeconds) / (rewardSeconds - 1);
        bossBar.progress(Math.max(0f, Math.min(1f, barDirection == 0 ? 1f - calculated : calculated)));

        Section section;
        if ((section = settings.getSection("in-zone.bossbar")) != null) {
            bossBar.title(StringUtils.format(section.getString("name").replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))));
        }
    }

    private void giveRewards(Player player, int newTime) {
        final List<Reward> rewardList = rollAndGiveRewards(player);
        if (settings.getStringList("messages.reward").isEmpty()) return;

        final String prefix = CONFIG.getString("prefix");
        boolean first = true;
        for (String string : settings.getStringList("messages.reward")) {
            if (first) {
                string = prefix + string;
                first = false;
            }

            if (string.contains("%reward%")) {
                for (Reward reward : rewardList) {
                    player.sendMessage(StringUtils.formatToString(string, Map.of("%reward%", Optional.ofNullable(reward.getDisplay()).orElse("---"), "%time%", TimeUtils.fancyTime(newTime * 1_000L))));
                }
                continue;
            }
            player.sendMessage(StringUtils.formatToString(string, Map.of("%time%", TimeUtils.fancyTime(newTime * 1_000L))));
        }
    }

    public long timeUntilNext(Player player) {
        Integer time = zonePlayers.get(player);
        if (time == null) return -1;
        return rewardSeconds * 1_000L - (time % rewardSeconds) * 1_000L;
    }

    public List<Reward> rollAndGiveRewards(Player player) {
        final List<Reward> rewardList = new ArrayList<>();
        if (rewards.isEmpty()) return rewardList;
        final HashMap<Reward, Double> chances = new HashMap<>();
        for (Reward reward : rewards) {
            chances.put(reward, reward.getChance());
        }

        for (int i = 0; i < rollAmount; i++) {
            Reward sel = RandomUtils.randomValue(chances);
            rewardList.add(sel);
            sel.run(player);
        }

        return rewardList;
    }

    public boolean reload() {
        if (!settings.reload()) return false;

        this.region = new Region(
                Serializers.LOCATION.deserialize(settings.getString("zone.location1")),
                Serializers.LOCATION.deserialize(settings.getString("zone.location2")),
                this
        );

        this.rewardSeconds = settings.getInt("reward-time-seconds", 180);
        this.rollAmount = settings.getInt("roll-amount", 1);

        rewards.clear();
        for (Map<Object, Object> map : settings.getMapList("rewards")) {
            final Reward reward = new Reward(map);
            rewards.add(reward);
        }

        return true;
    }

    public void disable() {
        for (BossBar bossBar : bossbars.values()) {
            bossBar.remove();
        }
    }

    public void setRegion(Region region) {
        this.region = region;
        settings.set("zone.location1", Serializers.LOCATION.serialize(region.getCorner1()));
        settings.set("zone.location2", Serializers.LOCATION.serialize(region.getCorner2()));
        settings.save();
    }

    public String getName() {
        return name;
    }

    public Config getSettings() {
        return settings;
    }

    public Region getRegion() {
        return region;
    }

    public int getTicks() {
        return ticks;
    }
}
