package su.hitori.pack.type.glyph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record Glyph(GlyphSnapshot glyphSnapshot, int index, boolean temp) implements Keyed {

    private static final Pattern PATTERN = Pattern.compile("E(?<code>\\d{3})");

    public static boolean isGlyphSymbol(char ch) {
        return PATTERN.matcher(String.format("%04X", (int) ch)).find();
    }

    public String getSymbol() {
        return new String(Character.toChars(Integer.parseInt(Integer.toHexString(index), 16)));
    }

    @Override
    public @NotNull Key key() {
        return glyphSnapshot.key();
    }
}