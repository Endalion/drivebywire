package edn.stratodonut.drivebywire;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import edn.stratodonut.drivebywire.blocks.ControllerHubBlock;
import edn.stratodonut.drivebywire.blocks.TFMGEngineControllerHubBlock;
import edn.stratodonut.drivebywire.blocks.TweakedControllerHubBlock;
import edn.stratodonut.drivebywire.blocks.WireNetworkBackupBlock;
import net.minecraft.resources.ResourceLocation;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static edn.stratodonut.drivebywire.DriveByWireMod.REGISTRATE;

public class WireBlocks {
    static {
        REGISTRATE.setCreativeTab(WireCreativeTabs.BASE_CREATIVE_TAB);
    }

    public static final BlockEntry<WireNetworkBackupBlock> BACKUP_BLOCK =
            REGISTRATE.block("backup_block", WireNetworkBackupBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(false))
                    .simpleItem()
                    .register();

    public static final BlockEntry<ControllerHubBlock> CONTROLLER_HUB =
            REGISTRATE.block("controller_hub", ControllerHubBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .transform(axeOrPickaxe())
                    .simpleItem()
                    .register();

    public static final BlockEntry<TweakedControllerHubBlock> TWEAKED_HUB =
            REGISTRATE.block("tweaked_controller_hub", TweakedControllerHubBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .transform(axeOrPickaxe())
                    .simpleItem()
                    .register();

    public static final BlockEntry<TFMGEngineControllerHubBlock> TFMG_ENGINE_CONTROLLER_HUB =
            REGISTRATE.block("tfmg_engine_controller_hub", TFMGEngineControllerHubBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .transform(axeOrPickaxe())
                    .simpleItem()
                    .register();

    public static void register() {}
}
