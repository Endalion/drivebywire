package edn.stratodonut.drivebywire.mixin.client;

import com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerClientHandler;
import edn.stratodonut.drivebywire.client.TFMGClientControllerTracker;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(EngineControllerClientHandler.class)
public class TFMGEngineControllerClientHandlerMixin {

    static {
        System.out.println("[DBW] TFMG EngineControllerClientHandler mixin loaded");
    }

    @Inject(method = "activateInLectern", at = @At("HEAD"), remap = false)
    private static void drivebywire$onActivate(BlockPos pos, CallbackInfo ci) {
        TFMGClientControllerTracker.set(pos);
    }

    @Inject(method = "deactivateInLectern", at = @At("HEAD"), remap = false)
    private static void drivebywire$onDeactivate(CallbackInfo ci) {
        TFMGClientControllerTracker.clear();
    }
}
