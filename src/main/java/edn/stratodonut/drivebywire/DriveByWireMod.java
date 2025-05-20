package edn.stratodonut.drivebywire;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("drivebywire")
public class DriveByWireMod
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "drivebywire";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public static final String UUID_KEY = DriveByWireMod.MOD_ID + "$ControllerHubUUID";
    public static final ConcurrentHashMap<String, BlockPos> hubs = new ConcurrentHashMap<>();

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE));
    }

    public DriveByWireMod() { onCtor(); }

    public void onCtor() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::onCommonSetup);

        // TODO: CHANGE LOGO
        // TODO: Test with audience(?)
        WireCreativeTabs.register(modEventBus);
        WireBlocks.register();
        WireBlockEntities.register();
        WireItems.register();
        WirePackets.registerPackets();

        WireSounds.register(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> WirePonders::register);
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
