package su.hitori.pack.type.blueprint;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public final class Blueprint implements Keyed {

    private final Key key;

    // blueprint settings
    private final Vector2i boundingBox;
    private final int interpolationDuration;
    private final int teleportationDuration;

    // nodes

    // variants

    // animations

    public Blueprint(Key key, Vector2i boundingBox, int interpolationDuration, int teleportationDuration) {
        this.key = key;
        this.boundingBox = boundingBox;
        this.interpolationDuration = interpolationDuration;
        this.teleportationDuration = teleportationDuration;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

}
