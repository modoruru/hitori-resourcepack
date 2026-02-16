package su.hitori.pack.type.glyph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public record GlyphSnapshot(Key key, byte[] texture, String path, int ascent, int height) implements Keyed {

    @Override
    public @NotNull Key key() {
        return key;
    }

}
