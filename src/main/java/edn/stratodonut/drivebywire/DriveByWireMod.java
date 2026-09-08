package edn.stratodonut.drivebywire;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import edn.stratodonut.drivebywire.compat.cc.GenericPeripherals;
import edn.stratodonut.drivebywire.wire.ShipWireNetworkManager;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.valkyrienskies.mod.api.ValkyrienSkies;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("drivebywire")
public class DriveByWireMod
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "drivebywire";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }

    public DriveByWireMod() { onCtor(); }

    public void onCtor() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::onCommonSetup);

        ValkyrienSkies.api().registerAttachment(ValkyrienSkies.api().newAttachmentRegistrationBuilder(ShipWireNetworkManager.class)
                .useLegacySerializer()
                .build()
        );

        // TODO: CHANGE LOGO
        // TODO: Test with audience(?)
        WireCreativeTabs.register(modEventBus);
        WireBlocks.register();
        WireBlockEntities.register();
        WireItems.register();
        WirePackets.registerPackets();

        if (LoadedMods.COMPUTER_CRAFT.isLoaded()) {
            GenericPeripherals.register();
        }

        WireSounds.register(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::onCtorClient);
    }

    private void onCtorClient() {
        PonderIndex.addPlugin(new WirePonderPlugin());
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // DO NOTHING
    }

    public static void warn(String format, Object arg) {
        LOGGER.warn(format, arg);
    }

    public static void warn(String format, Object... args) {
        LOGGER.warn(format, args);
    }

    public static ResourceLocation getResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
