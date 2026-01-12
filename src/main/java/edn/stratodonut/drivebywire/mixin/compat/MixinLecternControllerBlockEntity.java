package edn.stratodonut.drivebywire.mixin.compat;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.redstone.link.controller.LecternControllerBlockEntity;
import edn.stratodonut.drivebywire.util.HubItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static edn.stratodonut.drivebywire.util.HubItem.TAG;

@Mixin(LecternControllerBlockEntity.class)
public class MixinLecternControllerBlockEntity {

    @Unique
    private BlockPos drivebywire$blockPos;

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private void read(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (compound.contains(TAG)) {
            this.drivebywire$blockPos = BlockPos.of(compound.getLong(TAG));
        }
    }

    @Inject(method = "write", at = @At("TAIL"), remap = false)
    private void write(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (this.drivebywire$blockPos != null) {
            compound.putLong(TAG, this.drivebywire$blockPos.asLong());
        }
    }

    @Inject(method = "writeSafe", at = @At("TAIL"), remap = false)
    private void writeSafe(CompoundTag compound, CallbackInfo ci) {
        if (this.drivebywire$blockPos != null) {
            compound.putLong(TAG, this.drivebywire$blockPos.asLong());
        }
    }

    @Inject(method = "setController", at = @At("HEAD"), remap = false)
    private void setController(ItemStack newController, CallbackInfo ci) {
        if (newController != null && AllItems.LINKED_CONTROLLER.get() == newController.getItem()) {
            HubItem.ifHubPresent(newController, (pos) -> this.drivebywire$blockPos = pos);
        }
    }

    // the oldest version 0.5.1 don't have the method, so set require 0
    @Inject(method = "createLinkedController", at = @At("RETURN"), remap = false, require = 0)
    private void createLinkedController(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if ((stack.hasTag() && !stack.getTag().contains(TAG, Tag.TAG_LONG)) && this.drivebywire$blockPos != null) {
            HubItem.putHub(stack, this.drivebywire$blockPos);
        }
    }
}
