package su.hitori.pack.type.blueprint;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import su.hitori.pack.type.blueprint.animation.Animation;
import su.hitori.pack.type.blueprint.node.NodeData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Blueprint implements Keyed {

    private final Key key;

    // blueprint settings
    private final Vector2i boundingBox;
    private final int interpolationDuration;
    private final int teleportationDuration;

    private final Map<UUID, NodeData> nodes;
    private final Map<String, UUID> nodesByName;

    private final Map<UUID, Animation> animations;
    private final Map<String, UUID> animationsByName;

    public Blueprint(Key key, Vector2i boundingBox, int interpolationDuration, int teleportationDuration, Map<UUID, NodeData> nodes, Map<UUID, Animation> animations) {
        this.key = key;
        this.boundingBox = boundingBox;
        this.interpolationDuration = interpolationDuration;
        this.teleportationDuration = teleportationDuration;

        this.nodes = Map.copyOf(nodes);
        this.nodesByName = new HashMap<>();
        for (NodeData value : nodes.values()) {
            nodesByName.put(value.name, value.uuid);
        }

        this.animations = Map.copyOf(animations);
        this.animationsByName = new HashMap<>();

        for (Animation value : animations.values()) {
            animationsByName.put(value.name.toLowerCase(), value.uuid);
        }
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public Vector2i boundingBox() {
        return new Vector2i(boundingBox);
    }

    public int interpolationDuration() {
        return interpolationDuration;
    }

    public int teleportationDuration() {
        return teleportationDuration;
    }

    /**
     * @return mutable copy of all nodes
     */
    public Map<UUID, NodeData> nodes() {
        return new HashMap<>(nodes);
    }

    public Optional<NodeData> node(String nodeName) {
        return Optional.ofNullable(nodesByName.get(nodeName)).map(nodes::get);
    }

    public Optional<NodeData> node(UUID nodeUUID) {
        return Optional.ofNullable(nodes.get(nodeUUID));
    }

    public Map<UUID, Animation> animations() {
        return new HashMap<>(animations);
    }

    public Optional<Animation> animation(String animationName) {
        return Optional.ofNullable(animationsByName.get(animationName)).map(animations::get);
    }

    public Optional<Animation> animation(UUID animationUUID) {
        return Optional.ofNullable(animations.get(animationUUID));
    }

}
