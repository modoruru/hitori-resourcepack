package su.hitori.pack.pose.lie;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import su.hitori.api.nms.NMSUtil;
import su.hitori.api.util.Task;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.seat.SeatPose;
import su.hitori.pack.pose.seat.SeatPoseEntity;

import java.util.*;

public final class LyingPose {
    private static final EntityDataAccessor<OptionalInt> LEFT_SHOULDER =
            EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(19);
    private static final EntityDataAccessor<OptionalInt> RIGHT_SHOULDER =
            EntityDataSerializers.OPTIONAL_UNSIGNED_INT.createAccessor(20);
    private static final EntityDataAccessor<Byte> MAIN_HAND =
            EntityDataSerializers.BYTE.createAccessor(8);
    private static final EntityDataAccessor<Byte> SKIN_PARTS =
            EntityDataSerializers.BYTE.createAccessor(16);

    private final SeatPose seatPose;
    private final Player player;
    private final ServerPlayer serverPlayer;
    private final ServerPlayer npcPlayer;
    private final LyingPoseEntity hideNameEntity;

    private final Location fakeBedLocation;
    private final Block fakeBedBlock;
    private final BlockPos fakeBedBlockPos;
    private final double height;
    private final int renderRange;

    private ClientboundBlockUpdatePacket bedPacket;
    private ClientboundPlayerInfoUpdatePacket addNpcInfoPacket;
    private ClientboundPlayerInfoRemovePacket removeNpcInfoPacket;
    private ClientboundRemoveEntitiesPacket removeNpcPacket;
    private ClientboundAddEntityPacket createNpcPacket;
    private ClientboundTeleportEntityPacket teleportNpcPacket;

    private Set<Player> nearbyPlayers = new HashSet<>();
    private ClientboundBundlePacket initializationBundle;
    private NonNullList<ItemStack> equipmentCache;
    private ItemStack mainHandCache;

    public LyingPose(SeatPose seatPose) {
        this.seatPose = seatPose;
        this.player = (Player) seatPose.getRider();
        this.serverPlayer = ((CraftPlayer) player).getHandle();
        this.renderRange = player.getWorld().getSimulationDistance() * 16;

        this.fakeBedLocation = createFakeBedLocation();
        this.fakeBedBlock = fakeBedLocation.getBlock();
        this.fakeBedBlockPos = new BlockPos(
                fakeBedLocation.getBlockX(),
                fakeBedLocation.getBlockY(),
                fakeBedLocation.getBlockZ()
        );

        this.npcPlayer = createNPC();
        this.height = seatPose.getLocation().getY() + PoseService.BASE_OFFSET;

        positionNPC();
        initializePackets();
        this.hideNameEntity = new LyingPoseEntity(player.getLocation());
    }

    public void playAnimation(int animationId) {
        ClientboundAnimatePacket animatePacket = new ClientboundAnimatePacket(npcPlayer, animationId);
        for(Player nearbyPlayer : nearbyPlayers) sendPacket(nearbyPlayer, animatePacket);
    }

    public SeatPose seatPose() {
        return seatPose;
    }

    public Player player() {
        return player;
    }

    public ServerPlayer serverPlayer() {
        return serverPlayer;
    }

    private Location createFakeBedLocation() {
        Location seatLocation = seatPose.getLocation();
        Location bedLocation = seatLocation.clone();
        bedLocation.setY(bedLocation.getWorld().getMinHeight());
        return bedLocation;
    }

    private void positionNPC() {
        Location seatLocation = seatPose.getLocation();
        double scale = serverPlayer.getScale();
        double offset = height + 0.1125d * scale;

        npcPlayer.absSnapTo(
                seatLocation.getX(),
                offset,
                seatLocation.getZ(),
                0f,
                0f
        );
    }

    private void initializePackets() {
        Direction direction = calculateDirection();

        bedPacket = createBedPacket(direction);
        addNpcInfoPacket = createAddNpcInfoPacket();
        removeNpcInfoPacket = createRemoveNpcInfoPacket();
        removeNpcPacket = new ClientboundRemoveEntitiesPacket(npcPlayer.getId());
        createNpcPacket = new ClientboundAddEntityPacket(
                npcPlayer.getId(),
                npcPlayer.getUUID(),
                npcPlayer.getX(),
                npcPlayer.getY(),
                npcPlayer.getZ(),
                npcPlayer.getXRot(),
                npcPlayer.getYRot(),
                npcPlayer.getType(),
                0,
                npcPlayer.getDeltaMovement(),
                npcPlayer.getYHeadRot()
        );
        teleportNpcPacket = new ClientboundTeleportEntityPacket(
                npcPlayer.getId(),
                net.minecraft.world.entity.PositionMoveRotation.of(npcPlayer),
                Set.of(),
                false
        );
    }

    private ClientboundBlockUpdatePacket createBedPacket(Direction direction) {
        return new ClientboundBlockUpdatePacket(
                fakeBedBlockPos,
                Blocks.WHITE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, direction.getOpposite())
                        .setValue(BedBlock.PART, BedPart.HEAD)
        );
    }

    private ClientboundPlayerInfoUpdatePacket createAddNpcInfoPacket() {
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
        );
        return new ClientboundPlayerInfoUpdatePacket(actions, Collections.singletonList(npcPlayer));
    }

    private ClientboundPlayerInfoRemovePacket createRemoveNpcInfoPacket() {
        return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npcPlayer.getUUID()));
    }

    public void create() {
        nearbyPlayers = getNearbyPlayers();
        syncPlayerState();
        initializeNPCData();
        createInitializationBundle();
        setupViewers();
        setupSeatUpdateTask();
    }

    private void syncPlayerState() {
        npcPlayer.setGlowingTag(serverPlayer.hasGlowingTag());
        if (serverPlayer.hasGlowingTag()) {
            serverPlayer.setGlowingTag(false);
        }
    }

    private void initializeNPCData() {
        SynchedEntityData npcData = npcPlayer.getEntityData();
        SynchedEntityData playerData = serverPlayer.getEntityData();

        npcData.set(
                EntityDataSerializers.POSE.createAccessor(6),
                net.minecraft.world.entity.Pose.values()[org.bukkit.entity.Pose.SLEEPING.ordinal()]
        );
        npcData.set(
                EntityDataSerializers.OPTIONAL_BLOCK_POS.createAccessor(14),
                Optional.of(fakeBedBlockPos)
        );

        npcData.set(MAIN_HAND, playerData.get(MAIN_HAND));
        npcData.set(SKIN_PARTS, playerData.get(SKIN_PARTS));

        npcData.set(LEFT_SHOULDER, playerData.get(LEFT_SHOULDER));
        npcData.set(RIGHT_SHOULDER, playerData.get(RIGHT_SHOULDER));

        playerData.set(LEFT_SHOULDER, OptionalInt.empty());
        playerData.set(RIGHT_SHOULDER, OptionalInt.empty());

        serverPlayer.setInvisible(true);
        updateEquipmentVisibility(false);
    }

    private void createInitializationBundle() {
        ClientboundSetEntityDataPacket metaPacket = createEntityMetadataPacket();
        ClientboundUpdateAttributesPacket attributePacket = createAttributesPacket();

        initializationBundle = new ClientboundBundlePacket(List.of(
                addNpcInfoPacket,
                createNpcPacket,
                bedPacket,
                metaPacket,
                attributePacket,
                teleportNpcPacket
        ));
    }

    private ClientboundSetEntityDataPacket createEntityMetadataPacket() {
        List<SynchedEntityData.DataValue<?>> dirtyData = npcPlayer.getEntityData().isDirty()
                ? npcPlayer.getEntityData().packDirty()
                : npcPlayer.getEntityData().getNonDefaultValues();

        return new ClientboundSetEntityDataPacket(
                npcPlayer.getId(),
                Optional.ofNullable(dirtyData).orElseGet(List::of)
        );
    }

    private ClientboundUpdateAttributesPacket createAttributesPacket() {
        return new ClientboundUpdateAttributesPacket(
                npcPlayer.getId(),
                serverPlayer.getAttributes().getSyncableAttributes()
        );
    }

    private void setupViewers() {
        for (Player viewer : nearbyPlayers) {
            addViewer(viewer);
        }

        attachHideNameEntity();
    }

    private void attachHideNameEntity() {
        hideNameEntity.setVehicle(npcPlayer);

        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        packets.add(new ClientboundAddEntityPacket(
                hideNameEntity.getId(),
                hideNameEntity.getUUID(),
                hideNameEntity.getX(),
                hideNameEntity.getY(),
                hideNameEntity.getZ(),
                hideNameEntity.getXRot(),
                hideNameEntity.getYRot(),
                hideNameEntity.getType(),
                0,
                hideNameEntity.getDeltaMovement(),
                hideNameEntity.getYHeadRot()
        ));

        packets.add(new ClientboundSetEntityDataPacket(
                hideNameEntity.getId(),
                Optional.ofNullable(hideNameEntity.getEntityData().getNonDefaultValues())
                        .orElseGet(List::of)
        ));

        packets.add(new ClientboundSetPassengersPacket(npcPlayer));

        sendPacket(player, new ClientboundBundlePacket(packets));
    }

    private void setupSeatUpdateTask() {
        SeatPoseEntity seatEntity = (SeatPoseEntity) ((CraftEntity) seatPose.getSeatEntity()).getHandle();
        seatEntity.runnable = () -> {
            updateNearbyPlayers();
            maintainPlayerState();
            updateEquipment();
            updateSkin();
        };
    }

    private void updateNearbyPlayers() {
        Set<Player> currentPlayers = getNearbyPlayers();

        for (Player newViewer : currentPlayers) {
            if (!nearbyPlayers.contains(newViewer)) {
                nearbyPlayers.add(newViewer);
                addViewer(newViewer);
            }
        }

        Iterator<Player> iterator = nearbyPlayers.iterator();
        while (iterator.hasNext()) {
            Player oldViewer = iterator.next();
            if (!currentPlayers.contains(oldViewer)) {
                iterator.remove();
                removeViewer(oldViewer);
            }
        }
    }

    private void maintainPlayerState() {
        serverPlayer.setInvisible(true);
        updateEquipmentVisibility(false);
    }

    public void remove() {
        stopSeatUpdates();
        cleanupViewers();
        restorePlayerState();
    }

    private void stopSeatUpdates() {
        SeatPoseEntity seatEntity = (SeatPoseEntity) ((CraftEntity) seatPose.getSeatEntity()).getHandle();
        seatEntity.runnable = null;
    }

    private void cleanupViewers() {
        for (Player viewer : nearbyPlayers) {
            removeViewer(viewer);
        }
        sendPacket(player, new ClientboundRemoveEntitiesPacket(hideNameEntity.getId()));
    }

    private void restorePlayerState() {
        if (!serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY)) {
            serverPlayer.setInvisible(false);
        }

        updateEquipmentVisibility(true);
        restoreShoulderData();
        serverPlayer.setGlowingTag(npcPlayer.hasGlowingTag());
    }

    private void restoreShoulderData() {
        SynchedEntityData npcData = npcPlayer.getEntityData();
        SynchedEntityData playerData = serverPlayer.getEntityData();

        playerData.set(LEFT_SHOULDER, npcData.get(LEFT_SHOULDER));
        playerData.set(RIGHT_SHOULDER, npcData.get(RIGHT_SHOULDER));
    }

    private void addViewer(Player viewer) {
        sendPacket(viewer, initializationBundle);

        if (height < 1) return;

        Task.runEntity(viewer, () -> sendPacket(viewer, teleportNpcPacket), 1L);
        Task.runEntity(viewer, () -> sendPacket(viewer, teleportNpcPacket), 2L);
    }

    private void removeViewer(Player viewer) {
        sendPacket(viewer, removeNpcInfoPacket);
        sendPacket(viewer, removeNpcPacket);
        viewer.sendBlockChange(fakeBedLocation, fakeBedBlock.getBlockData());
    }

    private void updateEquipmentVisibility(boolean visible) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = visible ? serverPlayer.getItemBySlot(slot) : null;
            equipment.add(Pair.of(slot, item != null ? item : ItemStack.EMPTY));
        }

        ClientboundSetEquipmentPacket equipmentPacket =
                new ClientboundSetEquipmentPacket(serverPlayer.getId(), equipment);

        for (Player viewer : nearbyPlayers) {
            sendPacket(viewer, equipmentPacket);
        }
    }

    private void updateEquipment() {
        ItemStack mainHandItem = serverPlayer.getItemBySlot(EquipmentSlot.MAINHAND);

        if (shouldSkipEquipmentUpdate(mainHandItem)) {
            return;
        }

        cacheEquipment(mainHandItem);
        broadcastEquipmentUpdate();
        serverPlayer.containerMenu.sendAllDataToRemote();
    }

    private boolean shouldSkipEquipmentUpdate(ItemStack mainHandItem) {
        return equipmentCache != null
                && equipmentCache.equals(serverPlayer.getInventory().getContents())
                && mainHandCache == mainHandItem;
    }

    private void cacheEquipment(ItemStack mainHandItem) {
        equipmentCache = NonNullList.create();
        equipmentCache.addAll(serverPlayer.getInventory().getContents());
        mainHandCache = mainHandItem;
    }

    private void broadcastEquipmentUpdate() {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = serverPlayer.getItemBySlot(slot);
            equipment.add(Pair.of(slot, item));
        }

        ClientboundSetEquipmentPacket equipmentPacket =
                new ClientboundSetEquipmentPacket(npcPlayer.getId(), equipment);

        for (Player viewer : nearbyPlayers) {
            sendPacket(viewer, equipmentPacket);
        }
    }

    private void updateSkin() {
        npcPlayer.setInvisible(serverPlayer.activeEffects.containsKey(MobEffects.INVISIBILITY));

        SynchedEntityData npcData = npcPlayer.getEntityData();
        SynchedEntityData playerData = serverPlayer.getEntityData();

        npcData.set(MAIN_HAND, playerData.get(MAIN_HAND));
        npcData.set(SKIN_PARTS, playerData.get(SKIN_PARTS));

        if (!npcData.isDirty()) return;

        ClientboundSetEntityDataPacket updatePacket = new ClientboundSetEntityDataPacket(
                npcPlayer.getId(),
                Optional.ofNullable(npcData.packDirty()).orElseGet(List::of)
        );

        for (Player viewer : nearbyPlayers) {
            sendPacket(viewer, updatePacket);
        }
    }

    private Set<Player> getNearbyPlayers() {
        Set<Player> viewers = new HashSet<>();
        player.getWorld().getPlayers().stream()
                .filter(p -> seatPose.getLocation().distance(p.getLocation()) <= renderRange
                        && p.canSee(this.player))
                .forEach(viewers::add);
        return viewers;
    }

    private Direction calculateDirection() {
        return Direction.fromYRot(seatPose.getLocation().getYaw());
    }

    private ServerPlayer createNPC() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel world = ((CraftWorld) seatPose.getLocation().getWorld()).getHandle();
        GameProfile profile = new GameProfile(
                UUID.randomUUID(),
                player.getName(),
                serverPlayer.getGameProfile().properties()
        );

        ServerPlayer npc = new ServerPlayer(
                server,
                world,
                profile,
                serverPlayer.clientInformation()
        );
        npc.connection = serverPlayer.connection;

        return npc;
    }

    private static void sendPacket(Player player, Packet<?> packet) {
        NMSUtil.asNMS(player).connection.send(packet);
    }
}