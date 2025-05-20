package edn.stratodonut.drivebywire.util;

import edn.stratodonut.drivebywire.DriveByWireMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class HubItem {

    public static void putHub(ItemStack itemStack, String uuid) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putString(DriveByWireMod.UUID_KEY, uuid);
        itemStack.setTag(nbt);
    }
    
    public static void ifHubPresent(ItemStack itemStack, Consumer<BlockPos> runnable) {
        CompoundTag tag = itemStack.getTag();
        if (itemStack.hasTag() && tag != null && tag.contains(DriveByWireMod.UUID_KEY, Tag.TAG_STRING)) {
            BlockPos hubPos = DriveByWireMod.hubs.get(tag.getString(DriveByWireMod.UUID_KEY));
            if (hubPos != null) {
                runnable.accept(hubPos);
            }
        }
    }
}
