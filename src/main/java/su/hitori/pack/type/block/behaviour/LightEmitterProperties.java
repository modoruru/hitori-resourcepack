package su.hitori.pack.type.block.behaviour;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import su.hitori.api.util.Task;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;
import su.hitori.pack.type.block.placement.PlacementProperties;
import su.hitori.pack.type.block.placement.PlacementType;

import java.util.HashSet;
import java.util.Set;

public record LightEmitterProperties(int lightLevel, ItemModel disabled, ItemModel enabled) implements BehaviourProperties {

    private static final int[][] OFFSETS = {
            {1, 0, 0}, {0, 1, 0}, {0, 0, 1},
            {-1, 0, 0}, {0, -1, 0}, {0, 0, -1}
    };

    @Override
    public BehaviourType type() {
        return BehaviourType.LIGHT_EMITTER;
    }

    @Override
    public boolean onPlayerInteract(CustomBlock customBlock, Block clickedBlock, BlockState clickedState, Block center, BlockState centerState, ItemDisplay displayEntity, Player player, EquipmentSlot hand, ItemStack handItem) {
        centerState.additionalData = centerState.additionalData == 1 ? 0 : 1;
        return true;
    }

    @Override
    public void updateStateFromAdditionalData(CustomBlock customBlock, Block center, BlockState centerState, ItemDisplay displayEntity) {
        boolean enabled = centerState.additionalData == 1;

        ItemStack stack = displayEntity.getItemStack();
        stack.editMeta(meta -> meta.setItemModel((enabled ? this.enabled : this.disabled).resolve()));
        displayEntity.setItemStack(stack);

        place(customBlock.blockProperties().placementProperties(), centerState.direction(), centerState.orientation(), center, enabled);
    }

    public void place(PlacementProperties properties, Direction direction, Orientation orientation, Block center, boolean enabled) {
        int lightLevel = this.lightLevel;
        if(properties.type() == PlacementType.SOLID) --lightLevel;

        if(lightLevel <= 0) return;

        Light light;
        if(enabled) {
            light = (Light) Material.LIGHT.createBlockData();
            light.setLevel(lightLevel);
        }
        else light = null;

        Task.ensureSync(() -> {
            for (Block block : getBlocksForModification(properties, direction, orientation, center, enabled)) {
                block.setType(enabled ? Material.LIGHT : Material.AIR);
                if(enabled) block.setBlockData(light);
            }
        });
    }

    public Set<Block> getBlocksForModification(PlacementProperties properties, Direction direction, Orientation orientation, Block center, boolean enabled) {
        if(properties.type() == PlacementType.ENTITY)
            return Set.of(center);

        Set<Block> lightBlocks = new HashSet<>();
        Set<Block> placed = new HashSet<>(properties.asSolid().getBlocksAffectedByPlacement(direction, orientation, center));
        for (Block block : placed) {
            for (int[] offset : OFFSETS) {
                Block adjacent = block.getRelative(offset[0], offset[1], offset[2]);
                if(adjacent.getType() == (enabled ? Material.AIR : Material.LIGHT)) lightBlocks.add(adjacent);
            }
        }

        lightBlocks.removeAll(placed);

        return lightBlocks;
    }

}
