package su.hitori.pack.blueprint;

import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.pack.type.blueprint.Blueprint;

public final class Blueprints {

    private final Registry<@NotNull Blueprint> blueprintRegistry;

    public Blueprints(Registry<@NotNull Blueprint> blueprintRegistry) {
        this.blueprintRegistry = blueprintRegistry;
    }

}
