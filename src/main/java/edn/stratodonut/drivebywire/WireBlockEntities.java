package edn.stratodonut.drivebywire;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import edn.stratodonut.drivebywire.blocks.ControllerHubBlockEntity;
import edn.stratodonut.drivebywire.blocks.WireNetworkBackupBlockEntity;

import static edn.stratodonut.drivebywire.DriveByWireMod.REGISTRATE;

public class WireBlockEntities {
    public static final BlockEntityEntry<WireNetworkBackupBlockEntity> BACKUP_BLOCK_ENTITY = REGISTRATE
            .blockEntity("backup_block", WireNetworkBackupBlockEntity::new)
            .validBlocks(WireBlocks.BACKUP_BLOCK)
            .register();

    public static final BlockEntityEntry<ControllerHubBlockEntity> CONTROLLER_HUB_BLOCK_ENTITY = REGISTRATE
            .blockEntity("controller_hub", ControllerHubBlockEntity::new)
            .validBlocks(WireBlocks.CONTROLLER_HUB, WireBlocks.TWEAKED_HUB)
            .register();

    public static void register() {}
}
