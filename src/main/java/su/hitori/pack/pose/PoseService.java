package su.hitori.pack.pose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.hitori.api.Hitori;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.pack.pose.crawl.CrawlPose;
import su.hitori.pack.pose.event.PlayerPoseEvent;
import su.hitori.pack.pose.event.PlayerStartPoseEvent;
import su.hitori.pack.pose.event.PlayerStopPoseEvent;
import su.hitori.pack.pose.event.PoseType;
import su.hitori.pack.pose.lie.LyingPose;
import su.hitori.pack.pose.seat.SeatPose;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PoseService {

    private static final Logger LOGGER = LoggerFactory.instance().create(PoseService.class);

    public static final String SEAT_TAG = "seat_entity";
    public static final double STAIR_XZ_OFFSET = 0.123d;
    public static final double STAIR_Y_OFFSET = 0.5d;
    public static final double BASE_OFFSET = -0.05d;

    private final Map<Player, SeatPose> seatByRider = new HashMap<>();
    private final Map<Block, SeatPose> seatByBlock = new HashMap<>();
    private final Set<Player> blocked = new HashSet<>();

    private final Map<Player, LyingPose> lyingPoseByRider = new HashMap<>();
    private final Map<Block, LyingPose> lyingPoseByBlock = new HashMap<>();

    private final Map<Player, CrawlPose> crawlPoseByCrawling = new HashMap<>();

    public SeatPose getSeatPoseByRider(Player entity) {
        return seatByRider.get(entity);
    }

    public LyingPose getLyingPoseByRider(Player entity) {
        return lyingPoseByRider.get(entity);
    }

    public LyingPose getLyingPoseByBlock(Block block) {
        return lyingPoseByBlock.get(block);
    }

    public CrawlPose getCrawlPoseByCrawling(Player player) {
        return crawlPoseByCrawling.get(player);
    }

    public void removeAllPoses() {
        seatByRider.values().forEach(this::removeSeatPose);
        lyingPoseByRider.values().forEach(this::removeLyingPose);
        crawlPoseByCrawling.values().forEach(this::removeCrawlPose);
    }

    public boolean isBlockOccupied(Block block) {
        return seatByBlock.containsKey(block) || lyingPoseByBlock.containsKey(block);
    }

    public boolean isBlocked(Player player) {
        return blocked.contains(player);
    }

    public SeatPose getSeatPoseOnBlock(Block block) {
        return seatByBlock.get(block);
    }

    private boolean callEvent(Player player, PoseType poseType, boolean start) {
        PlayerPoseEvent event = start
                ? new PlayerStartPoseEvent(player, poseType)
                : new PlayerStopPoseEvent(player, poseType);
        return event.callEvent();
    }

    public CrawlPose createCrawlPose(Player player) {
        if(!callEvent(player, PoseType.CRAWL, true)) return null;

        CrawlPose crawlPose = new CrawlPose(this, player);
        crawlPose.start();
        crawlPoseByCrawling.put(player, crawlPose);

        return crawlPose;
    }

    public LyingPose createLyingPose(Block block, Player player) {
        return createLyingPose(block, player, 0d, 0d, 0d, player.getLocation().getYaw(), true);
    }

    public LyingPose createLyingPose(Block block, Player player, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        if(!callEvent(player, PoseType.LIE, true)) return null;

        Location seatLocation = createSeatLocation(block, player.getLocation(), xOffset, yOffset, zOffset, sitInBlockCenter);
        seatLocation.setYaw(seatRotation);

        Entity seatEntity = PoseUtil.createSeatEntity(seatLocation, player, true);
        if(seatEntity == null) return null;

        LyingPose lyingPose = new LyingPose(new SeatPose(seatEntity, player, yOffset, block, seatLocation));
        lyingPose.create();
        lyingPoseByRider.put(player, lyingPose);
        lyingPoseByBlock.put(block, lyingPose);

        return lyingPose;
    }

    public SeatPose createSeatPose(Block block, Player player) {
        return createSeatPose(block, player, true, 0d, 0d, 0d, player.getLocation().getYaw(), true);
    }

    public SeatPose createSeatPose(Block block, Player rider, boolean canRotate, double xOffset, double yOffset, double zOffset, float seatRotation, boolean sitInBlockCenter) {
        if(blocked.contains(rider)) return null;
        if(!callEvent(rider, PoseType.SEAT, true)) return null;

        Location seatLocation = createSeatLocation(
                block,
                rider.getLocation(),
                xOffset, yOffset, zOffset,
                sitInBlockCenter
        );
        seatLocation.setYaw(seatRotation);

        Entity seatEntity = PoseUtil.createSeatEntity(seatLocation, rider, canRotate);
        if(seatEntity == null) return null;

        SeatPose seatPose = new SeatPose(seatEntity, rider, yOffset, block, seatLocation);
        seatByRider.put(rider, seatPose);
        seatByBlock.put(block, seatPose);
        return seatPose;
    }

    private Location createSeatLocation(Block block, Location location, double xOffset, double yOffset, double zOffset, boolean sitInBlockCenter) {
        if(!sitInBlockCenter) return location.add(
                xOffset,
                yOffset - BASE_OFFSET,
                zOffset
        );

        double offset = block.getBoundingBox().getMinY() + block.getBoundingBox().getHeight();
        return block.getLocation().add(
                0.5d + xOffset,
                yOffset - BASE_OFFSET + (offset == 0 ? 1 : offset - block.getY()),
                0.5d + zOffset
        );
    }

    public void moveSeat(SeatPose seatPose, BlockFace blockDirection) {
        if(seatPose.getRider() instanceof Player player) {
            PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, player.getLocation(), player.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
            Bukkit.getPluginManager().callEvent(playerMoveEvent);
            if(playerMoveEvent.isCancelled()) return;
        }

        seatPose.setBlock(seatPose.getBlock().getRelative(blockDirection));
        seatPose.setLocation(seatPose.getLocation().add(blockDirection.getModX(), blockDirection.getModY(), blockDirection.getModZ()));
        PoseUtil.setEntityLocation(seatPose.getSeatEntity(), seatPose.getLocation());

        seatByBlock.put(seatPose.getBlock(), seatPose);
    }

    public boolean removeCrawlPose(CrawlPose crawlPose) {
        if(crawlPose == null) return false;

        callEvent(crawlPose.player(), PoseType.CRAWL, false);

        crawlPoseByCrawling.remove(crawlPose.player());
        crawlPose.stop();

        return true;
    }

    public boolean removeSeatPose(@Nullable SeatPose seatPose) {
        return removeSeatPose(seatPose, true);
    }

    public boolean removeSeatPose(@Nullable SeatPose seatPose, boolean useSafeDismount) {
        if(seatPose == null) return false;
        callEvent(seatPose.getRider(), PoseType.SEAT, false);

        Player rider = seatPose.getRider();
        blocked.add(rider);

        if(useSafeDismount) safeDismount(seatPose);

        seatByBlock.remove(seatPose.getBlock());
        seatByRider.remove(rider);
        seatPose.getSeatEntity().remove();
        blocked.remove(rider);
        return true;
    }

    public boolean removeLyingPose(@Nullable LyingPose pose) {
        return removeLyingPose(pose, true);
    }

    public boolean removeLyingPose(@Nullable LyingPose lyingPose, boolean useSafeDismount) {
        if(lyingPose == null) return false;

        SeatPose seatPose = lyingPose.seatPose();
        Player player = lyingPose.player();
        callEvent(player, PoseType.LIE, false);
        if(useSafeDismount) safeDismount(seatPose);

        lyingPoseByRider.remove(player);
        lyingPoseByBlock.remove(seatPose.getBlock());
        lyingPose.remove();
        seatPose.getSeatEntity().remove();

        return true;
    }

    private void safeDismount(@NotNull SeatPose seatPose) {
        Entity rider = seatPose.getRider();

        try {
            Material blockType = seatPose.getBlock().getType();

            Location returnLocation = seatPose.getLocation().add(
                    0d,
                    BASE_OFFSET + (Tag.STAIRS.isTagged(blockType) ? STAIR_Y_OFFSET : 0d) - seatPose.initialYOffset(),
                    0d
            );
            Location entityLocation = rider.getLocation();

            returnLocation.setYaw(entityLocation.getYaw());
            returnLocation.setPitch(entityLocation.getPitch());

            rider.teleport(returnLocation);
        }
        catch(Throwable e) {
            // If we can't access the block, entity or seat entity data in a Folia server environment we ignore this error
            if(!Hitori.instance().serverCoreInfo().isFolia()) LOGGER.log(Level.SEVERE, "Could not safely dismount the entity!", e);
        }
    }

    public SeatPose createStairSeatForEntity(Block block, Player entity) {
        Stairs stairs = (Stairs) block.getBlockData();
        if(stairs.getHalf() != Bisected.Half.BOTTOM) return createSeatPose(block, entity);

        BlockFace face = stairs.getFacing().getOppositeFace();
        if(stairs.getShape() == Stairs.Shape.STRAIGHT) {
            return switch (face) {
                case EAST -> createSeatPose(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, -90f, true);
                case SOUTH -> createSeatPose(block, entity, false, 0d, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 0f, true);
                case WEST -> createSeatPose(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, 0d, 90f, true);
                case NORTH -> createSeatPose(block, entity, false, 0d, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 180f, true);
                default -> null;
            };
        }

        // todo: rewrite this peace of someone shit
        Stairs.Shape stairShape = stairs.getShape();
        if(face == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_RIGHT || face == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_LEFT || face == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_RIGHT || face == BlockFace.EAST && stairShape == Stairs.Shape.INNER_LEFT) {
            return createSeatPose(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, -135f, true);
        }
        else if(face == BlockFace.NORTH && stairShape == Stairs.Shape.OUTER_LEFT || face == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_RIGHT || face == BlockFace.NORTH && stairShape == Stairs.Shape.INNER_LEFT || face == BlockFace.WEST && stairShape == Stairs.Shape.INNER_RIGHT) {
            return createSeatPose(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 135f, true);
        }
        else if(face == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_RIGHT || face == BlockFace.WEST && stairShape == Stairs.Shape.OUTER_LEFT || face == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_RIGHT || face == BlockFace.WEST && stairShape == Stairs.Shape.INNER_LEFT) {
            return createSeatPose(block, entity, false, -STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, 45f, true);
        }
        else if(face == BlockFace.SOUTH && stairShape == Stairs.Shape.OUTER_LEFT || face == BlockFace.EAST && stairShape == Stairs.Shape.OUTER_RIGHT || face == BlockFace.SOUTH && stairShape == Stairs.Shape.INNER_LEFT || face == BlockFace.EAST && stairShape == Stairs.Shape.INNER_RIGHT) {
            return createSeatPose(block, entity, false, STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, STAIR_XZ_OFFSET, -45f, true);
        }

        return null;
    }

}
