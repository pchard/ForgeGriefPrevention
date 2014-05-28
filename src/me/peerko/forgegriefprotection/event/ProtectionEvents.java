/*
 * Adapted from http://www.minecraftforum.net/topic/1889473-legobear154s-mods/
 */
package me.peerko.forgegriefprotection.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.peerko.forgegriefprotection.ProtBase;
import me.peerko.forgegriefprotection.mods.BuildCraft;
import me.peerko.forgegriefprotection.mods.ComputerCraft;
import me.peerko.forgegriefprotection.mods.ExtraUtilities;
import me.peerko.forgegriefprotection.mods.IndustrialCraft;
import me.peerko.forgegriefprotection.mods.ModularPowersuits;
import me.peerko.forgegriefprotection.mods.PortalGun;
import me.peerko.forgegriefprotection.mods.RotaryCraft;
import me.peerko.forgegriefprotection.mods.TinkererConstruct;
//import me.peerko.forgegriefprotection.mods.ThaumCraft;
//import me.peerko.forgegriefprotection.mods.ThaumicTinkerer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ProtectionEvents implements ITickHandler {
    public static ArrayList<ProtBase> entityProtections = new ArrayList<ProtBase>();
    public static ArrayList<ProtBase> tileProtections = new ArrayList<ProtBase>();
    public static ArrayList<ProtBase> toolProtections = new ArrayList<ProtBase>();

    public static ProtectionEvents instance = new ProtectionEvents();

    
    
    //public Resident lastOwner = null;
    public boolean enabled = true;
    public ArrayList<Entity> toRemove = new ArrayList<Entity>();
    public ArrayList<TileEntity> toRemoveTile = new ArrayList<TileEntity>();
    public boolean loaded = false;
    //@SuppressWarnings("rawtypes")
    //private List<Class> npcClasses = null;
    public boolean dynamicEnabling = true;

    public ProtectionEvents() {
        ProtectionEvents.entityProtections.addAll(Arrays.asList(new ProtBase[] {
                //Creeper.instance, Mobs.instance, TNT.instance,
                //ThaumCraft.instance, PortalGun.instance,
                IndustrialCraft.instance, ModularPowersuits.instance, RotaryCraft.instance
                /*, ArsMagica.instance, SteveCarts.instance,
                RailCraft.instance, TrainCraft.instance, Mekanism.instance,
                */ }));
        ProtectionEvents.tileProtections.addAll(Arrays.asList(new ProtBase[] {
                /*BuildCraft.instance,*/ ComputerCraft.instance, ExtraUtilities.instance, RotaryCraft.instance //RedPower.instance,
                /*ThaumCraft.instance*/ }));
        ProtectionEvents.toolProtections.addAll(Arrays.asList(new ProtBase[] {
                /*BuildCraft.instance,*/ ComputerCraft.instance, TinkererConstruct.instance,//RedPower.instance, ArsMagica.instance, 
                /*ThaumCraft.instance, ThaumicTinkerer.instance*/ }));
    }

    /*
     * public static ProtBase[] entityProtections = new ProtBase[] {
     * Creeper.instance, Mobs.instance, TNT.instance, ThaumCraft.instance,
     * //ArsMagica.instance, PortalGun.instance, IndustrialCraft.instance,
     * SteveCarts.instance, RailCraft.instance, TrainCraft.instance,
     * Mekanism.instance, ModularPowersuits.instance };
     * 
     * public static ProtBase[] tileProtections = new ProtBase[] {
     * BuildCraft.instance, RedPower.instance, ComputerCraft.instance,
     * ThaumCraft.instance };
     * 
     * public static ProtBase[] toolProtections = new ProtBase[] {
     * SingleBlockTools.instance, RangedTools.instance, ArsMagica.instance,
     * ThaumCraft.instance };
     */

    public static void addEntityProtection(ProtBase protection) {
        entityProtections.add(protection);
    }

    public static void addTileProtection(ProtBase protection) {
        tileProtections.add(protection);
    }

    public static void addToolProtection(ProtBase protection) {
        toolProtections.add(protection);
    }

    public boolean itemUsed(EntityPlayer player, PlayerInteractEvent ev) {
        try {
            boolean kill = false;

            ItemStack item = player.getHeldItem();
            if (item == null) {
                return true;
            }

            Item tool = item.getItem();
            if (tool == null) {
                return true;
            }

            /*
            // Always allow the usage of cart type items
            if (ItemIdRange.contains(MyTown.instance.carts, item)) {
                return true;
            }*/

            // Log.info(String.format("Item click : %s %s %s", r.name(), item,
            // tool.getClass()));

            //ProtBase lastCheck = null;
            //kill = null;
            for (ProtBase prot : toolProtections) {
                if (prot.enabled && prot.isEntityInstance(tool)) {
                    //lastCheck = prot;
                    kill = prot.update(player, tool, item, ev);
                    if (!kill) {
                        return false;
                    }
                }
            }

            /*
            if (!kill) {
                /*String sTool = String.format("[%s] %s", item.itemID
                        + (item.isStackable() && item.getItemDamage() > 0 ? ":"
                                + item.getItemDamage() : ""), tool
                        .getLocalizedName(null));

                //EntityPlayer pl = r.onlinePlayer;
                /*
                Log.severe(String
                        .format("[%s]Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s",
                                lastCheck.getClass().getSimpleName(),
                                pl.username, pl.dimension, (int) pl.posX,
                                (int) pl.posY, (int) pl.posZ, sTool, kill));
                MyTown.sendChatToPlayer(pl, "§4You cannot use that here - " + kill);
                return false;
            }*/
        } catch (Exception er) {
        	Logger.getLogger("Minecraft").log(Level.SEVERE, "Error in player " + player.toString()
                    + " item use check", er);
        }
        return true;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        if (!enabled) {
            return;
        }

        setFields();

        World world = (World) tickData[0];
        Entity e = null;
        TileEntity t = null;
        boolean kill = false;

        toRemove.clear();
        toRemoveTile.clear();

        try {
            for (int i = 0; i < world.loadedEntityList.size(); i++) {
                e = (Entity) world.loadedEntityList.get(i);
                if (e == null || e.isDead) {
                    continue;
                }

                //lastOwner = null;
                kill = false;

                if (e instanceof EntityPlayer) {
                    EntityPlayer pl = (EntityPlayer) e;
                    if (pl.isUsingItem()) {
                        //Resident r = MyTownDatasource.instance.getOrMakeResident(pl);
                        if (!ProtectionEvents.instance.itemUsed(pl, null)) {
                            pl.stopUsingItem();
                            // force update/sync of held item?
                            if (pl instanceof EntityPlayerMP)
                            	((EntityPlayerMP)pl).sendContainerToPlayer(pl.inventoryContainer);
                        }
                    }
                }

                for (ProtBase prot : entityProtections) {
                    if (prot.enabled && prot.isEntityInstance(e)) {
                        if (!prot.update(e)) {
                        	toRemove.add(e);
                            break;
                        }
                    }
                }

                /*
                if (kill != null) {
                    /*if (lastOwner != null) {
                        if (lastOwner.isOnline()) {
                            Log.severe(String
                                    .format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s",
                                            lastOwner.name(),
                                            lastOwner.onlinePlayer.dimension,
                                            (int) lastOwner.onlinePlayer.posX,
                                            (int) lastOwner.onlinePlayer.posY,
                                            (int) lastOwner.onlinePlayer.posZ,
                                            e.toString(), kill));
                            MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
                        } else {
                            Log.severe(String.format(
                                    "Player %s tried to bypass using %s - %s",
                                    lastOwner.name(), e.toString(), kill));
                        }
                    } else {
                        Log.severe(String.format(
                                "Entity %s tried to bypass using %s", e
                                        .toString(), kill));
                    }

                    toRemove.add(e);
                }*/
            }

            e = null;

            for (Entity en : toRemove) {
                world.removeEntity(en);
            }

            for (int i = 0; i < world.loadedTileEntityList.size(); i++) {
                t = (TileEntity) world.loadedTileEntityList.get(i);
                if (t == null) {
                    continue;
                }

                //lastOwner = null;
                kill = false;

                for (ProtBase prot : tileProtections) {
                    if (prot.enabled && prot.isEntityInstance(t)) {
                        kill = prot.update(t);
                        if (!kill) {
                        	toRemoveTile.add(t);
                            break;
                        }
                    }
                }

                /*
                if (kill != null) {
                    String block = String.format(
                            "TileEntity %s @ dim %s, %s,%s,%s", t.getClass()
                                    .toString(),
                            t.worldObj.provider.dimensionId, t.xCoord,
                            t.yCoord, t.zCoord);
                    /*if (lastOwner != null) {
                        if (lastOwner.isOnline()) {
                            Log.severe(String
                                    .format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s",
                                            lastOwner.name(),
                                            lastOwner.onlinePlayer.dimension,
                                            (int) lastOwner.onlinePlayer.posX,
                                            (int) lastOwner.onlinePlayer.posY,
                                            (int) lastOwner.onlinePlayer.posZ,
                                            block, kill));
                            MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
                        } else {
                            Log.severe(String.format(
                                    "Player %s tried to bypass using %s - %s",
                                    lastOwner.name(), block, kill));
                        }
                    } else {
                        Log.severe(String.format(
                                "TileEntity %s tried to bypass using %s",
                                block, kill));
                    }

                    toRemoveTile.add(t);
                }*/
            }

            for (TileEntity en : toRemoveTile) {
                Block.blocksList[en.worldObj.getBlockId(en.xCoord, en.yCoord,
                        en.zCoord)].dropBlockAsItem(en.worldObj, en.xCoord,
                        en.yCoord, en.zCoord, en.worldObj.getBlockMetadata(
                                en.xCoord, en.yCoord, en.zCoord), 0);
                en.worldObj.setBlock(en.xCoord, en.yCoord, en.zCoord, 0);
            }
        } catch (Exception er) {
            String ms = e == null ? t == null ? "#unknown#" : t.toString() : e.toString();
            Logger.getLogger("Minecraft").log(Level.SEVERE, "Error in entity " + ms + " pre-update check", er);
        }
    }

    /*
    @SuppressWarnings("rawtypes")
    public List<Class> getNPCClasses() {
        if (npcClasses == null) {
            npcClasses = Lists.newArrayList((Class) INpc.class);

            try {
                CustomNPCs.addNPCClasses(npcClasses);
            } catch (Throwable t) {

            }
        }

        return npcClasses;
    }*/

    public static List<ProtBase> getProtections() {
        ArrayList<ProtBase> result = new ArrayList<ProtBase>();

        result.addAll(entityProtections);
        result.addAll(tileProtections);
        result.addAll(toolProtections);

        return result;
    }

    private void setFields() {
        if (loaded) {
            return;
        }

        for (ProtBase prot : getProtections()) {
            if (dynamicEnabling) {
                prot.enabled = true;
            }

            if (prot.enabled && !prot.loaded()) {
                try {
                    prot.load();
                } catch (Exception e) {
                    prot.enabled = false;
                    Logger.getLogger("Minecraft").log(Level.INFO, String.format("§f[§1Prot§f]Module %s §4failed §fto load. (%s)",
                            prot.getClass().getSimpleName(), e.getMessage()));

                    if (!dynamicEnabling) {
                        throw new RuntimeException(
                                "ProtectionEvents cannot load "
                                        + prot.getClass().getSimpleName()
                                        + " class. Is " + prot.getMod()
                                        + " loaded?", e);
                    }
                }
            }

            if (dynamicEnabling && prot.enabled) {
                Logger.getLogger("Minecraft").log(Level.INFO, String.format("§f[§1Prot§f]Module %s §2loaded§f.", prot.getClass().getSimpleName()));
            }
        }

        loaded = true;
    }

    public void reload() {
        loaded = false;

        for (ProtBase prot : getProtections()) {
            prot.reload();
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {}

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "MyTown protection event handler";
    }
}
