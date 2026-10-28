package su.hitori.pack.host;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import su.hitori.api.Pair;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.util.LoggerUtil;
import su.hitori.api.util.Text;
import su.hitori.pack.PackConfiguration;
import su.hitori.pack.generation.Generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public final class PackServer {

    private final Logger LOGGER = LoggerFactory.instance().create();

    private final PackConfiguration configuration;
    private final Generator generator;

    private @Nullable HttpServer server;

    public PackServer(PackConfiguration configuration, Generator generator) {
        this.configuration = configuration;
        this.generator = generator;
    }

    public Generator generator() {
        return generator;
    }

    public void start() {
        if(server != null || !configuration.sendPack.get()) return;

        try {
            server = HttpServer.create(new InetSocketAddress(configuration.port.get()), 0);
            server.createContext("/pack", new ResourcePackHandler(generator));
            server.setExecutor(null);
            server.start();
        }
        catch (IOException exception) {
            LOGGER.warning(LoggerUtil.exceptionToString(exception));
        }
    }

    public void stop() {
        if(server != null) {
            server.stop(0);
            server = null;
        }
    }

    /**
     * @return kick message
     */
    public @Nullable Component createKickMessage(ResourcePackStatus status) {
        return switch (status) {
            case DECLINED, DISCARDED ->
                    configuration.requirePack.get()
                            ? Text.create(configuration.allowResourcepackToPlay.get())
                            : null;
            case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD -> Text.create(configuration.errorInstallingResourcepack.get());
            default -> null;
        };
    }

    public Optional<ResourcePackInfo> createInfo() {
        return generator.getResult()
                .map(pair -> {
                    URI uri = getPackURI();

                    return ResourcePackInfo.resourcePackInfo(
                            UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8)),
                            uri,
                            pair.second()
                    );
                });
    }

    public void sendPackForEveryone() {
        createInfo().ifPresent(info -> {
            ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                    .replace(true)
                    .required(true)
                    .packs(info)
                    .callback((_, status, audience) -> {
                        Component kickMessage = createKickMessage(status);
                        if(kickMessage != null) ((Player) audience).kick(kickMessage);
                    })
                    .build();
            Bukkit.getOnlinePlayers().forEach(player -> player.sendResourcePacks(request));
        });
    }

    public URI getPackURI() {
        return URI.create("http://" + configuration.publicIp.get() + "/pack");
    }

    private record ResourcePackHandler(Generator generator) implements HttpHandler {
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void handle(HttpExchange t) throws IOException {
            Optional<Pair<File, String>> pack = generator.getResult();
            if(pack.isEmpty()) {
                t.close();
                return;
            }

            File file = pack.get().first();
            byte[] byteArray = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(byteArray);
            fis.close();

            t.sendResponseHeaders(200, byteArray.length);
            OutputStream os = t.getResponseBody();
            os.write(byteArray, 0, byteArray.length);
            os.close();
        }
    }

}
