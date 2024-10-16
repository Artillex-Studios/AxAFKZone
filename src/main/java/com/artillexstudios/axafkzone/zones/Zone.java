package com.artillexstudios.axafkzone.zones;

import com.artillexstudios.axafkzone.reward.Reward;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.utils.RandomUtils;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.ActionBar;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

public class Zone {
    private final String name;
    private final Config settings;
    private Region region;
    private int ticks = 0;
    private final ConcurrentHashMap<Player, Integer> zonePlayers = new ConcurrentHashMap<>();
    private final MessageUtils msg;
    private int rewardSeconds;
    private int rollAmount;
    private final List<Reward> rewards = new ArrayList<>();
    private final Cooldown<Player> cooldown = new Cooldown<>();

    public Zone(String name, Config settings) {
        this.name = name;
        this.settings = settings;
        this.msg = new MessageUtils(settings.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
        this.region = new Region(Serializers.LOCATION.deserialize(settings.getString("zone.location1")),
                Serializers.LOCATION.deserialize(settings.getString("zone.location2")), this);
        reload();
    }

    public void tick() {
        boolean runChecks = ++ticks % 20 == 0;

        var players = region.getPlayersInZone();
        zonePlayers.forEach((player, time) -> {
            if (player.isDead() || !player.isOnline()) {
                Scheduler.get().run(t -> zonePlayers.remove(player));
                return;
            }

            if (!players.contains(player)) {
                msg.sendLang(player, "messages.left", Map.of("%time%", TimeUtils.fancyTime(time * 1_000L)));
                Scheduler.get().run(t -> zonePlayers.remove(player));
                return;
            }

            if (runChecks) {
                var newTime = zonePlayers.computeIfPresent(player, (p, t) -> t + 1);

                if (newTime != null && newTime % rewardSeconds == 0) {
                    var rewardList = giveRewards(player);
                    sendRewardMessages(player, rewardList, newTime);

                    if (CONFIG.getBoolean("reset-after-reward", false)) {
                        zonePlayers.put(player, 0);
                    }
                }
            }

            sendTitleAndActionbar(player, timeUntilNext(player));
            players.remove(player);
        });

        handleNewPlayers(players);
    }

    private void handleNewPlayers(Set<Player> players) {
        var ipLimit = CONFIG.getInt("zone-per-ip-limit", -1);
        players.forEach(player -> {
            if (cooldown.hasCooldown(player)) return;

            if (ipLimit != -1 && zonePlayers.keySet().stream()
                    .filter(p -> Objects.equals(Objects.requireNonNull(p.getAddress()).getAddress(), player.getAddress().getAddress()))
                    .count() >= ipLimit) {
                MESSAGEUTILS.sendLang(player, "zone.ip-limit");
                cooldown.addCooldown(player, 3_000L);
                return;
            }

            msg.sendLang(player, "messages.entered", Map.of("%time%", TimeUtils.fancyTime(rewardSeconds * 1_000L)));
            zonePlayers.put(player, 0);
        });
    }

    private void sendTitleAndActionbar(Player player, long timeUntilNext) {
        var zoneTitle = settings.getString("in-zone.title", null);
        var zoneSubTitle = settings.getString("in-zone.subtitle", null);

        if (zoneTitle != null || zoneSubTitle != null) {
            var title = NMSHandlers.getNmsHandler().newTitle(
                    Optional.ofNullable(zoneTitle)
                            .map(t -> StringUtils.format(t.replace("%time%", TimeUtils.fancyTime(timeUntilNext))))
                            .orElse(Component.empty()),
                    Optional.ofNullable(zoneSubTitle)
                            .map(t -> StringUtils.format(t.replace("%time%", TimeUtils.fancyTime(timeUntilNext))))
                            .orElse(Component.empty()),
                    0, 10, 0
            );
            title.send(player);
        }

        Optional.ofNullable(settings.getString("in-zone.actionbar", null))
                .filter(actionbar -> !actionbar.isBlank())
                .ifPresent(actionbar -> {
                    var actionBar = NMSHandlers.getNmsHandler().newActionBar(StringUtils.format(actionbar.replace("%time%", TimeUtils.fancyTime(timeUntilNext))));
                    actionBar.send(player);
                });
    }

    private void sendRewardMessages(Player player, List<Reward> rewardList, int newTime) {
        var rewardMessages = settings.getStringList("messages.reward");
        if (rewardMessages.isEmpty()) return;

        var prefix = CONFIG.getString("prefix");
        var first = true;
        for (var message : rewardMessages) {
            if (first) {
                message = prefix + message;
                first = false;
            }
            if (message.contains("%reward%")) {
                for (Reward reward : rewardList) {
                    player.sendMessage(StringUtils.formatToString(message, Map.of("%reward%", reward.getDisplay(), "%time%", TimeUtils.fancyTime(newTime * 1_000L))));
                }
            } else {
                player.sendMessage(StringUtils.formatToString(message, Map.of("%time%", TimeUtils.fancyTime(newTime * 1_000L))));
            }
        }
    }

    public long timeUntilNext(Player player) {
        return Optional.ofNullable(zonePlayers.get(player))
                .map(time -> rewardSeconds * 1_000L - (time % rewardSeconds) * 1_000L)
                .orElse(-1L);
    }

    public List<Reward> giveRewards(Player player) {
        if (rewards.isEmpty()) return Collections.emptyList();

        var rewardList = new ArrayList<Reward>();
        var chances = rewards.stream()
                .collect(Collectors.toMap(reward -> reward, Reward::getChance));

        for (var i = 0; i < rollAmount; i++) {
            var selectedReward = RandomUtils.randomValue(new HashMap<>(chances));
            rewardList.add(selectedReward);
            selectedReward.run(player);
        }
        return rewardList;
    }

    public boolean reload() {
        if (!settings.reload()) return false;

        this.rewardSeconds = settings.getInt("reward-time-seconds", 180);
        this.rollAmount = settings.getInt("roll-amount", 1);

        rewards.clear();
        settings.getMapList("rewards").forEach(map -> rewards.add(new Reward(map)));

        return true;
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
