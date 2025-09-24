package edn.stratodonut.drivebywire

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent

object WireSounds {
    val PLUG_IN = registerSoundEvent("plug_in")
    val PLUG_OUT = registerSoundEvent("plug_out")

    private fun registerSoundEvent(name: String): SoundEvent {
        val id = DriveByWireMod.getResource(name)
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id))
    }

    fun register() {
        // Sounds are registered in the object initialization
    }
}