package edn.stratodonut.drivebywire.client;

import com.tterrag.registrate.util.entry.RegistryEntry;
import edn.stratodonut.drivebywire.DriveByWireMod;
import edn.stratodonut.drivebywire.LoadedMods;
import edn.stratodonut.drivebywire.WireBlocks;
import edn.stratodonut.drivebywire.WireItems;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class PonderTags {
    public static final ResourceLocation SIGNAL_SOURCES = DriveByWireMod.getResource("signal_sources");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {

        PonderTagRegistrationHelper<RegistryEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                CatnipServices.REGISTRIES::getKeyOrThrow);

        helper.registerTag(SIGNAL_SOURCES)
                .addToIndex()
                .item(WireItems.WIRE.get(), true, false)
                .title("Multi-channel Sources")
                .description("Components which emit signals on multiple channels")
                .register();

        // Awesome
        HELPER.addToTag(SIGNAL_SOURCES)
                .add(WireItems.WIRE);
        itemHelper.addToTag(SIGNAL_SOURCES)
                .add(Blocks.LECTERN);
        HELPER.addToTag(SIGNAL_SOURCES)
                .add(WireBlocks.CONTROLLER_HUB);

        LoadedMods.TWEAKED_CONTROLLERS.runIfInstalled(() -> () -> {
            HELPER.addToTag(SIGNAL_SOURCES).add(WireBlocks.TWEAKED_HUB);
        });
    }
}
