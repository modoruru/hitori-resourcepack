package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.util.Pipeline;
import su.hitori.pack.generation.ErrorStack;
import su.hitori.pack.generation.GenerationConveyor;
import su.hitori.pack.generation.supplier.GenerationSupplier;

import java.util.*;

public abstract class AbstractConveyor<E extends Keyed> implements GenerationConveyor<E> {

    private final Key key;

    protected final Pipeline<GenerationSupplier<E>> suppliers;
    protected final Set<GenerationSupplier<E>> toAdd;

    protected final Map<Key, E> snapshots;

    protected boolean collectingOrGenerating;

    public AbstractConveyor(Key key) {
        this.key = key;
        this.suppliers = new Pipeline<>();
        this.toAdd = new HashSet<>();
        this.snapshots = new HashMap<>();
    }

    @Override
    public final void addSupplier(GenerationSupplier<E> supplier) {
        if(collectingOrGenerating) toAdd.add(supplier);
        else suppliers.addLast(supplier.key(), supplier);
    }

    @Override
    public void collect(ErrorStack errorStack) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        // first - remove old one
        suppliers.stream().forEach(supplier -> {
            if(!supplier.isActive()) suppliers.remove(supplier.key());
        });

        if(!toAdd.isEmpty()) {
            for (GenerationSupplier<E> supplier : toAdd) {
                if(!supplier.isActive()) continue;
                suppliers.addLast(supplier.key(), supplier);
            }
            toAdd.clear();
        }

        snapshots.clear();
        suppliers.forEach(supplier -> {
            Collection<E> supplied = supplier.supply();
            if(supplied == null || supplied.isEmpty()) return;

            for (E keyed : supplied) {
                Key key = keyed.key();
                if(this.snapshots.containsKey(key)) {
                    new RuntimeException("supplier " + supplier.key().asString() + " supplied item with existing id").printStackTrace();
                    return;
                }

                this.snapshots.put(key, keyed);
            }
        });

        collectingOrGenerating = false;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public void removeSupplier(Key key) {
        if(collectingOrGenerating) return;
        suppliers.remove(key);
    }

    @Override
    public void removeSuppliers(Collection<Key> keys) {
        if(collectingOrGenerating) return;
        for (Key key : keys) {
            suppliers.remove(key);
        }
    }

}
