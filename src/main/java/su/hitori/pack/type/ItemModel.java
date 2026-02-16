package su.hitori.pack.type;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import su.hitori.api.util.Either;

import java.util.Optional;

public record ItemModel(Key key, Either<NamespacedKey, Model> directItemModelOrModel) implements Keyed {

    public Optional<Model.ModelTransform> modelTransform() {
        return directItemModelOrModel.secondOptional()
                .flatMap(Model::itemFrameLikeDisplay);
    }

    public NamespacedKey resolve() {
        return directItemModelOrModel.firstOptional()
                .orElseGet(() -> new NamespacedKey("hitori", String.format("%s/%s", key.namespace(), key.value())));
    }

}