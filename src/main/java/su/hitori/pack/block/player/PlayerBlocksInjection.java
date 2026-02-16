package su.hitori.pack.block.player;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.kyori.adventure.key.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SoundType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import su.hitori.api.util.Task;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.level.Level;
import su.hitori.pack.block.level.LevelService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
Class that adds player an ability to break custom blocks placed with barriers
 */
@SuppressWarnings("resource")
public final class PlayerBlocksInjection extends ChannelInboundHandlerAdapter {

    private final ServerPlayer player;
    private final LevelService levelService;

    // temp fields
    private BlockPos destroyPos = null;

    private boolean breakingBlock = false;
    private int lastSentState = 0;
    private int ticksSinceStart = 0;

    PlayerBlocksInjection(Player player, LevelService levelService) {
        this.player = ((CraftPlayer) player).getHandle();
        this.levelService = levelService;
    }

    public static void inject(Player player, LevelService service) {
        if(!player.isValid()) return;
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        if(pipeline.get("packet_handler") == null) return;

        ChannelHandler handler = pipeline.get("player_blocks_handler");
        if(handler != null) pipeline.remove(handler);

        pipeline.addBefore(
                "packet_handler",
                "player_blocks_handler",
                new PlayerBlocksInjection(player, service)
        );
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof ServerboundPlayerActionPacket action && action(action.getAction(), action.getPos())) return;
        if(msg instanceof ServerboundSwingPacket) swing();
        if(msg instanceof ServerboundPickItemFromEntityPacket packet) Task.ensureSync(() -> pickItem(packet.id()));

        super.channelRead(ctx, msg);
    }

    private void pickItem(int entityId) {
        Entity entity = player.level().getEntity(entityId);
        if(!(entity instanceof Interaction)) return;

        AtomicInteger source = new AtomicInteger(-1);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        levelService.attemptToPickup(
                player.getBukkitEntity(),
                entity.getBukkitEntity().getLocation().getBlock(),
                cancelled::set,
                source::set
        );
        if(cancelled.get()) return;

        Inventory inventory = player.getInventory();
        int targetSlot = inventory.getSuitableHotbarSlot();
        int i = source.get();
        if (i != -1) {
            if (Inventory.isHotbarSlot(i) && Inventory.isHotbarSlot(targetSlot)) inventory.setSelectedSlot(i);
            else inventory.pickSlot(i, targetSlot);
        }

        player.connection.connection.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
        this.player.inventoryMenu.broadcastChanges();
        if (GlobalConfiguration.get().unsupportedSettings.updateEquipmentOnPlayerActions) {
            this.player.detectEquipmentUpdates();
        }
    }

    private boolean action(ServerboundPlayerActionPacket.Action action, BlockPos pos) {
        if(player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) return false;

        if(action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            Level level = getLevel();
            BlockState state = level.getState(pos.getX(), pos.getY(), pos.getZ());
            if(state.isEmpty() || player.getBukkitEntity().getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType() != Material.BARRIER) return false;

            this.breakingBlock = true;
            this.destroyPos = pos;

            this.lastSentState = (int) (getDestroyProgress() * 10F);
            this.ticksSinceStart = 0;
            return true;
        }
        else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            if(!breakingBlock) return false;

            this.breakingBlock = false;
            this.destroyPos = null;
            this.ticksSinceStart = 0;
            this.lastSentState = 0;

            return true;
        }

        return false;
    }

    private void swing() {
        if(!breakingBlock) return;

        int i = ticksSinceStart++;
        float f = getDestroyProgress() * (i + 1);
        int i1 = (int) (f * 10.0F);
        if(i1 == lastSentState) return;
        lastSentState = i1;
        if(i1 < 10) return;

        Task.runEntity(player.getBukkitEntity(), () -> {
            if(!breakingBlock) return;
            Level level = getLevel();
            levelService.removeCustomBlock(
                    level.getWorld().getBlockAt(destroyPos.getX(), destroyPos.getY(), destroyPos.getZ()),
                    true,
                    player.getBukkitEntity()
            );

            // damage tool
            var stack = player.getItemBySlot(EquipmentSlot.MAINHAND);
            Tool tool = stack.get(DataComponents.TOOL);
            if(tool != null && tool.damagePerBlock() > 0) {
                stack.hurtAndBreak(tool.damagePerBlock(), player, EquipmentSlot.MAINHAND);
            }

            // sound
            SoundType soundType = SoundType.STONE;
            MinecraftServer.getServer().getPlayerList().broadcast(
                    null,
                    destroyPos.getX(),
                    destroyPos.getY(),
                    destroyPos.getZ(),
                    64.0F,
                    ((CraftWorld) level.getWorld()).getHandle().dimension(),
                    new ClientboundSoundPacket(
                            Holder.direct(soundType.getBreakSound()),
                            SoundSource.BLOCKS,
                            destroyPos.getX() + .5,
                            destroyPos.getY() + .5,
                            destroyPos.getZ() + .5,
                            (soundType.getVolume() + 1.0F) / 2.0F,
                            soundType.getPitch() * 0.8F,
                            player.random.nextLong()
                    )
            );

            // field clearing
            destroyPos = null;
            breakingBlock = false;
            ticksSinceStart = 0;
            lastSentState = 0;
        }, 0L);
    }

    private float getDestroyProgress() {
        return (float) (getDestroySpeed() / 0.3 / 100);
    }

    private Level getLevel() {
        return levelService.getLevel(Key.key(player.level().getTypeKey().identifier().toString()));
    }

    // an attempt to recreate vanilla system
    private float getDestroySpeed() {
        float destroySpeed = 1F; // speed for destroy from tool, defaults to 1

        if(MobEffectUtil.hasDigSpeed(player)) destroySpeed *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;

        if(player.hasEffect(MobEffects.MINING_FATIGUE)) {
            destroySpeed *= switch (player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        }

        destroySpeed *= (float) player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if(player.isEyeInFluid(FluidTags.WATER)) destroySpeed *= (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);

        if(!player.onGround) destroySpeed /= 5.0F;

        return destroySpeed;
    }

}
