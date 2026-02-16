package su.hitori.pack.generation.supplier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import su.hitori.api.module.ModuleDescriptor;

import java.util.Collection;
import java.util.function.Supplier;

public interface GenerationSupplier<E extends Keyed> extends Keyed {

    boolean isActive();

    Collection<E> supply();

    static <E extends Keyed> ModuleSupplier<E> moduleSupplier(Key key, ModuleDescriptor descriptor, Supplier<Collection<E>> supplier) {
        return new ModuleSupplier<>(key, descriptor, supplier);
    }

    static <E extends Keyed> ModuleSupplier<E> moduleSupplier(ModuleDescriptor moduleDescriptor, Supplier<Collection<E>> supplier) {
        return new ModuleSupplier<>(moduleDescriptor, supplier);
    }

}
