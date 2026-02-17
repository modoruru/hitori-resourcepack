package su.hitori.pack.block.player;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.raytracing.RayTraceTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.nms.NMSUtil;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Task;
import su.hitori.pack.block.BlockPos;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.level.Level;
import su.hitori.pack.block.level.LevelService;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;
import su.hitori.pack.type.item.CustomItem;
import su.hitori.pack.type.item.ItemProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CustomBlockListener implements Listener {

    private final Registry<@NotNull CustomBlock> blockRegistry;
    private final Registry<@NotNull CustomItem> itemRegistry;
    private final LevelService levelService;

    private final Map<UUID, Long> lastActionTime = new HashMap<>();

    public CustomBlockListener(Registry<@NotNull CustomBlock> blockRegistry, Registry<@NotNull CustomItem> itemRegistry, LevelService levelService) {
        this.blockRegistry = blockRegistry;
        this.itemRegistry = itemRegistry;
        this.levelService = levelService;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        PlayerBlocksInjection.inject(event.getPlayer(), levelService);
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        if(levelService.handlePlayerInteraction(event.getRightClicked().getLocation().getBlock(), player, hand, player.getInventory().getItem(hand))) {
            player.swingHand(hand);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        lastActionTime.remove(event.getPlayer().getUniqueId());
    }

    private long diff(long a, long b) {
        return Math.max(a, b) - Math.min(a, b);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        UUID playerUid = player.getUniqueId();
        Block block = event.getClickedBlock();
        ItemStack stack = event.getItem();
        assert hand != null && block != null;

        long now = System.currentTimeMillis();
        long diff = (long) (diff(lastActionTime.getOrDefault(playerUid, -1L), now) / 20d);

        // it seems issue with multiple calls from one click is now fixed
        if(diff == 0 || diff == 1) {
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);

            Material type;
            if(stack != null && ((type = stack.getType()) == Material.WRITABLE_BOOK || type == Material.WRITTEN_BOOK)) {
                Task.runEntity(player, player::closeInventory, 1L);
                return;
            }
            return;
        }

        if(!player.isSneaking() && levelService.handlePlayerInteraction(block, player, hand, event.getItem())) {
            lastActionTime.put(playerUid, now);
            player.swingHand(hand);
            return;
        }

        if (stack == null || stack.isEmpty()) return;

        // first: check if item in player hand is custom block
        CustomBlock customBlock = CustomItem.getCustomItem(stack, itemRegistry)
                .map(CustomItem::properties)
                .map(ItemProperties::customBlock)
                .map(blockRegistry::get)
                .orElse(null);
        if (customBlock == null) return;

        // second: check game mode
        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE || (!customBlock.blockProperties().placementProperties().survivalFriendly() && gameMode == GameMode.SURVIVAL)) return;

        // third: check if there is another block
        BlockFace face = event.getBlockFace();
        Block placedPosition = block.getRelative(face);
        if (placedPosition.getType() != Material.AIR) return;

        // fourth: check some shift, interactable blocks and some other shit
        if(player.hasActiveItem() && player.getActiveItemHand() != hand) return;
        if(!player.isSneaking()) {
            ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
            var blockPos = new net.minecraft.core.BlockPos(block.getX(), block.getY(), block.getZ());
            InteractionResult interactionResult = serverLevel.getBlockState(blockPos).useWithoutItem(serverLevel, NMSUtil.asNMS(player), new BlockHitResult(
                    CraftLocation.toVec3(player.getEyeLocation()),
                    net.minecraft.core.Direction.fromYRot(player.getYaw()),
                    blockPos,
                    true
            ));
            event.setUseItemInHand(Event.Result.DENY);
            if(interactionResult.consumesAction()) return;
        }

        Orientation orientation = switch (face) {
            case DOWN -> Orientation.CEILING;
            case NORTH, EAST, SOUTH, WEST -> {
                Location interactionPoint = event.getInteractionPoint();
                assert interactionPoint != null;
                yield interactionPoint.getY() > block.getY() + .5
                        ? Orientation.SIDE_DOWN
                        : Orientation.SIDE_UP;
            }
            default -> Orientation.FLOOR;
        };

        Direction direction = (switch (face) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
            default -> Direction.fromYaw(player.getYaw(), true);
        }).opposite();

        if(levelService.placeCustomBlock(
                customBlock,
                direction,
                orientation,
                placedPosition,
                false,
                stack,
                player
        )) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);

            if(player.getGameMode() == GameMode.SURVIVAL)
                stack.setAmount(stack.getAmount() - 1);

            Material type = stack.getType();
            if(type == Material.WRITABLE_BOOK || type == Material.WRITTEN_BOOK) {
                Task.runEntity(player, player::closeInventory, 1L);
                return;
            }

            if(stack.getItemMeta().getEquippable().isSwappable())
                Task.runEntity(player, player::updateInventory, 1L);

            player.swingHand(hand);
            lastActionTime.put(playerUid, now);
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        ItemStack stack = event.getItemInHand();
        if(stack.isEmpty()) return;

        Optional<CustomItem> optionalCustomItem = CustomItem.getCustomItem(stack, itemRegistry);
        if(optionalCustomItem.isEmpty()) return;

        Optional<CustomBlock> optionalCustomBlock = optionalCustomItem
                .map(CustomItem::properties)
                .map(ItemProperties::customBlock)
                .map(blockRegistry::get);

        optionalCustomBlock.ifPresent(customBlock -> {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Block block = event.getBlock();
            Block origin = event.getBlockAgainst();

            BlockFace face = origin.getFace(block);
            Orientation orientation = switch (face) {
                case DOWN -> Orientation.CEILING;
                case NORTH, SOUTH, EAST, WEST -> {
                    Location eyes = player.getEyeLocation();
                    RayTraceResult result = player.getWorld().rayTrace(
                            builder -> builder
                                    .targets(RayTraceTarget.BLOCK)
                                    .maxDistance(5)
                                    .blockFilter(other -> BlockPos.equals(origin, other))
                                    .direction(eyes.getDirection())
                                    .start(eyes)
                    );
                    if(result == null) yield null;

                    yield result.getHitPosition().getY() > origin.getY() + 0.5
                            ? Orientation.SIDE_DOWN
                            : Orientation.SIDE_UP;
                }
                case null, default -> Orientation.FLOOR;
            };

            if(orientation == null) return;

            Direction direction = (switch (face) {
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case WEST -> Direction.WEST;
                case null, default -> Direction.fromYaw(player.getYaw(), true);
            }).opposite();

            Task.runLocation(
                    block.getLocation(),
                    () -> levelService.placeCustomBlock(
                            customBlock,
                            direction,
                            orientation,
                            block,
                            false,
                            stack,
                            player
                    ),
                    0L
            );
        });
    }

    // only for creative mode
    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Block bukkitBlock = event.getBlock();
        Level level = levelService.getLevel(bukkitBlock.getWorld());
        BlockState blockState = level.getState(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

        if(!blockState.isEmpty() && bukkitBlock.getType() == Material.BARRIER)
            levelService.removeCustomBlock(bukkitBlock, false, event.getPlayer());
    }

    @EventHandler
    private void onEntityTarget(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player player && levelService.isHitboxEntity(event.getEntity())) {
            levelService.removeCustomBlock(event.getEntity().getLocation().getBlock(), !player.getGameMode().isInvulnerable(), player);
        }
    }

    @EventHandler
    private void onPlayerPickBlock(PlayerPickBlockEvent event) {
        levelService.attemptToPickup(event.getPlayer(), event.getBlock(), event::setCancelled, event::setSourceSlot);
    }

}
