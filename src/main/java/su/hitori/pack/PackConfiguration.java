package su.hitori.pack;

import su.hitori.api.config.Configuration;

import java.nio.file.Path;

public final class PackConfiguration extends Configuration {

    public static PackConfiguration I;

    public PackConfiguration(Path path) {
        super(path);
        I = this;
    }

    public String publicIp = "localhost:25566";
    public int port = 25566;
    public boolean coreProtectSupport = true;

    public String unableToApplySkin = "Unable to apply skin while posing.";
    public String allowResourcepackToPlay = "Enable resourcepack installation to playing on this server.";
    public String errorInstallingResourcepack = "An error occurred while installing the resourcepack.";
    public String resourcepackIsGenerating = "Resourcepack is being generated, rejoin later.";

}
