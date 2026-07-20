package su.hitori.pack.block.level;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

final class ItemSourceDataType implements PersistentDataType<byte[], ItemStack> {

    final static ItemSourceDataType TYPE = new ItemSourceDataType();

    private ItemSourceDataType() {}

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<ItemStack> getComplexType() {
        return ItemStack.class;
    }

    @Override
    public byte[] toPrimitive(ItemStack complex, PersistentDataAdapterContext context) {
        return complex.serializeAsBytes();
    }

    @Override
    public ItemStack fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        return ItemStack.deserializeBytes(primitive);
    }

}
