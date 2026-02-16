package su.hitori.pack.pose.crawl;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import su.hitori.api.nms.NMSUtil;
import su.hitori.api.util.Task;
import su.hitori.pack.pose.PoseService;

import java.util.Set;

public final class CrawlPose {

    private final PoseService poseService;

    private final Player player;
    private final ServerPlayer serverPlayer;
    private final CrawlEntity crawlEntity;

    private boolean boxEntityExist;
    private boolean finished;

    public CrawlPose(PoseService poseService, Player player) {
        this.poseService = poseService;

        this.player = player;
        this.serverPlayer = NMSUtil.asNMS(player);
        this.crawlEntity = new CrawlEntity(player.getLocation());
    }

    public Player player() {
        return player;
    }

    public void start() {
        player.setSwimming(true);
        Task.runGlobally(() -> tick(player.getLocation()), 1L);
    }

    void tick(Location location) {
        if(finished || !checkValidity()) return;

        Location tickLocation = location.clone();
        Block locationBlock = tickLocation.getBlock();
        int blockSize = (int) ((tickLocation.getY() - tickLocation.getBlockY()) * 100);
        tickLocation.setY(tickLocation.getBlockY() + (blockSize >= 40 ? 2.49 : 1.49));
        Block aboveBlock = tickLocation.getBlock();
        boolean hasSolidBlockAbove = aboveBlock.getBoundingBox().contains(tickLocation.toVector()) && !aboveBlock.getCollisionShape().getBoundingBoxes().isEmpty();
        if(hasSolidBlockAbove) {
            destroyEntity();
            return;
        }

        Location playerLocation = location.clone();
        Task.ensureSync(() -> {
            if(finished) return;

            int height = locationBlock.getBoundingBox().getHeight() >= 0.4 || playerLocation.getY() % 0.015625 == 0.0 ? (player.getFallDistance() > 0.7 ? 0 : blockSize) : 0;

            playerLocation.setY(playerLocation.getY() + (height >= 40 ? 1.5 : 0.5));

            crawlEntity.setRawPeekAmount(height >= 40 ? 100 - height : 0);

            int entityId = crawlEntity.getId();
            if(!boxEntityExist) {
                crawlEntity.setPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                sendPacket(new ClientboundAddEntityPacket(
                        entityId,
                        crawlEntity.getUUID(),
                        crawlEntity.getX(), crawlEntity.getY(), crawlEntity.getZ(),
                        crawlEntity.getXRot(), crawlEntity.getYRot(),
                        crawlEntity.getType(), 0,
                        crawlEntity.getDeltaMovement(),
                        crawlEntity.getYHeadRot()
                ));
                sendEntityData();
                boxEntityExist = true;
            }
            else {
                sendEntityData();
                crawlEntity.setPosRaw(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                sendPacket(new ClientboundTeleportEntityPacket(entityId, net.minecraft.world.entity.PositionMoveRotation.of(crawlEntity), Set.of(), false));
            }
        });
    }

    private void sendEntityData() {
        var data = crawlEntity.getEntityData().getNonDefaultValues();
        if(data != null) sendPacket(new ClientboundSetEntityDataPacket(crawlEntity.getId(), data));
    }

    public void stop() {
        if(finished) return;
        finished = true;

        player.setSwimming(false);
        destroyEntity();
    }

    private boolean checkValidity() {
        if(player.isInWater() || player.isFlying()) {
            poseService.removeCrawlPose(this);
            return false;
        }
        return true;
    }

    private void destroyEntity() {
        if(!boxEntityExist) return;
        sendPacket(new ClientboundRemoveEntitiesPacket(crawlEntity.getId()));
        boxEntityExist = false;
    }

    private void sendPacket(Packet<?> packet) {
        serverPlayer.connection.send(packet);
    }

}
