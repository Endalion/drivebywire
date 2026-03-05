package edn.stratodonut.drivebywire;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.function.Supplier;

public enum LoadedMods {
    TWEAKED_CONTROLLERS("create_tweaked_controllers");

    private final String id;
    LoadedMods(String mod_id) { id = mod_id; }

    public ResourceLocation relative(String path) {
        return new ResourceLocation(id, path);
    }

    public boolean isLoaded() {
        return LoadingModList.get().getModFileById(id) != null;
    }

    public void runIfInstalled(Supplier<Runnable> runnable) {
        if (isLoaded()) {
            runnable.get().run();
        }
    }
}
