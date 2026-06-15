package su.hitori.pack.type.glyph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public record GlyphSnapshot(Key key, byte[] texture, String path, int ascent, int height) implements Keyed {

    @Override
    public Key key() {
        return key;
    }

}
