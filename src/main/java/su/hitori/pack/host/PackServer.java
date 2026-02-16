package su.hitori.pack.host;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.entity.Player;
import su.hitori.api.Pair;
import su.hitori.api.util.Text;
import su.hitori.pack.PackConfiguration;
import su.hitori.pack.generation.Generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

public final class PackServer {

    private final Generator generator;

    private HttpServer server;

    public PackServer(Generator generator) {
        this.generator = generator;
    }

    public void start() {
        if(server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress(PackConfiguration.I.port), 0);
            server.createContext("/pack", new ResourcePackHandler(generator));
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if(server != null) {
            server.stop(0);
            server = null;
        }
    }

    public void sendPack(Player player) {
        if(generator.isGenerating()) {
            player.kick(Text.create("Ресурспак генерируется, перезайдите позже."));
            return;
        }

        Optional<Pair<File, String>> pack = generator.getResult();
        assert pack.isPresent();
        String hash = pack.get().second();
        player.setResourcePack(getPackURI().toString(), hash, true);
    }

    public URI getPackURI() {
        return URI.create("http://" + PackConfiguration.I.publicIp + "/pack");
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
