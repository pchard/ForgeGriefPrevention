/*
 * Adapted from http://www.minecraftforum.net/topic/1889473-legobear154s-mods/
 */
package me.peerko.forgegriefprotection.event;

import java.util.ArrayList;

import me.peerko.forgegriefprotection.ProtBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.IPlayerTracker;

public class PlayerEvents implements IPlayerTracker {
    
    public static ArrayList<ProtBase> toolProtections = new ArrayList<ProtBase>();
    
    @ForgeSubscribe(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent ev) {
        if (ev.isCanceled()) {
            return;
        }

        EntityPlayer player = ev.entityPlayer;
        /*
        Resident r = source().getOrMakeResident(ev.entityPlayer);
        if (ev.action == Action.RIGHT_CLICK_AIR
                || ev.action == Action.RIGHT_CLICK_BLOCK) {
            if (r.pay.tryPayByHand()) {
                ev.setCanceled(true);
                r.onlinePlayer.stopUsingItem();
                return;
            }
        } else {
            r.pay.cancelPayment();
        }*/

        if (!ProtectionEvents.instance.itemUsed(player,ev)) {
            ev.setCanceled(true);
            player.stopUsingItem();
            return;
        }
        //Permissions perm = Permissions.Build;
        /*int x = ev.x, y = ev.y, z = ev.z;
        Action action = ev.action;

        if (action == Action.RIGHT_CLICK_AIR) // entity or air click
        {
            if (ev.entityPlayer.getHeldItem() != null
                    && ev.entityPlayer.getHeldItem().getItem() != null) {
                Item item = ev.entityPlayer.getHeldItem().getItem();
                /*MovingObjectPosition pos = Utils
                        .getMovingObjectPositionFromPlayer(
                                r.onlinePlayer.worldObj, r.onlinePlayer, false);
                if (pos == null) {
                    if (item instanceof ItemBow
                            || item instanceof ItemEgg
                            || item instanceof ItemPotion
                            || item instanceof ItemFishingRod
                            || item instanceof ItemExpBottle
                            || item instanceof ItemEnderEye
                            || item.getClass().getSimpleName()
                                    .equalsIgnoreCase("ItemNanoBow")) {
                        //perm = Permissions.Build;
                    } else {
                        return;
                    }

                    x = (int) ev.entityPlayer.posX;
                    y = (int) ev.entityPlayer.posY;
                    z = (int) ev.entityPlayer.posZ;
                } else {
                    action = Action.RIGHT_CLICK_BLOCK;
                    if (pos.typeOfHit == EnumMovingObjectType.ENTITY) {
                        x = (int) pos.entityHit.posX;
                        y = (int) pos.entityHit.posY;
                        z = (int) pos.entityHit.posZ;
                    } else {
                        x = pos.blockX;
                        y = pos.blockY;
                        z = pos.blockZ;
                    }
                }
            } else {
                return;
            }
        }*/

        //if (action == Action.LEFT_CLICK_BLOCK)
        	//Logger.getLogger("Minecraft").log(Level.INFO, "Check left click");
        //TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(ev.entityPlayer.dimension, x, y, z);
        /*if (!r.canInteract(targetBlock, perm)) {
            // Log.info("Permission denied %s %s", perm, action);
            // see if its a allowed block
            if (perm == Permissions.Build && action == Action.LEFT_CLICK_BLOCK) {
                World w = ev.entityPlayer.worldObj;
                // Log.info("Block is %s:%s", w.getBlockId(x, y, z),
                // w.getBlockMetadata(x, y, z));
                if (ItemIdRange.contains(MyTown.instance.leftClickAccessBlocks,
                        w.getBlockId(x, y, z), w.getBlockMetadata(x, y, z))) {
                    perm = Permissions.Access;
                    if (r.canInteract(targetBlock, perm)) {
                        return;
                    }
                }
            }

            r.onlinePlayer.stopUsingItem();
            ev.setCanceled(true);
            if (perm == Permissions.Access) {
                MyTown.sendChatToPlayer(ev.entityPlayer, Term.ErrPermCannotAccessHere.toString());
            } else {
                MyTown.sendChatToPlayer(ev.entityPlayer, Term.ErrPermCannotAccessHere.toString());
            }
        }*/
    }

    /*
    @ForgeSubscribe
    public void pickup(EntityItemPickupEvent ev) {
        if (ev.isCanceled()) {
            return;
        }

        Resident r = source().getOrMakeResident(ev.entityPlayer);

        if (!r.canInteract(ev.item)) {
            long time = System.currentTimeMillis();
            if (time > r.pickupWarningCooldown) {
                MyTown.sendChatToPlayer(ev.entityPlayer, Term.ErrPermCannotPickup.toString());
                r.pickupWarningCooldown = time + Resident.pickupSpamCooldown;
            }
            ev.setCanceled(true);
        }
    }*/

    @ForgeSubscribe
    public void entityAttack(AttackEntityEvent ev) {
        if (ev.isCanceled()) {
            return;
        }

        if (!ProtBase.canAttack(ev.entityPlayer, ev.target)) {
            ev.setCanceled(true);
        }
    }

    /*
    @ForgeSubscribe
    public void onLivingAttackEvent(LivingAttackEvent ev) {
        if (ev.isCanceled() || ev.entity != null) {
            return;
        }

        if (ev.entityLiving instanceof EntityPlayer) {
            Resident t = source().getOrMakeResident(
                    (EntityPlayer) ev.entityLiving);

            if (ev.source.getEntity() != null
                    && !t.canBeAttackedBy(ev.source.getEntity())
                    || ev.source.getSourceOfDamage() != null
                    && !t.canBeAttackedBy(ev.source.getSourceOfDamage())) {
                ev.setCanceled(true);
            }
        }

        Entity target = ev.entity;
        Resident attacker = null;

        if (ev.source.getEntity() != null
                && ev.source.getEntity() instanceof EntityPlayer) {
            attacker = source().getOrMakeResident(
                    (EntityPlayer) ev.source.getEntity());
        }

        if (ev.source.getSourceOfDamage() != null
                && ev.source.getSourceOfDamage() instanceof EntityPlayer) {
            attacker = source().getOrMakeResident(
                    (EntityPlayer) ev.source.getSourceOfDamage());
        }

        if (!attacker.isOnline()) {
            ev.setCanceled(true);
            return;
        }

        if (!attacker.canAttack(target)) {
            MyTown.sendChatToPlayer(attacker.onlinePlayer, Term.ErrPermCannotAttack.toString());
            ev.setCanceled(true);
            return;
        }
    }*/

    /*
    @ForgeSubscribe
    public void entityInteract(EntityInteractEvent ev) {
        if (ev.isCanceled()) {
            return;
        }

        Resident r = source().getOrMakeResident(ev.entityPlayer);

        if (!r.canInteract(ev.target)) {
            MyTown.sendChatToPlayer(ev.entityPlayer, Term.ErrPermCannotInteract.toString());
            ev.setCanceled(true);
        }
    }*/

    /*
    @ForgeSubscribe
    public void minecartCollision(MinecartCollisionEvent ev) {
        if (!(ev.collider instanceof EntityPlayer)) {
            return;
        }

        Resident r = source().getOrMakeResident((EntityPlayer) ev.collider);

        TownBlock t = source().getBlock(r.onlinePlayer.dimension,
                ev.minecart.chunkCoordX, ev.minecart.chunkCoordZ);

        if (t == null || t.town() == null || t.town() == r.town()
                || t.settings.allowCartInteraction) {
            return;
        }

        long time = System.currentTimeMillis();
        if (t.town().minecraftNotificationTime < time) {
            t.town().minecraftNotificationTime = time
                    + Town.dontSendCartNotification;
            t.town().sendNotification(Level.WARNING,
                    Term.MinecartMessedWith.toString());
        }
    }*/

    /*
    private MyTownDatasource source() {
        return MyTownDatasource.instance;
    }*/

    @Override
    public void onPlayerLogin(EntityPlayer player) {
        /*/ load the resident
        Resident r = source().getOrMakeResident(player);

        if (!WorldBorder.instance.isWithinArea(player)) {
            Log.warning(String
                    .format("Player %s logged in over the world edge %s (%s, %s, %s). Sending to spawn.",
                            r.name(), player.dimension, player.posX,
                            player.posY, player.posZ));
            r.respawnPlayer();
        }

        TownBlock t = source().getBlock(r.onlinePlayer.dimension,
                player.chunkCoordX, player.chunkCoordZ);

        r.location = t != null && t.town() != null ? t.town() : null;
        r.location2 = t != null && t.town() != null ? t.owner() : null;

        if (!r.canInteract(t, (int) player.posY, Permissions.Enter)) {
            Log.warning(String
                    .format("Player %s logged in at a enemy town %s (%s, %s, %s, %s) with bouncing on. Sending to spawn.",
                            r.name(), r.location.name(), player.dimension,
                            player.posX, player.posY, player.posZ));
            r.respawnPlayer();
        }

        if (r.town() != null) {
            r.town().notifyPlayerLoggedOn(r);
        }

        r.loggedIn();*/
    }

    @Override
    public void onPlayerLogout(EntityPlayer player) {
        /*Resident res = source().getOrMakeResident(player);

        if (res.town() != null) {
            res.town().notifyPlayerLoggedOff(res);
        }

        res.loggedOf();*/
    }

    @Override
    public void onPlayerChangedDimension(EntityPlayer player) {}

    @Override
    public void onPlayerRespawn(EntityPlayer player) {}

    /*
    @ForgeSubscribe
    public void serverChat(ServerChatEvent ev) {
        if (ev.isCanceled() || ev.message == null
                || ev.message.trim().length() < 1 || !Formatter.formatChat) {
            return;
        }

        ev.setCanceled(true);
        Resident res = source().getOrMakeResident(ev.player);
        CmdChat.sendToChannelFromDirectTalk(res, ev.message, res.activeChannel,
                false);
    }

    @ForgeSubscribe
    public void livingUpdate(LivingUpdateEvent ev) {
        if (ev.isCanceled() || !(ev.entityLiving instanceof EntityPlayer)) {
            return;
        }

        // so we don't re-link to player to be online
        // as this is called after the player logs off
        Resident res = source().getOrMakeResident(
                (EntityPlayer) ev.entityLiving);

        if (res != null && res.isOnline()) {
            res.update();
        }
    }*/
}
