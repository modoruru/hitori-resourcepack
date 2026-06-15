package su.hitori.pack.type.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.hitori.pack.type.block.behaviour.BehaviourProperties;
import su.hitori.pack.type.block.placement.OrientationProperties;
import su.hitori.pack.type.block.placement.PlacementProperties;
import su.hitori.pack.type.block.placement.SolidPlacementProperties;

public final class BlockProperties {

    private final Key key;

    private @Nullable PlacementProperties placementProperties = SolidPlacementProperties.createCuboid(OrientationProperties.createLocked(Orientation.FLOOR), false, true, 1, 1, 1);
    private @Nullable BehaviourProperties behaviourProperties;
    private @Nullable Key itemToDrop;

    public BlockProperties(@NotNull Key key) {
        this.key = key;
    }

    public Key key() {
        return key;
    }

    public BlockProperties placementProperties(@Nullable PlacementProperties placementProperties) {
        this.placementProperties = placementProperties;
        return this;
    }

    public @Nullable PlacementProperties placementProperties() {
        return placementProperties;
    }

    public BlockProperties behaviourProperties(@Nullable BehaviourProperties behaviourProperties) {
        this.behaviourProperties = behaviourProperties;
        return this;
    }

    public @Nullable BehaviourProperties behaviourProperties() {
        return behaviourProperties;
    }

    public BlockProperties itemToDrop(@Nullable Key itemToDrop) {
        this.itemToDrop = itemToDrop;
        return this;
    }

    public @Nullable Key itemToDrop() {
        return itemToDrop;
    }

}
