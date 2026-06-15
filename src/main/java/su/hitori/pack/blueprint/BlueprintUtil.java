package su.hitori.pack.blueprint;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;

@ApiStatus.Internal
public final class BlueprintUtil {

    private static final EntityDataAccessor<Integer> INTERPOLATION_DELAY_DATA = new EntityDataAccessor<>(8, EntityDataSerializers.INT);

    private BlueprintUtil() {
    }

    public static ClientboundAddEntityPacket createEntityPacket(Entity entity) {
        return new ClientboundAddEntityPacket(
                entity.getId(),
                entity.getUUID(),
                entity.getX(), entity.getY(), entity.getZ(),
                entity.getXRot(), entity.getYRot(),
                entity.getType(),
                0,
                entity.getDeltaMovement(),
                entity.getYHeadRot()
        );
    }

    public static ClientboundSetEntityDataPacket createSetEntityDataPacket(Entity entity, boolean onlyDirty) {
        SynchedEntityData entityData = entity.getEntityData();

        var metadata = onlyDirty ? entityData.packDirty() : entityData.packAll();
        if (metadata == null) return null;

        if(entity instanceof Display) {
            metadata = new ArrayList<>(metadata);
            metadata.add(new SynchedEntityData.DataValue<>(INTERPOLATION_DELAY_DATA.id(), INTERPOLATION_DELAY_DATA.serializer(), 0));
        }

        return new ClientboundSetEntityDataPacket(entity.getId(), metadata);
    }

    public static ClientboundSetPassengersPacket createSetPassengersPacket(int vehicle, Collection<Integer> passengers) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeVarInt(vehicle);

        // writing var int array
        int size = passengers.size();
        var iterator = passengers.iterator();
        friendlyByteBuf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            friendlyByteBuf.writeVarInt(iterator.next());
        }

        return ClientboundSetPassengersPacket.STREAM_CODEC.decode(friendlyByteBuf);
    }

}
