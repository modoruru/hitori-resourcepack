package su.hitori.pack.type.block.placement;

import su.hitori.pack.type.block.Orientation;

public final class OrientationProperties {

    private final boolean unlocked;
    private final Orientation def;

    private OrientationProperties(boolean unlocked, Orientation def) {
        this.unlocked = unlocked;
        this.def = def;
    }

    public boolean unlocked() {
        return unlocked;
    }

    public Orientation def() {
        return def;
    }

    public static OrientationProperties createUnlocked() {
        return new OrientationProperties(true, null);
    }

    public static OrientationProperties createLocked(Orientation def) {
        return new OrientationProperties(false, def);
    }

}
