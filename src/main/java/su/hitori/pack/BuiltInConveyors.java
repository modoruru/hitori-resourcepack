package su.hitori.pack;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.module.ModuleDescriptor;
import su.hitori.api.registry.Registry;
import su.hitori.api.registry.RegistryAccess;
import su.hitori.api.registry.RegistryKey;
import su.hitori.api.util.UnsafeUtil;
import su.hitori.pack.generation.Generator;
import su.hitori.pack.impl.*;
import su.hitori.pack.type.AssetsSource;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.Sound;
import su.hitori.pack.type.Translations;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.RawBlueprint;
import su.hitori.pack.type.glyph.Glyph;
import su.hitori.pack.type.glyph.GlyphSnapshot;
import su.hitori.pack.type.item.CustomItem;

import java.util.Optional;

public final class BuiltInConveyors implements RegistryAccess {

    public static final RegistryKey<CustomItem> CUSTOM_ITEM = new RegistryKey<>(Key.key("custom_item"), CustomItem.class);
    public static final RegistryKey<Glyph> GLYPH = new RegistryKey<>(Key.key("glyph"), Glyph.class);
    public static final RegistryKey<CustomBlock> CUSTOM_BLOCK = new RegistryKey<>(Key.key("custom_block"), CustomBlock.class);
    public static final RegistryKey<Blueprint> BLUEPRINT = new RegistryKey<>(Key.key("blueprint"), Blueprint.class);

    private final CustomItemConveyor customItemConveyor;
    private final GlyphConveyor glyphConveyor;
    private final CustomBlockConveyor customBlockConveyor;
    private final BlueprintConveyor blueprintConveyor;

    BuiltInConveyors(PackModule packModule) {
        Generator generator = packModule.generator();
        ModuleDescriptor descriptor = packModule.moduleDescriptor();
        generator.addConveyor(descriptor, new ItemModelConveyor(Key.key("item_model")), ItemModel.class);
        generator.addConveyor(descriptor, this.customItemConveyor = new CustomItemConveyor(Key.key("custom_item"), CUSTOM_ITEM), CustomItem.class);
        generator.addConveyor(descriptor, this.glyphConveyor = new GlyphConveyor(packModule, Key.key("glyph"), GLYPH), GlyphSnapshot.class);
        generator.addConveyor(descriptor, new SoundConveyor(Key.key("sound")), Sound.class);
        generator.addConveyor(descriptor, new TranslationsConveyor(Key.key("translations")), Translations.class);
        generator.addConveyor(descriptor, this.customBlockConveyor = new CustomBlockConveyor(Key.key("custom_block"), CUSTOM_BLOCK), CustomBlock.class);
        generator.addConveyor(descriptor, new AssetsConveyor(Key.key("assets")), AssetsSource.class);
        generator.addConveyor(descriptor, this.blueprintConveyor = new BlueprintConveyor(Key.key("blueprint"), BLUEPRINT), RawBlueprint.class);
    }

    @Override
    public <E extends Keyed> Optional<Registry<@NotNull E>> access(RegistryKey<E> key) throws IllegalAccessError {
        AbstractConveyorWithRegistry<?, ?> conveyor;
        if(key == CUSTOM_ITEM) conveyor = customItemConveyor;
        else if (key == GLYPH) conveyor = glyphConveyor;
        else if (key == CUSTOM_BLOCK) conveyor = customBlockConveyor;
        else if (key == BLUEPRINT) conveyor = blueprintConveyor;
        else throw new IllegalAccessError();

        return Optional.of(conveyor.registry())
                .map(UnsafeUtil::cast);
    }

}
