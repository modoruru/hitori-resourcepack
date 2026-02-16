package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import su.hitori.api.registry.RegistryKey;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.item.CustomItem;

import java.util.Map;

public final class CustomItemConveyor extends AbstractConveyorWithRegistry<CustomItem, CustomItem> {

    public CustomItemConveyor(Key key, RegistryKey<CustomItem> registryKey) {
        super(key, registryKey);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        registry.clear();
        for (Map.Entry<Key, CustomItem> itemEntry : snapshots.entrySet()) {
            registry.register(itemEntry.getKey(), itemEntry.getValue());
        }

        collectingOrGenerating = false;
    }

}
