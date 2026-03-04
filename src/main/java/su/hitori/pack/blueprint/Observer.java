package su.hitori.pack.blueprint;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

record Observer(Set<Integer> entitySent, ServerPlayer serverPlayer) {

    void sendPacket(Packet<?> packet) {
        serverPlayer.connection.connection.send(packet);
    }

}
