package edn.stratodonut.drivebywire

import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.item.ItemDescription
import com.simibubi.create.foundation.item.TooltipHelper
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object DriveByWireMod : ModInitializer {
    const val MOD_ID = "drivebywire"
    private val LOGGER = LoggerFactory.getLogger(MOD_ID)
    
    val REGISTRATE = CreateRegistrate.create(MOD_ID).apply {
        setTooltipModifierFactory { item ->
            ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
        }
    }

    override fun onInitialize() {
        LOGGER.info("Initializing Drive-By-Wire")
        
        WireCreativeTabs.register()
        WireBlocks.register()
        WireBlockEntities.register()
        WireItems.register()
        WirePackets.registerPackets()
        WireSounds.register()
    }

    fun warn(format: String, vararg args: Any?) {
        LOGGER.warn(format, *args)
    }

    fun getResource(path: String): Identifier {
        return Identifier(MOD_ID, path)
    }
}