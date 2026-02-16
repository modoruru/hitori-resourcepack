package su.hitori.pack;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Text;
import su.hitori.pack.impl.GlyphConveyor;
import su.hitori.pack.type.glyph.Glyph;

import java.util.UUID;

final class TextSupport {

    private final Registry<@NotNull Glyph> glyphRegistry;

    private UUID tagResolverUid;

    TextSupport(Registry<@NotNull Glyph> glyphRegistry) {
        this.glyphRegistry = glyphRegistry;
    }

    public void load() {
        tagResolverUid = Text.addResolver(
                TagResolver.builder()
                        .tag("shift", (queue, _) -> {
                            if(!queue.hasNext()) return null;
                            int shift;
                            try {shift = Integer.parseInt(queue.peek().value());}
                            catch (Exception e) {return null;}

                            if(shift == 0) return Tag.selfClosingInserting(Component.empty());

                            return Tag.selfClosingInserting(Component.text(computeOffset(Math.abs(shift), shift >= 0)));
                        })
                        .tag("glyph", (queue, _) -> {
                            try {
                                if(!queue.hasNext()) return null;
                                String firstPart = queue.pop().value();
                                String secondPart = queue.hasNext() ? queue.peek().value() : null;
                                return glyphRegistry.getOptional(secondPart == null ? Key.key(firstPart) : Key.key(firstPart, secondPart))
                                        .map(glyph -> Tag.selfClosingInserting(Component.text(glyph.getSymbol())))
                                        .orElse(null);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .build()
        );
    }

    private String computeOffset(int length, boolean positiveOffset) {
        StringBuilder builder = new StringBuilder();
        int remainder = length;
        for (int i = GlyphConveyor.SHIFTS.length - 1; i >= 0; i--) {
            int value = GlyphConveyor.SHIFTS[i];
            if (remainder >= value) {
                remainder -= value;
                builder.append(
                        glyphRegistry.getOptional(Key.key((positiveOffset ? "shift_" : "neg_shift_") + value))
                                .map(Glyph::getSymbol)
                                .orElse("")
                );
            }
        }
        return builder.toString();
    }

    public void unload() {
        Text.removeResolver(tagResolverUid);
        tagResolverUid = null;
    }

}
