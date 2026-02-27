package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import org.joml.Vector2i;
import org.json.JSONObject;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.registry.RegistryKey;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.RawBlueprint;

import java.util.Optional;
import java.util.logging.Logger;

public final class BlueprintConveyor extends AbstractConveyorWithRegistry<RawBlueprint, Blueprint> {

    private static final Logger LOGGER = LoggerFactory.instance().create(BlueprintConveyor.class);

    public BlueprintConveyor(Key key, RegistryKey<Blueprint> registryKey) {
        super(key, registryKey);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        registry.clear();
        for (RawBlueprint snapshot : snapshots.values()) {
            Key key = snapshot.key();

            JSONObject body = snapshot.body();
            JSONObject
                    settingsBody   = body.optJSONObject("settings"),
                    texturesBody   = body.optJSONObject("textures"),
                    nodesBody      = body.optJSONObject("nodes"),
                    variantsBody   = body.optJSONObject("variants"),
                    animationsBody = body.optJSONObject("animations");

            // let's check at the start if all bodies is there
            if(settingsBody == null || texturesBody == null || nodesBody == null || variantsBody == null || animationsBody == null) {
                LOGGER.warning(String.format(
                        "Blueprint json of %s missing some of this bodies: [settings, textures, nodes, variants, animations]",
                        key.asString()
                ));
                continue;
            }

            registry.register(
                    key,
                    new Blueprint(
                            key,
                            Optional.ofNullable(settingsBody.optJSONArray("bounding_box"))
                                    .map(array -> new Vector2i(array.getInt(0), array.getInt(1)))
                                    .orElse(null),
                            settingsBody.optInt("interpolation_duration", 1),
                            settingsBody.optInt("teleportation_duration", 1)
                    )
            );
        }

        collectingOrGenerating = false;
    }

}
