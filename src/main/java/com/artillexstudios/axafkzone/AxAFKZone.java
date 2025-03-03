package com.artillexstudios.axafkzone;

import com.artillexstudios.axafkzone.commands.Commands;
import com.artillexstudios.axafkzone.libraries.Libraries;
import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.listeners.WorldListeners;
import com.artillexstudios.axafkzone.schedulers.TickZones;
import com.artillexstudios.axafkzone.utils.FileUtils;
import com.artillexstudios.axafkzone.utils.NumberUtils;
import com.artillexstudios.axafkzone.utils.UpdateNotifier;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.executor.ThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;

import java.io.File;
import java.net.URLClassLoader;

public final class AxAFKZone extends AxPlugin {
    public static Config CONFIG;
    public static Config LANG;
    public static MessageUtils MESSAGEUTILS;
    private static AxPlugin instance;
    private static ThreadedQueue<Runnable> threadedQueue;
    public static BukkitAudiences BUKKITAUDIENCES;

    public static ThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public static AxPlugin getInstance() {
        return instance;
    }

    public void load() {
        Libraries.load(new DependencyManager(getDescription(), new File(getDataFolder(), "lib"), URLClassLoaderWrapper.wrap((URLClassLoader) getClassLoader())));
    }

    public void enable() {
        instance = this;

        int pluginId = 22054;
        new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

        NumberUtils.reload();
        TickZones.start();

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        threadedQueue = new ThreadedQueue<>("AxAFKZone-Datastore-thread");

        BUKKITAUDIENCES = BukkitAudiences.create(this);
        Commands.registerCommand();
        FileUtils.loadAll();

        getServer().getPluginManager().registerEvents(new WandListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldListeners(), this);

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 6598);
    }

    public void updateFlags(FeatureFlags flags) {
        flags.USE_LEGACY_HEX_FORMATTER.set(true);
    }

    public void disable() {
        TickZones.stop();
        for (Zone zone : Zones.getZones().values()) {
            zone.disable();
        }
    }
}
