/*
 * Adapted from http://www.minecraftforum.net/topic/1889473-legobear154s-mods/
 */
package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import me.peerko.forgegriefprotection.ProtBase;
import me.peerko.forgegriefprotection.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ThaumCraft extends ProtBase {
    public static ThaumCraft instance = new ThaumCraft();
    public int explosionRadius = 6;

    private Class<?> clAlumentum = null, clTileArcaneBore, clEntityFrostShard,
            clItemWandFire, clItemWandExcavation, clItemWandLightning,
            clItemWandTrade;
    Field fBore_toDig, fBore_digX, fBore_digZ, fBore_digY;
    Field fFrostShard_shootingEntity;

    // boolean toDig = false; int digX = 0; int digZ = 0; int digY = 0;

    @Override
    public void load() throws Exception {
        clEntityFrostShard = Class
                .forName("thaumcraft.common.entities.projectile.EntityFrostShard");
        clAlumentum = Class
                .forName("thaumcraft.common.entities.projectile.EntityAlumentum");

        clItemWandExcavation = Class
                .forName("thaumcraft.common.items.wands.ItemWandExcavation");
        clItemWandFire = Class
                .forName("thaumcraft.common.items.wands.ItemWandFire");
        clItemWandLightning = Class
                .forName("thaumcraft.common.items.wands.ItemWandLightning");
        clItemWandTrade = Class
                .forName("thaumcraft.common.items.wands.ItemWandTrade");

        clTileArcaneBore = Class
                .forName("thaumcraft.common.tiles.TileArcaneBore");
        fBore_toDig = clTileArcaneBore.getDeclaredField("toDig");
        fBore_digX = clTileArcaneBore.getDeclaredField("digX");
        fBore_digY = clTileArcaneBore.getDeclaredField("digY");
        fBore_digZ = clTileArcaneBore.getDeclaredField("digZ");

        fFrostShard_shootingEntity = clEntityFrostShard
                .getDeclaredField("shootingEntity");
    }

    @Override
    public boolean loaded() {
        return clAlumentum != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clAlumentum.isInstance(e) || clEntityFrostShard.isInstance(e);
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        return e.getClass() == clTileArcaneBore;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return (clItemWandExcavation.isInstance(e) && e.getClass().equals(clItemWandExcavation))
                || (clItemWandFire.isInstance(e) && e.getClass().equals(clItemWandFire))
                || (clItemWandLightning.isInstance(e) && e.getClass().equals(clItemWandLightning))
                || (clItemWandTrade.isInstance(e) && e.getClass().equals(clItemWandTrade));
    }

    @Override
    public boolean update(Entity e) throws Exception {
    	//Logger.getLogger("DEBUG").log(Level.ALL, "ThaumCraft::update(Entity)");
        if (clAlumentum.isInstance(e)) {
            EntityThrowable t = (EntityThrowable) e;
            EntityLiving owner = t.getThrower();

            if (owner == null || !(owner instanceof EntityPlayer)) {
                return false; //"No owner or is not a player";
            }

            EntityPlayer thrower = (EntityPlayer) owner;

            int x = (int) (t.posX + t.motionX);
            int y = (int) (t.posY + t.motionY);
            int z = (int) (t.posZ + t.motionZ);
            //int dim = thrower.dimension;

            if (!(canBuild(thrower, x - explosionRadius, y, z - explosionRadius) &&
            	canBuild(thrower, x - explosionRadius, y, z + explosionRadius) &&
            	canBuild(thrower, x + explosionRadius, y, z - explosionRadius) &&
            	canBuild(thrower, x + explosionRadius, y, z + explosionRadius)))
            return false;
            /*
            if (!thrower.canInteract(dim, x - explosionRadius, y, z
                    - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x - explosionRadius, y, z
                            + explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            + explosionRadius, Permissions.Build)) {
                return "Explosion would hit a protected town";
            }*/
        } else if (clEntityFrostShard.isInstance(e)) {
            Entity shooter = (Entity) fFrostShard_shootingEntity.get(e);

            if (shooter == null || !(shooter instanceof EntityPlayer)) {
                return false; //"No owner";
            }

            EntityPlayer thrower = (EntityPlayer) shooter;

            int x = (int) (e.posX + e.motionX);
            int y = (int) (e.posY + e.motionY);
            int z = (int) (e.posZ + e.motionZ);
            int radius = 1;
            //int dim = thrower.dimension;

            if (!(canBuild(thrower, x - radius, y - radius, z - radius) &&
            	canBuild(thrower, x - radius, y - radius, z + radius) &&
            	canBuild(thrower, x + radius, y - radius, z - radius) &&
            	canBuild(thrower, x + radius, y - radius, z + radius)))
            	return false;
            /*
            if (!thrower.canInteract(dim, x - radius, y - radius, y + radius, z
                    - radius, Permissions.Build)
                    || !thrower.canInteract(dim, x - radius, y - radius, y
                            + radius, z + radius, Permissions.Build)
                    || !thrower.canInteract(dim, x + radius, y - radius, y
                            + radius, z - radius, Permissions.Build)
                    || !thrower.canInteract(dim, x + radius, y - radius, y
                            + radius, z + radius, Permissions.Build)) {
                return "Cannot build here";
            }*/
        }

        return true;
    }

    @Override
    public boolean update(EntityPlayer res, Item tool, ItemStack item)
            throws Exception {
    	//Logger.getLogger("DEBUG").log(Level.ALL, "ThaumCraft::update(player, tool, item)");
        if (clItemWandFire.isInstance(tool) && tool.getClass().equals(clItemWandFire)) {
            List<Entity> list = getTargets(res.worldObj,
                    res.getLook(17), res, 17);
            for (Entity e : list) {
            	if (!canAttack(res, e)) {
                    return false;
                }
            }
        } else if (clItemWandLightning.isInstance(tool) && tool.getClass().equals(clItemWandLightning)) {
            List<Entity> list = getTargets(res.worldObj,
                    res.getLook(20), res, 20);
            for (Entity e : list) {
            	if (!canAttack(res, e)) {
                    return false;
                }
            }
        } else if (clItemWandExcavation.isInstance(tool) && tool.getClass().equals(clItemWandExcavation)) {
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(
                    res.worldObj, res, false, 10.0D);

            if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
            	if (!canBuild(res, pos.blockX, pos.blockY, pos.blockZ)) {
            		return false;
            	}
            }
        } else if (clItemWandTrade.isInstance(tool) && tool.getClass().equals(clItemWandTrade)) {
            if (!res.isSneaking()) {
                MovingObjectPosition pos = Utils
                        .getMovingObjectPositionFromPlayer(
                                res.worldObj, res,
                                false, 10.0D);

                if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                    int x = pos.blockX;
                    int y = pos.blockY;
                    int z = pos.blockZ;
                    int radius = 3;
                    //int dim = res.dimension;

                    if (!(canBuild(res, x - radius, y - radius, z - radius) &&
                    	canBuild(res, x - radius, y - radius, z + radius) &&
                    	canBuild(res, x + radius, y - radius, z - radius) &&
                    	canBuild(res, x + radius, y - radius, z + radius)))
                    	return false;
                    /*
                    if (!res.canInteract(dim, x - radius, y - radius, y
                            + radius, z - radius, Permissions.Build)
                            || !res.canInteract(dim, x - radius, y - radius, y
                                    + radius, z + radius, Permissions.Build)
                            || !res.canInteract(dim, x + radius, y - radius, y
                                    + radius, z - radius, Permissions.Build)
                            || !res.canInteract(dim, x + radius, y - radius, y
                                    + radius, z + radius, Permissions.Build)) {
                        return "Cannot build here";
                    }*/
                }
            }
        }

        return true;
    }

    @Override
	public boolean update(TileEntity e) throws Exception {
	    if (clTileArcaneBore.isInstance(e)) {
	        fBore_toDig.setAccessible(true);
	        fBore_digX.setAccessible(true);
	        fBore_digY.setAccessible(true);
	        fBore_digZ.setAccessible(true);
	
	        if (fBore_toDig.getBoolean(e)) {
	            /*EntityPlayer actor = getActorFromLocation(
	                    e.worldObj.provider.dimensionId, e.xCoord, e.yCoord,
	                    e.zCoord, "#thaumcraft-bore#");*/
	            if (!canBuild(null, fBore_digX.getInt(e), fBore_digY.getInt(e), fBore_digZ.getInt(e))) {
	                fBore_toDig.set(e, false);
	            }
	        }
	    }
	    return true;
	}

	private List<Entity> getTargets(World world, Vec3 tvec, EntityPlayer p,
            double range) {
        Entity pointedEntity = null;
        Vec3 vec3d = world.getWorldVec3Pool().getVecFromPool(p.posX, p.posY,
                p.posZ);
        Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range,
                tvec.zCoord * range);
        float f1 = 1.0F;
        List<?> list = world.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox
                .addCoord(tvec.xCoord * range, tvec.yCoord * range,
                        tvec.zCoord * range).expand(f1, f1, f1));

        ArrayList<Entity> l = new ArrayList<Entity>();
        for (int i = 0; i < list.size(); i++) {
            Entity entity = (Entity) list.get(i);
            if (entity.canBeCollidedWith()) {
                float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2,
                        f2 * 1.25F, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb
                        .calculateIntercept(vec3d, vec3d2);

                if (movingobjectposition != null) {
                    pointedEntity = entity;

                    if (pointedEntity != null
                            && p.canEntityBeSeen(pointedEntity)) {
                        l.add(pointedEntity);
                    }
                }
            }
        }

        return l;
    }

    @Override
    public String getMod() {
        return "ThaumCraft";
    }

    @Override
    public String getComment() {
        return "Build check: EntityAlumentum & ItemWandExcavation";
    }
}
