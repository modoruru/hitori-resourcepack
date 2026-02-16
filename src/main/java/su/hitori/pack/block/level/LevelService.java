package su.hitori.pack.block.level;

import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Task;
import su.hitori.pack.PackModule;
import su.hitori.pack.block.BlockPos;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.player.PlayerBlocksInjection;
import su.hitori.pack.block.protection.CombinedProtectionService;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.seat.SeatPose;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.Model;
import su.hitori.pack.type.block.BlockProperties;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;
import su.hitori.pack.type.block.behaviour.BehaviourProperties;
import su.hitori.pack.type.block.behaviour.BehaviourType;
import su.hitori.pack.type.block.placement.EntityPlacementProperties;
import su.hitori.pack.type.block.placement.OrientationProperties;
import su.hitori.pack.type.block.placement.PlacementProperties;
import su.hitori.pack.type.block.placement.PlacementType;
import su.hitori.pack.type.item.CustomItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static su.hitori.pack.util.MathUtil.toRadians;

public final class LevelService {

    private static final Logger LOGGER = LoggerFactory.instance().create(LevelService.class);
    private static final NamespacedKey
            HITBOX = new NamespacedKey("resourcepack", "hitbox"),
            DISPLAY = new NamespacedKey("resourcepack", "display"),
            ORIGINAL_ITEM = new NamespacedKey("resourcepack", "original_item"),
            ADDITIONAL_DATA = new NamespacedKey("resourcepack", "additional_data");

    private static final int VALIDATE_DELAY_TICKS = 5 * 20;

    private final PackModule packModule;
    private final CombinedProtectionService combinedProtectionService;
    private final ExecutorService executorService;
    private final Registry<@NotNull CustomBlock> blockRegistry;
    private final Registry<@NotNull CustomItem> itemRegistry;
    private final Map<Key, Level> levels;

    private final AtomicBoolean performingCheck;

    private Task validateTask;
    private boolean loaded;

    public LevelService(PackModule packModule, CombinedProtectionService combinedProtectionService, Registry<@NotNull CustomBlock> blockRegistry, Registry<@NotNull CustomItem> itemRegistry) {
        this.packModule = packModule;
        this.combinedProtectionService = combinedProtectionService;
        this.executorService = packModule.executorService();
        this.blockRegistry = blockRegistry;
        this.itemRegistry = itemRegistry;
        this.levels = new HashMap<>();

        this.performingCheck = new AtomicBoolean();
    }

    public void validatePlacedBlocks() {
        if(performingCheck.get()) return;
        executorService.execute(this::internalValidatePlacedBlocks);
    }

    private void internalValidatePlacedBlocks() {
        performingCheck.set(true);

        for (Level level : levels.values()) {
            for (net.minecraft.world.entity.Entity entity : level.getServerLevel().moonrise$getEntityLookup().getAll()) {
                Entity bukkitEntity = entity.getBukkitEntity();
                if(!bukkitEntity.getPersistentDataContainer().has(DISPLAY)) continue;

                Block center = bukkitEntity.getLocation().getBlock();

                BlockState state = level.getState(center.getX(), center.getY(), center.getZ());
                if(state == null) continue;

                CustomBlock customBlock = blockRegistry.get(state.key());
                if(customBlock == null) continue;

                BlockProperties blockProperties = customBlock.blockProperties();
                PlacementProperties placement = blockProperties.placementProperties();
                BehaviourProperties behaviourProperties = blockProperties.behaviourProperties();
                Material expectedBlockType;
                if(placement.type() == PlacementType.SOLID) expectedBlockType = Material.BARRIER;
                else {
                    if(behaviourProperties.type() == BehaviourType.LIGHT_EMITTER && state.additionalData == 1) expectedBlockType = Material.LIGHT;
                    else expectedBlockType = Material.AIR;
                }

                for (Block block : placement.getBlocksAffectedByPlacement(
                        state.direction(),
                        state.orientation(),
                        center
                )) {
                    if(block.getType() != expectedBlockType) {
                        Task.ensureSync(() -> removeCustomBlock(center, true, null, true));
                        break;
                    }
                }

                if(behaviourProperties.type() == BehaviourType.LIGHT_EMITTER) {
                    behaviourProperties.asLightEmitter().place(placement, state.direction(), state.orientation(), center, state.additionalData == 1);
                }
            }
        }

        performingCheck.set(false);
    }

    public Level getLevel(Key key) {
        return levels.get(key);
    }

    public Level getLevel(World world) {
        return getLevel(world.getKey());
    }

    public void loadChunk(Chunk chunk) {
        Level level = getLevel(chunk.getWorld());
        if(level != null) level.loadChunk(chunk.getX(), chunk.getZ());
    }

    public void unloadChunk(Chunk chunk, boolean save) {
        Level level = getLevel(chunk.getWorld());
        if(level != null) level.unloadChunk(chunk.getX(), chunk.getZ(), save);
    }

    public void loadLevel(World world) {
        if(!loaded) return;

        Key key = world.key();
        if(getLevel(world) != null) return;

        Level level = new Level(key);
        levels.put(key, level);
        for (Chunk chunk : world.getLoadedChunks()) {
            level.loadChunk(chunk.getX(), chunk.getZ());
        }
    }

    public void saveLevel(World world) {
        Level level = getLevel(world);
        if(level != null) level.save();
    }

    public void unloadLevel(World world) {
        Level level = getLevel(world);
        if(level != null) level.unload();
    }

    public void attemptToPickup(Player player, Block block, Consumer<Boolean> setCancelled, Consumer<Integer> setSourceSlot) {
        if(block.getType() != Material.BARRIER && block.getType() != Material.AIR) return;

        Level windmillLevel = getLevel(block.getWorld());
        BlockState state = windmillLevel.getState(block.getX(), block.getY(), block.getZ());
        if(state.isEmpty()) return;

        Key customItem = Optional.ofNullable(blockRegistry.get(state.key()))
                .map(CustomBlock::blockProperties)
                .map(BlockProperties::itemToDrop)
                .orElse(null);
        if(customItem == null) return;

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if(customItem.equals(CustomItem.getId(stack).orElse(null))) {
                if(i <= 8) {
                    setCancelled.accept(true);
                    inventory.setHeldItemSlot(i);
                    return;
                }

                setSourceSlot.accept(i);
                return;
            }
        }

        if(player.getGameMode() == GameMode.CREATIVE) {
            CustomItem item = itemRegistry.get(customItem);
            if(item == null) return;

            setCancelled.accept(true);
            Task.runEntity(player, () -> inventory.setItemInMainHand(item.create()), 0L);
        }
    }

    public void load() {
        if(loaded) return;

        Bukkit.getOnlinePlayers().forEach(player -> PlayerBlocksInjection.inject(player, this));

        for (World world : Bukkit.getWorlds()) {
            Key key = world.key();
            Level level = new Level(key);
            levels.put(key, level);

            for (Chunk chunk : world.getLoadedChunks()) {
                level.loadChunk(chunk.getX(), chunk.getZ());
            }
        }

        validateTask = Task.runTaskTimerAsync(
                this::validatePlacedBlocks,
                VALIDATE_DELAY_TICKS * 2,
                VALIDATE_DELAY_TICKS
        );
        loaded = true;
    }

    public void unload() {
        if(!loaded) return;
        loaded = false;

        validateTask.cancel();

        for (Level level : levels.values()) {
            level.unload();
        }
        levels.clear();
    }

    /**
     * handles player rbm on block
     * @return was interaction handled or there's no suitable action to do
     */
    public boolean handlePlayerInteraction(Block block, Player player, EquipmentSlot hand, ItemStack handItem) {
        World world = block.getWorld();
        Level level = getLevel(world);
        if(level == null) return false;

        BlockState state = level.getState(block.getX(), block.getY(), block.getZ());
        if(state.isEmpty()) return false;

        CustomBlock customBlock = blockRegistry.get(state.key());
        if(customBlock == null) return false;

        Block parent;
        if(!state.isChild()) parent = block;
        else {
            BlockPos parentPos = state.parent();
            assert parentPos != null;
            parent = world.getBlockAt(parentPos.x(), parentPos.y(), parentPos.z());
        }

        ItemDisplay display = world.getNearbyEntitiesByType(ItemDisplay.class, parent.getLocation().toCenterLocation(), 0.01)
                .stream()
                .findFirst()
                .orElse(null);

        BehaviourProperties behaviourProperties = customBlock.blockProperties().behaviourProperties();
        BlockState blockState = level.getState(parent.getX(), parent.getY(), parent.getZ());
        boolean consumed = behaviourProperties.onPlayerInteract(
                customBlock,
                block,
                state,
                parent,
                blockState,
                display,
                player,
                hand,
                handItem
        );
        if(consumed) behaviourProperties.updateStateFromAdditionalData(customBlock, parent, blockState, display);
        return consumed;
    }

    public boolean placeCustomBlock(CustomBlock customBlock, Direction inputDirection, Orientation inputOrientation, Block center, boolean ignoreEntities, @Nullable ItemStack placedFrom, Player whoPlaced) {
        PlacementProperties placementProperties = customBlock.blockProperties().placementProperties();

        if(whoPlaced != null && !whoPlaced.getGameMode().isInvulnerable() && !placementProperties.survivalFriendly())
            return false;

        Direction direction;
        Orientation orientation;

        if(!placementProperties.unlockedSubDirections() && inputDirection.subDirection()) direction = Direction.fromYaw(inputDirection.yaw(), false);
        else direction = inputDirection;

        OrientationProperties orientationProperties = placementProperties.orientationProperties();
        orientation = orientationProperties.unlocked() ? inputOrientation : orientationProperties.def();

        World world = center.getWorld();
        Level level = getLevel(world);

        if(!placementProperties.canBePlaced(direction, orientation, center, center, level, ignoreEntities) || !combinedProtectionService.isAbleToBreak(center, whoPlaced)) return false;

        PlacementType placementType = placementProperties.type();

        if(placementType == PlacementType.ENTITY) {
            EntityPlacementProperties entityPlacement = placementProperties.asEntity();
            Location entityLocation = entityPlacement.getPositionForEntity(
                    direction,
                    orientation,
                    center.getLocation().add(.5, 0, .5)
            );

            Interaction interaction = world.spawn(entityLocation, Interaction.class);
            interaction.getPersistentDataContainer().set(HITBOX, PersistentDataType.BOOLEAN, true);
            interaction.setInteractionWidth((float) entityPlacement.size());
            interaction.setInteractionHeight((float) entityPlacement.size());
        }

        Collection<Block> blocks = placementProperties.getBlocksAffectedByPlacement(direction, orientation, center);
        BlockPos centerPos = new BlockPos(center);
        int additionalDataInitial = placedFrom != null ? placedFrom.getPersistentDataContainer().getOrDefault(ADDITIONAL_DATA, PersistentDataType.INTEGER, 0) : 0;

        for (Block block : blocks) {
            if(placementType == PlacementType.SOLID) block.setType(Material.BARRIER);

            boolean isCenter = BlockPos.equals(center, block);
            level.setState(
                    block.getX(), block.getY(), block.getZ(),
                    new BlockState(
                            customBlock.key(),
                            direction,
                            orientation,
                            isCenter
                                    ? null
                                    : centerPos,
                            isCenter ? additionalDataInitial : 0
                    )
            );
        }

        ItemDisplay display = world.spawn(
                center.getLocation().toCenterLocation(),
                ItemDisplay.class
        );
        PersistentDataContainer data = display.getPersistentDataContainer();
        data.set(DISPLAY, PersistentDataType.BOOLEAN, true);

        if(placedFrom != null) {
            ItemStack originalItem = placedFrom.clone();
            originalItem.setAmount(1);
            data.set(ORIGINAL_ITEM, ItemSourceDataType.TYPE, originalItem);
        }

        BehaviourProperties behaviourProperties = customBlock.blockProperties().behaviourProperties();

        ItemModel itemModel;
        switch (behaviourProperties.type()) {
            case DEFAULT -> itemModel = behaviourProperties.asDefault().model();
            case LIGHT_EMITTER -> itemModel = behaviourProperties.asLightEmitter().disabled();
            case MODEL_SEQUENCE -> itemModel = behaviourProperties.asModelSequence().sequence().getFirst();
            case SEAT -> itemModel = behaviourProperties.asSeat().model();
            default -> {
                return false;
            }
        }

        display.setTransformationMatrix(createPose(
                orientation,
                direction,
                placementProperties.itemFrameLikeDisplay()
                        ? itemModel.modelTransform().orElse(Model.ModelTransform.EMPTY)
                        : Model.ModelTransform.EMPTY
        ));

        display.setItemStack(new ItemStack(Material.POPPED_CHORUS_FRUIT){{
            editMeta(meta -> meta.setItemModel(itemModel.resolve()));
        }});

        if(additionalDataInitial != 0) behaviourProperties.updateStateFromAdditionalData(
                customBlock,
                center,
                level.getState(center.getX(), center.getY(), center.getZ()),
                display
        );

        return true;
    }

    public static Matrix4f createPose(Orientation orientation, Direction direction, Model.ModelTransform modelTransform) {
        Matrix4f pose = new Matrix4f();

        // rotation applied from placement (orientation and direction)
        Vector3f placementRotation = new Vector3f(orientation.rotationMod());
        placementRotation.y += (orientation == Orientation.CEILING ? direction.opposite() : direction).yRotationMod();
        placementRotation.mul((float) Math.PI / 180f);

        pose.rotate(new Quaternionf().rotationYXZ(
                placementRotation.y,
                placementRotation.x,
                placementRotation.z
        ));
        pose.translate(modelTransform.translation());
        pose.rotate(new Quaternionf().rotationXYZ(
                toRadians(modelTransform.rotation().x()),
                toRadians(modelTransform.rotation().y()),
                toRadians(modelTransform.rotation().z())
        ));
        pose.scale(modelTransform.scale());

        return pose;
    }

    public boolean isHitboxEntity(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(HITBOX, PersistentDataType.BOOLEAN, false);
    }

    public boolean removeCustomBlock(Block child, boolean drop, Player player) {
        return removeCustomBlock(child, drop, player, false);
    }

    private boolean removeCustomBlock(Block child, boolean drop, Player player, boolean forceRemoved) {
        World world = child.getWorld();
        Level level = getLevel(world);
        if(level == null) return false;

        BlockState state = level.getState(child.getX(), child.getY(), child.getZ());
        if(state.isEmpty()) return false;

        if(player != null && !combinedProtectionService.isAbleToBreak(child, player)) return false;

        BlockState parentState;
        Block parent;
        if(!state.isChild()) {
            parentState = state;
            parent = child;
        }
        else {
            BlockPos parentPos = state.parent();
            assert parentPos != null;
            parentState = level.getState(parentPos.x(), parentPos.y(), parentPos.z());
            parent = world.getBlockAt(parentPos.x(), parentPos.y(), parentPos.z());
        }

        CustomBlock customBlock = blockRegistry.get(parentState.key());
        if(customBlock == null) return false;

        if(player != null && !player.getGameMode().isInvulnerable() && !customBlock.blockProperties().placementProperties().survivalFriendly())
            return false;

        Direction direction = parentState.direction();
        Orientation orientation = parentState.orientation();

        BlockProperties blockProperties = customBlock.blockProperties();
        PlacementProperties placement = blockProperties.placementProperties();
        BehaviourProperties behaviour = blockProperties.behaviourProperties();

        PoseService poseService = packModule.poseService();
        for (Block block : placement.getBlocksAffectedByPlacement(
                direction,
                orientation,
                parent
        )) {
            level.setState(
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    null
            );

            if(placement.type() == PlacementType.SOLID && block.getType() == Material.BARRIER) block.setType(Material.AIR);

            SeatPose seatPose = poseService.getSeatPoseOnBlock(block);
            if(seatPose != null) poseService.removeSeatPose(seatPose, true);
        }

        if(behaviour.type() == BehaviourType.LIGHT_EMITTER)
            behaviour.asLightEmitter().place(placement, direction, orientation, parent, false);

        if(forceRemoved) {
            LOGGER.warning(String.format(
                    "Block with center on [%s %s %s] with id \"%s\" was removed because of interrupted structure (probably replacing block with command)",
                    parent.getX(),
                    parent.getY(),
                    parent.getZ(),
                    customBlock.key().asString()
            ));
        }

        ItemDisplay itemDisplay = world.getNearbyEntitiesByType(ItemDisplay.class, parent.getLocation().toCenterLocation(), 0.01)
                .stream()
                .findFirst()
                .orElse(null);

        if(drop) {
            ItemStack toDrop = null;
            if(itemDisplay == null || (toDrop = itemDisplay.getPersistentDataContainer().get(ORIGINAL_ITEM, ItemSourceDataType.TYPE)) == null) {
                CustomItem item = Optional.ofNullable(blockProperties.itemToDrop())
                        .map(itemRegistry::get)
                        .orElse(null);

                if(item != null) toDrop = item.create();
            }

            if(toDrop != null) {
                toDrop.editPersistentDataContainer(data -> data.set(ADDITIONAL_DATA, PersistentDataType.INTEGER, parentState.additionalData));
                world.dropItemNaturally(parent.getLocation().toCenterLocation(), toDrop);
            }
        }

        if(itemDisplay != null) itemDisplay.remove();

        if(placement.type() == PlacementType.ENTITY) {
            Location location = placement.asEntity().getPositionForEntity(
                    direction,
                    orientation,
                    parent.getLocation().add(.5, 0, .5)
            );
            world.getNearbyEntitiesByType(Interaction.class, location, 0.01)
                    .stream()
                    .filter(entity -> entity.getLocation().equals(location))
                    .findFirst()
                    .ifPresent(Entity::remove);
        }

        return true;
    }

}
