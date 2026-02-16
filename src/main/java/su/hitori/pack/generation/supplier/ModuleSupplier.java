package su.hitori.pack.generation.supplier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.module.ModuleDescriptor;

import java.util.Collection;
import java.util.function.Supplier;

public final class ModuleSupplier<E extends Keyed> implements GenerationSupplier<E> {

    private final Key key;
    private final ModuleDescriptor moduleDescriptor;
    private final Supplier<Collection<E>> supplier;
    private boolean forceDisabled;

    public ModuleSupplier(ModuleDescriptor moduleDescriptor, Supplier<Collection<E>> supplier) {
        this(moduleDescriptor.key(), moduleDescriptor, supplier);
    }

    public ModuleSupplier(Key key, ModuleDescriptor moduleDescriptor, Supplier<Collection<E>> supplier) {
        this.key = key;
        this.moduleDescriptor = moduleDescriptor;
        this.supplier = supplier;
    }

    @Override
    public boolean isActive() {
        if(forceDisabled) return false;

        if(!moduleDescriptor.isEnabled() && !moduleDescriptor.isEnabling()) {
            forceDisabled = true;
            return false;
        }

        return true;
    }

    @Override
    public Collection<E> supply() {
        return supplier.get();
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

}
