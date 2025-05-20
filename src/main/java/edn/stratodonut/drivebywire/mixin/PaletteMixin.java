package edn.stratodonut.drivebywire.mixin;

import edn.stratodonut.drivebywire.DriveByWireMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(StructureTemplate.Palette.class)
public class PaletteMixin {

    @Mutable
    @Shadow
    @Final
    private List<StructureTemplate.StructureBlockInfo> blocks;

    @Inject(method = "<init>", at = {@At("RETURN")})
    private void constructorMixin(List<StructureTemplate.StructureBlockInfo> blockInfoList, CallbackInfo ci) {

        Map<String, String> uuidMap = new HashMap<>();

        for (StructureTemplate.StructureBlockInfo block : blockInfoList) {
            if (block.nbt() == null) continue;
            System.out.println(block.nbt());

            drivebywire$updateIfPresent(block.nbt(), uuidMap);

            if (block.nbt().contains("Controller", Tag.TAG_COMPOUND)) {
                CompoundTag controller = block.nbt().getCompound("Controller");
                if (controller.contains("tag", Tag.TAG_COMPOUND)) {
                    CompoundTag tag = controller.getCompound("tag");
                    drivebywire$updateIfPresent(tag, uuidMap);
                }
            }
        }
        this.blocks = blockInfoList;
    }

    @Unique
    private static void drivebywire$updateIfPresent(CompoundTag tag, Map<String, String> uuids) {
        if (tag.contains(DriveByWireMod.UUID_KEY, Tag.TAG_STRING)) {
            String oldUuid = tag.getString(DriveByWireMod.UUID_KEY);

            if (!uuids.containsKey(oldUuid)) {
                uuids.put(oldUuid, UUID.randomUUID().toString());
            }
            tag.remove(DriveByWireMod.UUID_KEY);
            tag.putString(DriveByWireMod.UUID_KEY, uuids.get(oldUuid));
        }
    }
}
