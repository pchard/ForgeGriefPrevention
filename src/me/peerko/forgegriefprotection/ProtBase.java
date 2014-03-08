/*
 * Adapted from http://www.minecraftforum.net/topic/1889473-legobear154s-mods/
 */
package me.peerko.forgegriefprotection;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;


public abstract class ProtBase {
    public boolean enabled = false;
	public void reload() {}

    public void load() throws Exception {}

    public boolean loaded() {
        return true;
    }

    public boolean isEntityInstance(Item item) {
        return false;
    }

    public boolean isEntityInstance(Entity e) {
        return false;
    }

    public boolean isEntityInstance(TileEntity e) {
        return false;
    }

    /**
     * @return true if allowed
     */
    public boolean update(EntityPlayer r, Item tool, ItemStack item)
            throws Exception {
        throw new Exception("Protection doesn't support Players");
    }

    /**
     * @return true if allowed
     */
    public boolean update(Entity e) throws Exception {
        throw new Exception("Protection doesn't support Entity's");
    }

    /**
     * @return true if allowed
     */
    public boolean update(TileEntity e) throws Exception {
        throw new Exception("Protection doesn't support TileEntity's");
    }

    public abstract String getMod();

    public abstract String getComment();

    public boolean defaultEnabled() {
        return false;
    }

    /*
    public static EntityPlayer getActorFromLocation(int dim, int x, int y, int z, String defaultActor) {
    	org.bukkit.Location tilelocation = null;
    	EntityPlayer defaultPlayer = null;
    	List<WorldServer> worlds = MinecraftServer.getServer().worlds;
    	for (int j = 0; j < worlds.size(); ++j)
        {
            WorldServer worldserver = worlds.get(j);
            if (worldserver.dimension == dim) {
            	tilelocation = new org.bukkit.Location(worldserver.getWorld(),x,y,z);
            	break;
            }
        }
    	if (tilelocation != null) {
    		try {
		    	Object claim = //GriefPrevention.instance.dataStore.getClaimAt(tilelocation, false, null);
		    		getClaimAt.invoke(dataStore, tilelocation, false, clClaim.cast(null));
		    	if (claim != null) {
		    		String owner = (String) claim.getClass().getMethod("getOwnerName").invoke(claim);
		    		EntityPlayer pl = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(owner);
		    		if (pl != null)
		    			return pl;
		    		// else
		    	}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
	    	defaultPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(defaultActor);
    	}
    	if (defaultPlayer == null)
    		return null;
    	return defaultPlayer;
    }*/

    public static MovingObjectPosition getThrowableHitOnNextTick(
            EntityThrowable e) {
        Vec3 var16 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX,
                e.posY, e.posZ);
        Vec3 var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(
                e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
        //MovingObjectPosition var3 = e.worldObj.clip(var16, var2);
        MovingObjectPosition var3 = e.worldObj.rayTraceBlocks_do_do(var16, var2, false, false);
        var16 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY,
                e.posZ);
        var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX,
                e.posY + e.motionY, e.posZ + e.motionZ);

        if (var3 != null) {
            var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(
                    var3.hitVec.xCoord, var3.hitVec.yCoord, var3.hitVec.zCoord);
        }

        Entity var4 = null;
        List<?> var5 = e.worldObj.getEntitiesWithinAABBExcludingEntity(e,
                e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ).expand(
                        1.0D, 1.0D, 1.0D));
        double var6 = 0.0D;
        // EntityLiving var8 = e.getThrower();

        for (int var9 = 0; var9 < var5.size(); ++var9) {
            Entity var10 = (Entity) var5.get(var9);

            if (var10.canBeCollidedWith()) {
                float var11 = 0.3F;
                AxisAlignedBB var12 = var10.boundingBox.expand(var11, var11,
                        var11);
                MovingObjectPosition var13 = var12.calculateIntercept(var16,
                        var2);

                if (var13 != null) {
                    double var14 = var16.distanceTo(var13.hitVec);

                    if (var14 < var6 || var6 == 0.0D) {
                        var4 = var10;
                        var6 = var14;
                    }
                }
            }
        }

        if (var4 != null) {
            var3 = new MovingObjectPosition(var4);
        }

        if (var3 != null) {
            if (var3.typeOfHit == EnumMovingObjectType.TILE
                    && e.worldObj.getBlockId(var3.blockX, var3.blockY,
                            var3.blockZ) == Block.portal.blockID) {
                return null;
            } else {
                return var3;
            }
        }

        return null;
    }

    protected void dropMinecart(EntityMinecart e) {
        try {
            e.killMinecart(DamageSource.generic); // drop cart as item, may get
                                                  // changed in the future
        } catch (Exception ex) {
            int times = 10;
            for (; times >= 0 && !e.isDead; times--) {
                try {
                    e.attackEntityFrom(DamageSource.generic, 1000);
                } catch (Exception ex2) {}
            }

            if (times == 0) {
                e.setDead(); // if nothing else works, just kill it
            }
        }
    }

    /*
    public static Object getClaimAt(org.bukkit.Location location, String playername) {
    	try {
			Object pdata = dataStore.getClass().getMethod("getPlayerData", String.class).invoke(dataStore, playername);
			Object lastClaim = pdata.getClass().getField("lastClaim").get(pdata);
			return getClaimAt.invoke(dataStore, location, false, clClaim.cast(lastClaim));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }*/

	public static boolean canInteract(EntityPlayer player, int x, int y, int z) {
		PlayerInteractEvent event = new PlayerInteractEvent(
				(org.bukkit.entity.Player) player.getBukkitEntity(),
				org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,
				new org.bukkit.inventory.ItemStack(
						player.getHeldItem().itemID,
						player.getHeldItem().stackSize,
						(short)player.getHeldItem().getItemDamage()),
				player.worldObj.getWorld().getBlockAt(x, y, z),
				org.bukkit.block.BlockFace.DOWN
			);
		MinecraftServer.getServer().server.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		/*
		//PlayerData pdata = griefPrevention.instance.dataStore.getPlayerData(player.username);
		org.bukkit.Location location = new org.bukkit.Location(player.worldObj.getWorld(), x, y, z);
		Object claim = getClaimAt(location, player.username);
		//Claim claim = griefPrevention.instance.dataStore.getClaimAt(location, false, pdata.lastClaim);
		if (claim != null) {
			try {
				return (String) allowAccess.invoke(claim, (org.bukkit.entity.Player) player.getBukkitEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		return true;
	}

	/*public static String canBuild(String player, World world, int x, int y, int z) {
		org.bukkit.Location location = new org.bukkit.Location(world.getWorld(), x, y, z);
		Object claim = getClaimAt(location, player);
		org.bukkit.entity.Player bplayer;
		EntityPlayer mplayer = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
		CraftEntity.getEntity(MinecraftServer.getServer().server, );
		if (mplayer != null)
			bplayer = (org.bukkit.entity.Player) mplayer.getBukkitEntity();
		else
			bplayer = MinecraftServer.getServer().server.getOfflinePlayer(player);
		if (claim != null) {
			try {
				return (String) allowBuild.invoke(claim, bplayer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}*/

/*
	public static boolean canExplode(Entity e, double x, double y, double z, float power) {
		org.bukkit.Location location = new org.bukkit.Location(e.worldObj.getWorld(), x, y, z);
		Explosion ex = new Explosion(e.worldObj, e, x, y, z, power);
		ex.doExplosionA();
		@SuppressWarnings("unchecked")
		EntityExplodeEvent event = new EntityExplodeEvent(e.getBukkitEntity(), location, ex.affectedBlockPositions, 0.3F);
		MinecraftServer.getServer().server.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		return true;
	}
*/

	public static boolean canBuild(EntityPlayer player, org.bukkit.block.Block block) {
		BlockBreakEvent event = new BlockBreakEvent(
				block,
				(org.bukkit.entity.Player)player.getBukkitEntity()
			);
		MinecraftServer.getServer().server.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		return true;
	}

	public static boolean canBuild(EntityPlayer player, int x, int y, int z) {
		//org.bukkit.block.Block b = player.worldObj.getWorld().getBlockAt(x, y, z);
		/*
		BlockDamageEvent event = new BlockDamageEvent(
				(org.bukkit.entity.Player)player.getBukkitEntity(),
				player.worldObj.getWorld().getBlockAt(x, y, z),
				new org.bukkit.inventory.ItemStack(
						player.getHeldItem().itemID,
						player.getHeldItem().stackSize,
						(short)player.getHeldItem().getItemDamage()),
				false // instaBreak
			); */
		BlockBreakEvent event = new BlockBreakEvent(
				player.worldObj.getWorld().getBlockAt(x, y, z),
				(org.bukkit.entity.Player)player.getBukkitEntity()
			);
		
		MinecraftServer.getServer().server.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		//else
			//System.out.println("no problem for " + player.username + " building at ["+x+","+y+","+z+"]");
		/*
		org.bukkit.Location location = new org.bukkit.Location(player.worldObj.getWorld(), x, y, z);
		Object claim = getClaimAt(location, player.username);
		if (claim != null) {
			try {
				return (String) allowBuild.invoke(claim, (org.bukkit.entity.Player) player.getBukkitEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		return true;
	}

	public static boolean canAttack(EntityPlayer attacker, Entity victim) {
		PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
				(org.bukkit.entity.Player)attacker.getBukkitEntity(),
				victim.getBukkitEntity()
			);
		MinecraftServer.getServer().server.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
/*
		org.bukkit.Location location = victim.bukkitEntity.getLocation();
		Object claim = getClaimAt(location, attacker.username);
		if (claim != null) {
			try {
				return (String) allowContainers.invoke(claim, (org.bukkit.entity.Player) attacker.getBukkitEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		return true;
	}
}
