package su.hitori.pack.type.item;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.KeyUtil;
import su.hitori.api.util.UnsafeUtil;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public record CustomItem(ItemProperties properties) implements Keyed {

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
    public Key key() {
        return properties.key();
    }

    public ItemStack create(int amount) {
        ItemStack stack = create();
        stack.setAmount(amount);
        return stack;
    }

    public ItemStack create() {
        ItemStack stack = new ItemStack(properties.type());

        for (DataComponentType.Valued<?> valuedComponentType : properties.valuedComponentTypes()) {
            Object value = UnsafeUtil.cast(properties.valuedComponent(valuedComponentType));

            if(value == null) stack.unsetData(valuedComponentType);
            else stack.setData(UnsafeUtil.cast(valuedComponentType), value);
        }

        for (DataComponentType.NonValued nonValuedComponentType : properties.toSetNonValuedComponentTypes()) {
            stack.setData(nonValuedComponentType);
        }

        for (DataComponentType.NonValued nonValuedComponentType : properties.toUnsetNonValuedComponentTypes()) {
            stack.unsetData(nonValuedComponentType);
        }

        if(properties.itemModel() != null)
            stack.setData(DataComponentTypes.ITEM_MODEL, properties.itemModel().resolve());

        return stack;
    }

}
