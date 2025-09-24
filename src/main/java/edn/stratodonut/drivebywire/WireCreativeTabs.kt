package edn.stratodonut.drivebywire

import com.simibubi.create.foundation.utility.Components
import edn.stratodonut.drivebywire.blocks.TweakedControllerHubBlock
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object WireCreativeTabs {
    val BASE_CREATIVE_TAB: CreativeModeTab = FabricItemGroup.builder()
        .title(Components.translatable("itemGroup.drivebywire"))
        .icon { WireItems.WIRE.asItem().defaultInstance }
        .displayItems { displayParams, output ->
            DriveByWireMod.REGISTRATE.getAll(Registries.BLOCK).forEach { entry ->
                if (include(entry.get())) {
                    output.accept(entry.get().asItem())
                }
            }
            
            DriveByWireMod.REGISTRATE.getAll(Registries.ITEM).forEach { entry ->
                if (include(entry.get())) {
                    output.accept(entry.get())
                }
            }
        }
        .build()

    fun include(thing: Any): Boolean {
        if (!FabricLoader.getInstance().isModLoaded("create_tweaked_controllers")) {
            if (thing is TweakedControllerHubBlock) return false
        }
        return true
    }

    fun register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DriveByWireMod.getResource("base"), BASE_CREATIVE_TAB)
    }
}