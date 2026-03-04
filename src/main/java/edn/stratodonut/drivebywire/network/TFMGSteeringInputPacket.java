package edn.stratodonut.drivebywire.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.drivebywire.compat.TFMGSteeringState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class TFMGSteeringInputPacket extends SimplePacketBase {

    private final BlockPos pos;
    float steering;

    public TFMGSteeringInputPacket(BlockPos pos, float steering) {
        this.pos = pos;
        this.steering = steering;
    }

    public TFMGSteeringInputPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.steering = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeFloat(steering);
    }

    @Override
    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            TFMGSteeringState.set(pos, steering);
        });
        return true;
    }
}