package su.hitori.pack.type.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.KeyUtil;
import su.hitori.api.util.Text;
import su.hitori.pack.type.ItemModel;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
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
            meta.setEnchantmentGlintOverride(properties.glintOverride());

            Boolean fire = properties.fireResistant();
            if(fire != null) meta.setDamageResistant(fire ? DamageTypeTags.IS_FIRE : null);

            int durability = properties.durability();
            if(durability != DISABLED) ((Damageable) meta).setMaxDamage(durability);

            Boolean tooltip = properties.hideTooltip();
            if(tooltip != null) meta.setHideTooltip(tooltip);

            var unbreakable = properties.unbreakable();
            if(unbreakable != null) meta.setUnbreakable(unbreakable);

            Optional.ofNullable(properties.itemModel())
                    .map(ItemModel::resolve)
                    .ifPresent(meta::setItemModel);

            int maxStackSize = properties.maxStackSize();
            meta.setMaxStackSize(maxStackSize == DISABLED ? null : maxStackSize);

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

        if(properties.consumable() != null) stack.setData(DataComponentTypes.CONSUMABLE, properties.consumable());
        if(properties.food() != null) stack.setData(DataComponentTypes.FOOD, properties.food());
        if(properties.tool() != null) stack.setData(DataComponentTypes.TOOL, properties.tool());
        if(properties.jukebox() != null) stack.setData(DataComponentTypes.JUKEBOX_PLAYABLE, properties.jukebox());
        if(properties.equipment() != null) stack.setData(DataComponentTypes.EQUIPPABLE, properties.equipment());

        return stack;
    }

}
