package de.pdinklag.mcstats.fabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import de.pdinklag.mcstats.Config;
import de.pdinklag.mcstats.Updater;
import de.pdinklag.mcstats.LogWriter;
import de.pdinklag.mcstats.DataSource;
import net.fabricmc.loader.api.FabricLoader;

public class FabricUpdater extends Updater {
    public FabricUpdater(Config config, LogWriter log) {
        super(config, log);
    }

    @Override
    protected String getServerMotd() {
        for (DataSource dataSource : config.getDataSources()) {
            final Path propertiesPath = dataSource.getServerPath().resolve("server.properties");
            if (Files.exists(propertiesPath)) {
                final Properties properties = new Properties();
                final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                try (final InputStream fis = Files.newInputStream(propertiesPath)) {
                    properties.load(new InputStreamReader(fis, decoder));
                } catch (CharacterCodingException e) {
                    try (final BufferedReader reader = Files.newBufferedReader(propertiesPath,
                            StandardCharsets.ISO_8859_1)) {
                        properties.load(reader);
                    } catch (IOException e1) {
                    }
                } catch (IOException e) {
                }
                final String motd = properties.getProperty("motd");
                if (motd != null) {
                    return motd;
                }
            }
        }
        return null;
    }

    @Override
    protected String getVersion() {
        return FabricLoader.getInstance().getModContainer("fabricstats")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}


