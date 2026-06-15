package su.hitori.pack.blueprint;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.Set;
import java.util.UUID;

final class Observer {

    final Set<Integer> entitySent;
    final ServerPlayer serverPlayer;
    UUID cameraUuid;
    GameType originalGameType;

    Observer(Set<Integer> entitySent, ServerPlayer serverPlayer) {
        this.entitySent = entitySent;
        this.serverPlayer = serverPlayer;
    }

    void sendPacket(Packet<?> packet) {
        serverPlayer.connection.connection.send(packet);
    }

}
