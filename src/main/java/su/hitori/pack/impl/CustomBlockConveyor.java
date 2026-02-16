package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import su.hitori.api.registry.RegistryKey;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.block.CustomBlock;

import java.util.Map;

public final class CustomBlockConveyor extends AbstractConveyorWithRegistry<CustomBlock, CustomBlock> {

    public CustomBlockConveyor(Key key, RegistryKey<CustomBlock> registryKey) {
        super(key, registryKey);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        registry.clear();
        for (Map.Entry<Key, CustomBlock> blockEntry : snapshots.entrySet()) {
            registry.register(blockEntry.getKey(), blockEntry.getValue());
        }

        collectingOrGenerating = false;
    }

}
