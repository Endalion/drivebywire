package edn.stratodonut.drivebywire;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import edn.stratodonut.drivebywire.client.PonderScenes;
import edn.stratodonut.drivebywire.client.PonderTags;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class WirePonders {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(WireItems.WIRE)
                .addStoryBoard("wires", PonderScenes::basicTut, PonderTags.SIGNAL_SOURCES)
                .addStoryBoard("lectern", PonderScenes::lecternTut, PonderTags.SIGNAL_SOURCES);

        HELPER.forComponents(WireBlocks.BACKUP_BLOCK)
                .addStoryBoard("save", PonderScenes::saveTut)
                .addStoryBoard("link", PonderScenes::linkTut);
    }
}
