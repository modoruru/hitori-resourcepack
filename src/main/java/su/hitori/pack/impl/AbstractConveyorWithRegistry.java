package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import su.hitori.api.registry.MappedRegistry;
import su.hitori.api.registry.Registry;
import su.hitori.api.registry.RegistryKey;

public abstract class AbstractConveyorWithRegistry<E extends Keyed, R extends Keyed> extends AbstractConveyor<E> {

    protected final MappedRegistry<R> registry;

    public AbstractConveyorWithRegistry(Key key, RegistryKey<R> registryKey) {
        super(key);
        this.registry = new MappedRegistry<>(registryKey);
    }

    public Registry<R> registry() {
        return registry;
    }

}
