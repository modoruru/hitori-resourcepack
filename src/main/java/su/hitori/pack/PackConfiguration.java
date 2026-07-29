package su.hitori.pack;

import su.hitori.api.configuration.Field;
import su.hitori.api.configuration.SectionScheme;

public final class PackConfiguration extends SectionScheme {

    public final Field<Boolean> sendPack = Field.create(true);
    public final Field<Boolean> requirePack = Field.create(true);
    public final Field<String> publicIp = Field.create("localhost:25566");
    public final Field<Integer> port = Field.create(25566);
    public final Field<Boolean> coreProtectSupport = Field.create(true);

    public final Field<String> unableToApplySkin = Field.create("Unable to apply skin while posing.");
    public final Field<String> allowResourcepackToPlay = Field.create("Enable resourcepack installation to playing on this server.");
    public final Field<String> errorInstallingResourcepack = Field.create("An error occurred while installing the resourcepack.");
    public final Field<String> resourcepackIsGenerating = Field.create("Resourcepack is being generated, rejoin later.");

}
