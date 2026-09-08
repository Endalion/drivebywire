package edn.stratodonut.drivebywire.compat.cc;

import dan200.computercraft.api.ForgeComputerCraftAPI;

public class GenericPeripherals {
    public static void register() {
        // Hubs have no BlockEntity, so we use a peripheral provider instead.
        ForgeComputerCraftAPI.registerPeripheralProvider(new HubPeripheralProvider());
    }
}
