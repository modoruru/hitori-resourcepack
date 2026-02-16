package su.hitori.pack.type.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import su.hitori.pack.type.block.behaviour.BehaviourProperties;
import su.hitori.pack.type.block.placement.OrientationProperties;
import su.hitori.pack.type.block.placement.PlacementProperties;
import su.hitori.pack.type.block.placement.SolidPlacementProperties;

public final class BlockProperties {

    private final Key key;

    private PlacementProperties placementProperties = SolidPlacementProperties.createCuboid(OrientationProperties.createLocked(Orientation.FLOOR), false, true, 1, 1, 1);
    private BehaviourProperties behaviourProperties;
    private Key itemToDrop;

    public BlockProperties(@NotNull Key key) {
        this.key = key;
    }

    public Key key() {
        return key;
    }

    public BlockProperties placementProperties(PlacementProperties placementProperties) {
        this.placementProperties = placementProperties;
        return this;
    }

    public PlacementProperties placementProperties() {
        return placementProperties;
    }

    public BlockProperties behaviourProperties(BehaviourProperties behaviourProperties) {
        this.behaviourProperties = behaviourProperties;
        return this;
    }

    public BehaviourProperties behaviourProperties() {
        return behaviourProperties;
    }

    public BlockProperties itemToDrop(Key itemToDrop) {
        this.itemToDrop = itemToDrop;
        return this;
    }

    public Key itemToDrop() {
        return itemToDrop;
    }

}
