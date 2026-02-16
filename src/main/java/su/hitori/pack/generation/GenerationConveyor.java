package su.hitori.pack.generation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import su.hitori.pack.generation.supplier.GenerationSupplier;

import java.util.Collection;

public interface GenerationConveyor<E extends Keyed> extends Keyed {

    void removeSupplier(Key key);

    void removeSuppliers(Collection<Key> keys);

    default void removeSuppliers(Key... keys) {
        for (Key key : keys) {
            removeSupplier(key);
        }
    }

    /**
     * adds supplier of objects
     */
    void addSupplier(GenerationSupplier<E> supplier);

    default void addSuppliers(GenerationSupplier<E>... suppliers) {
        for (GenerationSupplier<E> supplier : suppliers) {
            addSupplier(supplier);
        }
    }

    /**
     *  called before generation and should collect all elements to generate
     */
    void collect(ErrorStack errorStack);

    void generate(GenerationContext context);

}
