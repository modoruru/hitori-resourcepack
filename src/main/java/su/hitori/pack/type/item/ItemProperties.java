package su.hitori.pack.type.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.util.Text;
import su.hitori.pack.type.ItemModel;

import java.util.*;

public final class ItemProperties {

    private final Key key;
    private Component name;

    private Material type = Material.POPPED_CHORUS_FRUIT;
    private final List<String> lore = new ArrayList<>();
    private int maxStackSize = CustomItem.DISABLED;
    private int durability = CustomItem.DISABLED;
    private Boolean unbreakable;
    private Boolean fireResistant;
    private Boolean hideTooltip;
    private Set<ItemFlag> flags = Set.of();
    private Boolean glintOverride;
    private Consumable consumable;
    private FoodProperties food;
    private ToolComponent tool;
    private JukeboxPlayable jukebox;
    private EquippableComponent equippableComponent;
    private PotionContents potion;
    private ItemRarity rarity;
    private final Map<Attribute, AttributeModifier> attributeModifiers = new HashMap<>();

    private ItemModel itemModel;
    private Key customBlock;

    public ItemProperties(@NotNull Key key) {
        this.key = key;
        setNameById();
    }

    private void setNameById() {
        name = Component.translatable(String.format("item.%s.%s", key.namespace(), key.value())).fallback(key.value());
    }

    public Key key() {
        return key;
    }

    public ItemProperties name(String name) {
        if (name == null) setNameById();
        else this.name = Text.create(name);
        return this;
    }

    public Component name() {
        return name;
    }

    public ItemProperties type(@NotNull Material type) {
        this.type = type;
        return this;
    }

    public Material type() {
        return type;
    }

    public ItemProperties lore(Collection<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
        return this;
    }

    public Collection<String> lore() {
        return this.lore;
    }

    public ItemProperties maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize <= 0 ? CustomItem.DISABLED : maxStackSize;
        return this;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public ItemProperties durability(int durability) {
        this.durability = durability <= 0 ? CustomItem.DISABLED : durability;
        return this;
    }

    public int durability() {
        return durability;
    }

    public ItemProperties unbreakable(Boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public Boolean unbreakable() {
        return unbreakable;
    }

    public ItemProperties fireResistant(Boolean fireResistant) {
        this.fireResistant = fireResistant;
        return this;
    }

    public Boolean fireResistant() {
        return fireResistant;
    }

    public ItemProperties hideTooltip(Boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public Boolean hideTooltip() {
        return hideTooltip;
    }

    public ItemProperties flags(Set<ItemFlag> flags) {
        if (flags != null && !flags.isEmpty()) this.flags = EnumSet.copyOf(flags);
        return this;
    }

    public ItemProperties flags(ItemFlag... flags) {
        return flags(new HashSet<>(Arrays.asList(flags)));
    }

    public Set<ItemFlag> flags() {
        return flags;
    }

    public ItemProperties glintOverride(Boolean glintOverride) {
        this.glintOverride = glintOverride;
        return this;
    }

    public Boolean glintOverride() {
        return glintOverride;
    }

    public ItemProperties consumable(Consumable consumable) {
        this.consumable = consumable;
        return this;
    }

    public ItemProperties consumable(float consumeSeconds, ItemUseAnimation animation, Holder<SoundEvent> sound, boolean hasConsumeParticles, List<ConsumeEffect> onConsumeEffects) {
        return consumable(new Consumable(consumeSeconds, animation, sound, hasConsumeParticles, onConsumeEffects));
    }

    public ItemProperties consumable(Consumable.Builder builder) {
        return consumable(builder.build());
    }

    public Consumable consumable() {
        return consumable;
    }

    public ItemProperties food(FoodProperties food) {
        this.food = food;
        return this;
    }

    public ItemProperties food(int nutrition, float saturationModifier, boolean canAlwaysEat) {
        return food(new FoodProperties(nutrition, FoodConstants.saturationByModifier(nutrition, saturationModifier), canAlwaysEat));
    }

    public FoodProperties food() {
        return food;
    }

    public ItemProperties tool(ToolComponent tool) {
        this.tool = tool;
        return this;
    }

    public ToolComponent tool() {
        return tool;
    }

    public ItemProperties jukebox(JukeboxPlayable jukebox) {
        this.jukebox = jukebox;
        return this;
    }

    public JukeboxPlayable jukebox() {
        return jukebox;
    }

    public ItemProperties equipment(EquippableComponent equippableComponent) {
        this.equippableComponent = equippableComponent;
        return this;
    }

    public EquippableComponent equipment() {
        return equippableComponent;
    }

    public ItemProperties potion(PotionContents potion) {
        this.potion = potion;
        return this;
    }

    public PotionContents potion() {
        return potion;
    }

    public ItemProperties rarity(ItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemRarity rarity() {
        return rarity;
    }

    public ItemProperties addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        this.attributeModifiers.put(attribute, attributeModifier);
        return this;
    }

    public Set<Map.Entry<Attribute, AttributeModifier>> attributeModifiers() {
        return attributeModifiers.entrySet();
    }

    public ItemProperties itemModel(ItemModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public ItemModel itemModel() {
        return itemModel;
    }

    public ItemProperties customBlock(Key customBlock) {
        this.customBlock = customBlock;
        return this;
    }

    public Key customBlock() {
        return customBlock;
    }

}
