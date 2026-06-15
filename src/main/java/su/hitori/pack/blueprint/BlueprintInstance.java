package su.hitori.pack.blueprint;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.hitori.api.nms.NMSUtil;
import su.hitori.api.util.Task;
import su.hitori.pack.blueprint.node.BodyNodeInstance;
import su.hitori.pack.blueprint.node.CameraNodeInstance;
import su.hitori.pack.blueprint.node.NodeInstance;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.animation.Animation;
import su.hitori.pack.type.blueprint.animation.Frame;
import su.hitori.pack.type.blueprint.node.LocatorNodeData;
import su.hitori.pack.type.blueprint.node.NodeData;

import java.util.*;
import java.util.concurrent.ExecutorService;

public final class BlueprintInstance {

    private final Blueprints blueprints;
    private final Blueprint blueprint;
    private final ExecutorService executorService;

    private final Map<UUID, NodeInstance<? extends NodeData, ? extends Entity>> nodes;
    private final Map<UUID, Transformation> locators;
    private final Map<Player, Observer> observers;
    private final Map<Integer, GameProfile> charactersSkins;

    private Display.ItemDisplay mainEntity;
    private Location location;

    private Animation animation;
    private int animationTick;
    private boolean paused;
    private Task tickTask;

    private boolean shown, destroyed;

    BlueprintInstance(Blueprints blueprints, Blueprint blueprint, ExecutorService executorService) {
        this.blueprints = blueprints;
        this.blueprint = blueprint;
        this.executorService = executorService;

        this.nodes = new HashMap<>();
        this.locators = new HashMap<>();
        this.observers = new HashMap<>();
        this.charactersSkins = new HashMap<>();

        for (Integer character : blueprint.characters()) {
            charactersSkins.put(character, null);
        }

        this.tickTask = Task.runTaskTimerAsync(this::tick, 0L, 1L);
    }

    public Blueprint blueprint() {
        return blueprint;
    }

    public org.bukkit.util.Transformation locatorTransformation(UUID locatorUuid) {
        Transformation transformation = locators.get(locatorUuid);
        if(transformation == null) return null;

        return new org.bukkit.util.Transformation(
                new Vector3f(transformation.getTranslation()),
                new Quaternionf(transformation.getLeftRotation()),
                new Vector3f(transformation.getScale()),
                new Quaternionf(transformation.getRightRotation())
        );
    }

    public BlueprintInstance assignSkin(int characterId, PlayerProfile playerProfile) {
        if(destroyed || !charactersSkins.containsKey(characterId)) return this;

        GameProfile gameProfile;
        if(playerProfile == null) gameProfile = null;
        else gameProfile = ((CraftPlayerProfile) playerProfile).getGameProfile();
        charactersSkins.put(characterId, gameProfile);

        if(shown) {
            Map<Integer, ClientboundSetEntityDataPacket> updated = new HashMap<>();
            for (NodeInstance<? extends NodeData, ? extends Entity> nodeInstance : nodes.values()) {
                if(!(nodeInstance instanceof BodyNodeInstance bodyNodeInstance)) continue;

                if(bodyNodeInstance.nodeData().characterId == characterId) {
                    bodyNodeInstance.assignSkin(gameProfile);
                    Display.ItemDisplay entity = bodyNodeInstance.entity();
                    ClientboundSetEntityDataPacket setEntityDataPacket = BlueprintUtil.createSetEntityDataPacket(entity, true);
                    if(setEntityDataPacket == null) continue;

                    updated.put(entity.getId(), setEntityDataPacket);
                }
            }

            for (Observer observer : observers.values()) {
                for (Map.Entry<Integer, ClientboundSetEntityDataPacket> entry : updated.entrySet()) {
                    if(observer.entitySent.contains(entry.getKey()))
                        observer.sendPacket(entry.getValue());
                }
            }
        }

        return this;
    }

    public BlueprintInstance setCamera(Player observerPlayer, UUID cameraNodeUUID) {
        Observer observer;
        if(destroyed || (observer = observers.get(observerPlayer)) == null || observer.cameraUuid != null) return this;

        var nodeInstance = nodes.get(cameraNodeUUID);
        if(!(nodeInstance instanceof CameraNodeInstance cameraNodeInstance)) return this;

        observer.cameraUuid = cameraNodeUUID;
        observer.originalGameType = observer.serverPlayer.gameMode();
        observer.serverPlayer.setGameMode(GameType.SPECTATOR);

        observer.sendPacket(new ClientboundSetCameraPacket(cameraNodeInstance.entity()));

        return this;
    }

    public BlueprintInstance removeCamera(Player observerPlayer) {
        Observer observer;
        if(destroyed || (observer = observers.get(observerPlayer)) == null || observer.cameraUuid == null) return this;

        observer.sendPacket(new ClientboundSetCameraPacket(observer.serverPlayer));
        observer.serverPlayer.setGameMode(observer.originalGameType);

        observer.cameraUuid = null;
        observer.originalGameType = null;

        return this;
    }

    public BlueprintInstance addObserver(Player player) {
        if(destroyed || observers.containsKey(player)) return this;
        Observer observer = new Observer(new HashSet<>(), NMSUtil.asNMS(player));
        observers.put(player, observer);

        if(!shown) return this;

        observer.sendPacket(createShowPayload(observer));
        return this;
    }

    public boolean observing(Player player) {
        if(destroyed) return false;
        return observers.containsKey(player);
    }

    private ClientboundBundlePacket createShowPayload(Observer observer) {
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        List<Integer> passengers = new ArrayList<>();

        packets.add(BlueprintUtil.createEntityPacket(mainEntity));
        packets.add(BlueprintUtil.createSetEntityDataPacket(mainEntity, false));
        observer.entitySent.add(mainEntity.getId());

        for (NodeInstance<? extends NodeData, ? extends Entity> nodeInstance : nodes.values()) {
            int nodeEntityId = nodeInstance.entity().getId();

            packets.addAll(nodeInstance.createAddPackets());
            passengers.add(nodeEntityId);
            observer.entitySent.add(nodeEntityId);
        }

        packets.add(BlueprintUtil.createSetPassengersPacket(mainEntity.getId(), passengers));

        return new ClientboundBundlePacket(packets);
    }

    private ClientboundRemoveEntitiesPacket createHidePayload(Observer observer) {
        int[] entities = new int[observer.entitySent.size() + 1];
        entities[0] = mainEntity.getId();
        
        var iterator = observer.entitySent.iterator();
        for (int i = 0; i < entities.length - 1; i++) {
            entities[i + 1] = iterator.next();
        }

        observer.entitySent.clear();

        return new ClientboundRemoveEntitiesPacket(entities);
    }

    public BlueprintInstance removeObserver(Player player) {
        if(destroyed) return this;

        Observer observer = observers.remove(player);
        if(observer == null) return this;
        observer.sendPacket(createHidePayload(observer));
        return this;
    }

    // move to the other location
    public BlueprintInstance move(Location location) {
        if(destroyed || location == null) return this;
        this.location = location;
        if(!shown) return this;

        Vec3 position = new Vec3(location.getX(), location.getY(), location.getZ());
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        if(!serverLevel.dimension().identifier().equals(mainEntity.level().dimension().identifier())) {
            for (Observer observer : observers.values()) {
                observer.sendPacket(createHidePayload(observer));
            }

            for (NodeInstance<? extends NodeData, ? extends Entity> nodeInstance : nodes.values()) {
                Entity entity = nodeInstance.entity();
                entity.setLevel(serverLevel);
                entity.setPos(position);
            }
            
            mainEntity.setLevel(serverLevel);
            mainEntity.setPos(position);

            for (Observer observer : observers.values()) {
                observer.sendPacket(createShowPayload(observer));
            }

            return this;
        }

        mainEntity.setLevel(serverLevel);
        mainEntity.setPos(position);

        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(
                mainEntity.getId(),
                PositionMoveRotation.of(mainEntity),
                Set.of(),
                false
        );
        for (Observer observer : observers.values()) {
            observer.sendPacket(packet);
        }

        return this;
    }

    // show up the blueprint
    public BlueprintInstance show() {
        if(destroyed || shown) return this;
        shown = true;

        Vec3 position = new Vec3(location.getX(), location.getY(), location.getZ());
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        mainEntity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);
        mainEntity.setPos(position);

        for (NodeData nodeData : blueprint.nodes().values()) {
            if(nodeData instanceof LocatorNodeData)
                locators.put(nodeData.uuid, nodeData.transformation);

            NodeInstance<? extends NodeData, ? extends Entity> nodeInstance = NodeInstance.create(this, nodeData);
            if(nodeInstance == null) continue;

            nodeInstance.summon(serverLevel, position);
            if(nodeInstance instanceof BodyNodeInstance bodyNodeInstance) {
                GameProfile gameProfile = charactersSkins.get(bodyNodeInstance.nodeData().characterId);
                if(gameProfile != null) bodyNodeInstance.assignSkin(gameProfile);
            }

            nodes.put(nodeData.uuid, nodeInstance);
        }

        for (Observer observer : observers.values()) {
            observer.sendPacket(createShowPayload(observer));
        }

        return this;
    }

    // hide the blueprint
    public BlueprintInstance hide() {
        if(destroyed || !shown) return this;

        for (Observer observer : observers.values()) {
            observer.sendPacket(createHidePayload(observer));
        }

        mainEntity = null;
        nodes.clear();

        return this;
    }

    // cleanup all data so this instance no longer can be reused
    public BlueprintInstance destroy() {
        if(destroyed) return this;
        if(shown) hide();

        observers.clear();
        charactersSkins.clear();
        location = null;
        animation = null;
        animationTick = -1;
        if(tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        blueprints.remove(this);
        destroyed = true;

        return this;
    }

    public boolean destroyed() {
        return destroyed;
    }

    public BlueprintInstance play(Animation animation) {
        if(destroyed || this.animation != null || animation == null) return this;
        if(blueprint.animation(animation.uuid).isEmpty()) return this;

        this.animation = animation;
        this.animationTick = 0;

        return this;
    }

    public BlueprintInstance togglePause() {
        if(destroyed) return this;
        paused = !paused;
        return this;
    }

    public BlueprintInstance stop() {
        if(destroyed || animation == null) return this;

        animation = null;
        animationTick = -1;

        return this;
    }

    private void tick() {
        if(!shown || animation == null || paused) return;

        if(animationTick > animation.duration) switch (animation.loopMode) {
            case LOOP -> animationTick = 0;
            case ONCE -> {
                setFrame(0, true);
                animation = null;
                animationTick = -1;
                return;
            }
            case HOLD -> {
                animation = null;
                animationTick = -1;
                return;
            }
        }

        setFrame(animationTick++, false);
    }

    private void setFrame(int frameTick, boolean useInitialTransformationIfAbsent) {
        Optional<Frame> optionalFrame = animation.getFrame(frameTick);
        if(optionalFrame.isEmpty()) return;

        Frame frame = optionalFrame.get();

        Map<UUID, Transformation> nodeTransformations = frame.nodeTransformations();
        for (NodeInstance<? extends NodeData, ? extends Entity> nodeInstance : nodes.values()) {
            Transformation transformation = nodeTransformations.get(nodeInstance.uuid());
            if(transformation == null) {
                if(!useInitialTransformationIfAbsent)
                    continue;

                transformation = nodeInstance.nodeData().transformation;
            }

            nodeInstance.applyTransformation(transformation);

            Entity entity = nodeInstance.entity();
            ClientboundSetEntityDataPacket setEntityDataPacket = BlueprintUtil.createSetEntityDataPacket(entity, true);
            if(setEntityDataPacket == null) continue;

            for (Observer observer : observers.values()) {
                if(observer.entitySent.contains(entity.getId()))
                    observer.sendPacket(setEntityDataPacket);
            }
        }

        for (UUID locatorUuid : locators.keySet()) {
            Transformation transformation = nodeTransformations.get(locatorUuid);
            if(transformation == null) {
                if(!useInitialTransformationIfAbsent)
                    continue;

                transformation = blueprint.node(locatorUuid)
                        .map(nodeData -> nodeData.transformation)
                        .orElseThrow();
            }

            locators.put(locatorUuid, transformation);
        }
    }

}
