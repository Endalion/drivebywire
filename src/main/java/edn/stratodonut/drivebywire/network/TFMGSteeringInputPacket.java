package edn.stratodonut.drivebywire.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.drivebywire.compat.TFMGSteeringState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class TFMGSteeringInputPacket extends SimplePacketBase {

    private final BlockPos pos;
    private final boolean left;
    private final boolean right;

    public TFMGSteeringInputPacket(BlockPos pos, boolean left, boolean right) {
        this.pos = pos;
        this.left = left;
        this.right = right;
    }

    public TFMGSteeringInputPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.left = buf.readBoolean();
        this.right = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(left);
        buf.writeBoolean(right);
    }

    @Override
    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            TFMGSteeringState.set(pos, left, right);
        });
        return true;
    }
}