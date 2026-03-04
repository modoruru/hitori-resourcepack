package su.hitori.pack.blueprint;

import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.pack.type.blueprint.Blueprint;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public final class Blueprints {

    private final Registry<@NotNull Blueprint> blueprintRegistry;
    private final ExecutorService executorService;
    private final Set<BlueprintInstance> instances;

    public Blueprints(Registry<@NotNull Blueprint> blueprintRegistry, ExecutorService executorService) {
        this.blueprintRegistry = blueprintRegistry;
        this.executorService = executorService;
        this.instances = new HashSet<>();
    }

    public BlueprintInstance instantiate(Blueprint blueprint) {
        BlueprintInstance blueprintInstance = new BlueprintInstance(this, blueprint, executorService);
        instances.add(blueprintInstance);
        return blueprintInstance;
    }

    void remove(BlueprintInstance blueprintInstance) {
        instances.remove(blueprintInstance);
    }

    public void destroyAll() {
        instances.forEach(BlueprintInstance::destroy);
    }

}
