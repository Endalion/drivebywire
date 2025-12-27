package edn.stratodonut.drivebywire;

import com.tterrag.registrate.util.entry.RegistryEntry;
import edn.stratodonut.drivebywire.blocks.TFMGEngineControllerHubBlock;
import edn.stratodonut.drivebywire.blocks.TweakedControllerHubBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static edn.stratodonut.drivebywire.DriveByWireMod.REGISTRATE;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class WireCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DriveByWireMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BASE_CREATIVE_TAB = REGISTER.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.drivebywire"))
                    .icon(WireItems.WIRE.asItem()::getDefaultInstance)
                    .displayItems((displayParams, output) -> {
                        for (RegistryEntry<Block> entry : REGISTRATE.getAll(Registries.BLOCK)) {
                            if (include(entry.get())) output.accept(entry.get().asItem());
                        }

                        for (RegistryEntry<Item> entry : REGISTRATE.getAll(Registries.ITEM)) {
                            if (include(entry.get())) output.accept(entry.get());
                        }
                    })
                    .build());

    public static boolean include(Object thing) {
        if (!ModList.get().isLoaded("create_tweaked_controllers")) {
            if (thing instanceof TweakedControllerHubBlock) return false;
        }
        if (!ModList.get().isLoaded("tfmg")) {
            if (thing instanceof TFMGEngineControllerHubBlock) return false;
        }
        return true;
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
