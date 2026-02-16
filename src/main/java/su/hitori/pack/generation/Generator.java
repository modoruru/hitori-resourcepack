package su.hitori.pack.generation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import su.hitori.api.Pair;
import su.hitori.api.module.ModuleDescriptor;
import su.hitori.pack.generation.supplier.GenerationSupplier;

import java.io.File;
import java.util.Optional;

@NotNullByDefault
public interface Generator {

    boolean generate();

    /**
     * returns true if generation process is running, false otherwise
     */
    boolean isGenerating();

    /**
     * @throws IllegalAccessError if generation process is going on.
     */
    <E extends Keyed> @Nullable GenerationConveyor<E> getConveyor(Key key, Class<E> clazz) throws IllegalAccessError;

    default <E extends Keyed> Generator addSupplierToConveyor(Key key, Class<E> clazz, GenerationSupplier<E> supplier) {
        GenerationConveyor<E> conveyor = getConveyor(key, clazz);
        if(conveyor != null) conveyor.addSupplier(supplier);
        return this;
    }

    default <E extends Keyed> Generator addSuppliersToConveyor(Key key, Class<E> clazz, GenerationSupplier<E>... suppliers) {
        GenerationConveyor<E> conveyor = getConveyor(key, clazz);
        if(conveyor != null) conveyor.addSuppliers(suppliers);
        return this;
    }

    <E extends Keyed> void addConveyor(ModuleDescriptor moduleDescriptor, GenerationConveyor<E> conveyor, Class<E> objectType);

    /**
     * returns file and hash if generated, or empty if pack is not generated yet or generating right now
     */
    Optional<Pair<File, String>> getResult();

}
