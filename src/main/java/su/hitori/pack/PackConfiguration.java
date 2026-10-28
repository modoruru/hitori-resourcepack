package su.hitori.pack;

import su.hitori.api.configuration.Field;
import su.hitori.api.configuration.SectionScheme;

public final class PackConfiguration extends SectionScheme {

    public final Field<String> publicIp = Field.create("localhost:25566");
    public final Field<Integer> port = Field.create(25566);

    public final Field<Boolean>
            sendPack = Field.create(true),
            requirePack = Field.create(true),
            coreProtectSupport = Field.create(true),
            sendPackDuringConfigurationPhase = Field.create(false),
            restrictActionsWhenLoadingPackDuringPlay = Field.create(true);

    public final Field<String>
            unableToApplySkin = Field.create("Unable to apply skin while posing."),
            allowResourcepackToPlay = Field.create("Enable resource pack installation to playing on this server."),
            errorInstallingResourcepack = Field.create("An error occurred while installing the resource pack."),
            resourcepackIsGenerating = Field.create("Resource pack is being generated, rejoin later.");

}
