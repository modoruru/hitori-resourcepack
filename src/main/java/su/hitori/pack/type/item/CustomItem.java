package su.hitori.pack.type.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.components.CraftFoodComponent;
import org.bukkit.craftbukkit.inventory.components.CraftJukeboxComponent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.KeyUtil;
import su.hitori.api.util.Text;
import su.hitori.pack.type.ItemModel;

import java.util.Map;
import java.util.Optional;

public record CustomItem(ItemProperties properties) implements Keyed {

    public static final int DISABLED = -1;
    public static final NamespacedKey ITEM_ID = KeyUtil.create("item");

    public static Optional<Key> getId(ItemStack stack) {
        return Optional.ofNullable(stack)
                .map(itemStack -> itemStack.getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING))
                .map(Key::key);
    }

    public static Optional<CustomItem> getCustomItem(ItemStack stack, Registry<@NotNull CustomItem> itemRegistry) {
        return getId(stack).map(itemRegistry::get);
    }

    @Override
    public @NotNull Key key() {
        return properties.key();
    }

    public ItemStack create() {
        ItemStack stack = new ItemStack(properties.type());

        stack.addItemFlags(properties.flags().toArray(new ItemFlag[0]));

        stack.editMeta(meta -> {
            FoodProperties food = properties.food();
            if(food != null) meta.setFood(new CraftFoodComponent(food));

            ToolComponent tool = properties.tool();
            meta.setTool(tool);

            meta.setEnchantmentGlintOverride(properties.glintOverride());

            Boolean fire = properties.fireResistant();
            if(fire != null) meta.setDamageResistant(fire ? DamageTypeTags.IS_FIRE : null);

            int durability = properties.durability();
            if(durability != DISABLED) ((Damageable) meta).setMaxDamage(durability);

            Boolean tooltip = properties.hideTooltip();
            if(tooltip != null) meta.setHideTooltip(tooltip);

            var jukebox = properties.jukebox();
            if(jukebox != null) meta.setJukeboxPlayable(new CraftJukeboxComponent(jukebox));

            var unbreakable = properties.unbreakable();
            if(unbreakable != null) meta.setUnbreakable(unbreakable);

            Optional.ofNullable(properties.itemModel())
                    .map(ItemModel::resolve)
                    .ifPresent(meta::setItemModel);

            int maxStackSize = properties.maxStackSize();
            meta.setMaxStackSize(maxStackSize == DISABLED ? null : maxStackSize);

            meta.setEquippable(properties.equipment());

            meta.displayName(properties.name().decoration(TextDecoration.ITALIC, false));

            meta.setRarity(properties.rarity());

            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, key().asString());

            var attributeModifiers = properties.attributeModifiers();
            for (Map.Entry<Attribute, AttributeModifier> attributeModifier : attributeModifiers) {
                meta.addAttributeModifier(
                        attributeModifier.getKey(),
                        attributeModifier.getValue()
                );
            }
        });

        var lore = properties.lore();
        if(!lore.isEmpty()) stack.lore(
                lore.stream()
                        .map(row -> Text.create(row).decoration(TextDecoration.ITALIC, false))
                        .toList()
        );

        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        var builder = DataComponentPatch.builder();
        if(properties.consumable() != null) builder.set(DataComponents.CONSUMABLE, properties.consumable());
        if(properties.potion() != null) builder.set(DataComponents.POTION_CONTENTS, properties.potion());
        nms.applyComponents(builder.build());
        stack = CraftItemStack.asBukkitCopy(nms);

        return stack;
    }

}
