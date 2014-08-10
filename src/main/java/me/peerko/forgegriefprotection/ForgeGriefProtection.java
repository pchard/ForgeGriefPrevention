package me.peerko.forgegriefprotection;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.peerko.forgegriefprotection.event.PlayerEvents;
import me.peerko.forgegriefprotection.event.ProtectionEvents;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ForgeGriefProtection", name = "ForgeGriefProtection", version = "1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
public class ForgeGriefProtection {

	@Mod.Instance("ForgeGriefProtection")
    public static ForgeGriefProtection instance;

	@Mod.ServerStarted
	public void modsLoaded(FMLServerStartedEvent event) {
		PlayerEvents peh = new PlayerEvents();
		MinecraftForge.EVENT_BUS.register(peh);
        GameRegistry.registerPlayerTracker(peh);
        MinecraftForge.EVENT_BUS.register(ProtectionEvents.instance);
        TickRegistry.registerTickHandler(ProtectionEvents.instance, Side.SERVER);
		Logger.getLogger("Minecraft").log(Level.INFO, "ForgeGriefPrevention loaded.");
		FMLNetworkHandler.instance().findNetworkModHandler(instance).getNetworkId();
	}
}
