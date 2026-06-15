package su.hitori.pack.type.item;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.util.UnsafeUtil;
import su.hitori.pack.type.ItemModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public final class ItemProperties {

    private static final Logger LOGGER = LoggerFactory.instance().create();

    private final Key key;

    private Material type = Material.POPPED_CHORUS_FRUIT;

    private final Map<DataComponentType.Valued<?>, Object> dataComponents;
    private final Set<DataComponentType.NonValued> toSet, toUnset;

    private @Nullable ItemModel itemModel;
    private @Nullable Key customBlock;

    public ItemProperties(@NotNull Key key) {
        this.key = key;

        this.dataComponents = new HashMap<>();
        this.toSet = new HashSet<>();
        this.toUnset = new HashSet<>();
    }

    private void setNameById() {
        Component component = valuedComponent(DataComponentTypes.CUSTOM_NAME);
        if(component == null) valuedComponent(DataComponentTypes.CUSTOM_NAME, Component.translatable(String.format("item.%s.%s", key.namespace(), key.value())).fallback(key.value()));
    }

    public Key key() {
        return key;
    }

    public ItemProperties type(@NotNull Material type) {
        this.type = type;
        return this;
    }

    public Material type() {
        return type;
    }

    private void verifyDataComponentType(DataComponentType type) {
        final DataComponentType retrievedType = Registry.DATA_COMPONENT_TYPE.get(type.key());
        if(retrievedType == null) throw new IllegalArgumentException("passed DataComponentType does not exists in minecraft registry.");
    }

    public Set<DataComponentType.Valued<?>> valuedComponentTypes() {
        return dataComponents.keySet();
    }

    public Set<DataComponentType.NonValued> toSetNonValuedComponentTypes() {
        return Set.copyOf(toSet);
    }

    public Set<DataComponentType.NonValued> toUnsetNonValuedComponentTypes() {
        return Set.copyOf(toUnset);
    }

    public <T> ItemProperties valuedComponent(DataComponentType.Valued<T> valuedComponentType, T value) {
        verifyDataComponentType(valuedComponentType);

        if(valuedComponentType.key().equals(DataComponentTypes.ITEM_MODEL.key()))
            throw new IllegalArgumentException("It's forbidden to set \"Item model\" using this method. Use ItemProperties#itemModel(su.hitori.pack.type.ItemModel) instead.");

        if(value == null) dataComponents.remove(valuedComponentType);
        else dataComponents.put(valuedComponentType, value);

        if(valuedComponentType.key().equals(DataComponentTypes.CUSTOM_NAME.key()))
            setNameById();

        return this;
    }

    public @Nullable <T> T valuedComponent(DataComponentType.Valued<T> valuedComponentType) {
        verifyDataComponentType(valuedComponentType);
        return UnsafeUtil.cast(dataComponents.get(valuedComponentType));
    }

    public void setNonValuedComponent(DataComponentType.NonValued nonValuedComponentType) {
        verifyDataComponentType(nonValuedComponentType);

        toSet.add(nonValuedComponentType);
        toUnset.remove(nonValuedComponentType);
    }

    public void unsetNonValuedComponent(DataComponentType.NonValued nonValuedComponentType) {
        verifyDataComponentType(nonValuedComponentType);

        toSet.remove(nonValuedComponentType);
        toUnset.add(nonValuedComponentType);
    }

    public boolean isNonValuedComponentSet(DataComponentType.NonValued nonValuedComponentType) {
        return toSet.contains(nonValuedComponentType);
    }

    public boolean isNonValuedComponentUnSet(DataComponentType.NonValued nonValuedComponentType) {
        return toUnset.contains(nonValuedComponentType);
    }

    public ItemProperties itemModel(@Nullable ItemModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public @Nullable ItemModel itemModel() {
        return itemModel;
    }

    public ItemProperties customBlock(Key customBlock) {
        this.customBlock = customBlock;
        return this;
    }

    public @Nullable Key customBlock() {
        return customBlock;
    }

}
