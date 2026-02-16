package su.hitori.pack.type;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Sound(Key key, byte[] opusData, Optional<String> subtitleTranslatableKey) implements Keyed {

    @Override
    public @NotNull Key key() {
        return key;
    }

}
