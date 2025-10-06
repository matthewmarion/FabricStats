package de.pdinklag.mcstats.fabric;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.fabricmc.api.DedicatedServerModInitializer;

import de.pdinklag.mcstats.Config;
import de.pdinklag.mcstats.cli.JSONConfig;
import de.pdinklag.mcstats.cli.StdoutLogWriter;
import de.pdinklag.mcstats.util.ResourceUtils;

public class MinecraftStatsFabric implements DedicatedServerModInitializer {
    private static final String STATS_RESOURCE_DIR = "stats";
    private static final String WEB_RESOURCE_DIR = "www";
    private static final int DEFAULT_UPDATE_INTERVAL_MINUTES = 6;

    private ScheduledExecutorService scheduler;

    @Override
    public void onInitializeServer() {
        try {
            System.out.println("MinecraftStatsFabric.onInitializeServer");
            Path configPath = Path.of("..","config", "cli", "config.json");
            final Config config = new JSONConfig(configPath);

            // Hardcode Dynmap's built-in webserver path as document root
            final Path dynmapWeb = Path.of("dynmap", "web", "mcstats");
            if (!Files.exists(dynmapWeb)) {
                Files.createDirectories(dynmapWeb);
            }
            config.setDocumentRoot(dynmapWeb);

            ResourceUtils.extractResourcesToFiles(STATS_RESOURCE_DIR, config.getStatsPath());
            ResourceUtils.extractResourcesToFiles(WEB_RESOURCE_DIR, config.getDocumentRoot());

            final StdoutLogWriter log = new StdoutLogWriter();
            final FabricUpdater updater = new FabricUpdater(config, log);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(updater::run, 0, DEFAULT_UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (scheduler != null) {
                    scheduler.shutdownNow();
                }
            }));
        } catch (Exception e) {
            System.err.println("MinecraftStats: onInitializeServer failed");
            e.printStackTrace(System.err);
        }
    }
}
