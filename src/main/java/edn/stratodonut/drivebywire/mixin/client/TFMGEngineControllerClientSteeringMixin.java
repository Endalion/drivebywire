package edn.stratodonut.drivebywire.mixin.client;

import com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlockEntity;
import edn.stratodonut.drivebywire.WirePackets;
import edn.stratodonut.drivebywire.client.TFMGClientControllerTracker;
import edn.stratodonut.drivebywire.network.TFMGSteeringInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Pseudo
@Mixin(EngineControllerBlockEntity.class)
public abstract class TFMGEngineControllerClientSteeringMixin {

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void drivebywire$captureSteering(CallbackInfo ci) {
        EngineControllerBlockEntity self = (EngineControllerBlockEntity)(Object)this;
        Level level = self.getLevel();

        if (level == null || !level.isClientSide) return;

        BlockPos active = TFMGClientControllerTracker.get();
        if (active == null || !active.equals(self.getBlockPos())) return;

        float angle = self.steeringWheelAngle.getValue();

        final float CENTER_EPS = 4.0f;

        // force-center early
        if (Math.abs(angle) < CENTER_EPS) {
            angle = 0.0f;
        }

        boolean left  = angle < 0;
        boolean right = angle > 0;

        WirePackets.getChannel().sendToServer(
                new TFMGSteeringInputPacket(self.getBlockPos(), left, right)
        );
    }
}
