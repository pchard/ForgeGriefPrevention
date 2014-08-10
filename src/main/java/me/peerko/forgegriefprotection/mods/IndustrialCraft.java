package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.peerko.forgegriefprotection.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;


public class IndustrialCraft extends ProtBase {
    public static IndustrialCraft instance = new IndustrialCraft();

    // tnts
    Class<?> clDynamite = null, clStickyDynamite, clNuke, clITNT,
	    clEntityIC2Explosive;
    Field fFuse1, fFuse2;

    // laser
    public int explosionRadius = 6;
    Class<?> clLaser = null;
    Field fTickInAir, fOwner,fDynOwner,fTntOwner, fExplosive, fRange, fPower, fBlockBreaks, fExplosivePower;

    @Override
    public void load() throws Exception {
	clDynamite = Class.forName("ic2.core.block.EntityDynamite");
	clStickyDynamite = Class.forName("ic2.core.block.EntityStickyDynamite");
	clNuke = Class.forName("ic2.core.block.EntityNuke");
	clITNT = Class.forName("ic2.core.block.EntityItnt");
	clEntityIC2Explosive = Class
		.forName("ic2.core.block.EntityIC2Explosive");

	fFuse1 = clEntityIC2Explosive.getDeclaredField("fuse");
	fTntOwner = clEntityIC2Explosive.getDeclaredField("igniter");
	fFuse2 = clDynamite.getDeclaredField("fuse");
	fDynOwner = clDynamite.getDeclaredField("owner");

	clLaser = Class.forName("ic2.core.item.tool.EntityMiningLaser");
	fOwner = clLaser.getDeclaredField("owner");
	fTickInAir = clLaser.getDeclaredField("ticksInAir");
	fExplosive = clLaser.getDeclaredField("explosive");
	fRange = clLaser.getDeclaredField("range");
	fPower = clLaser.getDeclaredField("power");
	fBlockBreaks = clLaser.getDeclaredField("blockBreaks");

	fExplosivePower = clEntityIC2Explosive.getDeclaredField("explosivePower");
    }

    @Override
    public boolean loaded() {
	return clDynamite != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
	Class<?> c = e.getClass();
	return c == clLaser || c == clDynamite || c == clStickyDynamite
		|| clEntityIC2Explosive.isInstance(e);
    }

    @Override
    public boolean update(Entity e) throws Exception {
	if (e.isDead) {
	    return true;
	}

	Class<?> c = e.getClass();
	if (c == clLaser) {
	    /*
	     * if ((int)e.posX == (int)e.prevPosX && (int)e.posY ==
	     * (int)e.prevPosY && (int)e.posZ == (int)e.prevPosZ) // didn't move
	     * return null;
	     */
	    fTickInAir.setAccessible(true);
	    EntityPlayer owner = (EntityPlayer) fOwner.get(e); // actually
	    // living
	    Integer ticksInAir = (Integer) fTickInAir.get(e);
	    Boolean explosive = (Boolean) fExplosive.get(e);
	    Float range = (Float) fRange.get(e);
	    Float power = (Float) fPower.get(e);
	    Integer blockBreaks = (Integer) fBlockBreaks.get(e);

	    if (owner == null) {
		return false;
	    }

	    //Resident res = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getOrMakeResident(owner);

	    if (range < 1.0F || power <= 0.0F || blockBreaks <= 0) {
		if (explosive) {
		    int x = (int) e.posX, y = (int) e.posY, z = (int) e.posZ;

		    /*
                    if (!res.canInteract(x - explosionRadius, y
                            - explosionRadius, y + explosionRadius, z
                            - explosionRadius, Permissions.Build)
                            || !res.canInteract(x - explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    + explosionRadius, Permissions.Build)
                            || !res.canInteract(x + explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    - explosionRadius, Permissions.Build)
                            || !res.canInteract(x + explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    + explosionRadius, Permissions.Build)) */
		    if (!canBuild(owner, x - explosionRadius, y - explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y + explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y - explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y + explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y - explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y + explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y - explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y + explosionRadius, z + explosionRadius)
			    )
		    {
			return false; // "Explosion would hit a protected town";
		    }
		}
		return true;
	    }

	    ticksInAir++;

	    Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
	    Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY
		    + e.motionY, e.posZ + e.motionZ);
	    MovingObjectPosition var3 = e.worldObj.rayTraceBlocks_do_do(var1,
		    var2, false, true);
	    var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

	    if (var3 != null) {
		var2 = Vec3.createVectorHelper(var3.hitVec.xCoord,
			var3.hitVec.yCoord, var3.hitVec.zCoord);
	    } else {
		var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY
			+ e.motionY, e.posZ + e.motionZ);
	    }

	    Entity var4 = null;
	    List<?> var5 = e.worldObj.getEntitiesWithinAABBExcludingEntity(e,
		    e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ)
		    .expand(1.0D, 1.0D, 1.0D));
	    double var6 = 0.0D;
	    int var8;

	    for (var8 = 0; var8 < var5.size(); ++var8) {
		Entity var9 = (Entity) var5.get(var8);

		if (var9.canBeCollidedWith()
			&& (var9 != owner || ticksInAir >= 5)) {
		    float var10 = 0.3F;
		    AxisAlignedBB var11 = var9.boundingBox.expand(var10, var10,
			    var10);
		    MovingObjectPosition var12 = var11.calculateIntercept(var1,
			    var2);

		    if (var12 != null) {
			double var13 = var1.distanceTo(var12.hitVec);

			if (var13 < var6 || var6 == 0.0D) {
			    var4 = var9;
			    var6 = var13;
			}
		    }
		}
	    }

	    if (var4 != null) {
		var3 = new MovingObjectPosition(var4);
	    }

	    if (var3 != null) {
		if ((var3.typeOfHit == EnumMovingObjectType.ENTITY
			&& !canAttack(owner, var3.entityHit))
			// && !res.canAttack(var3.entityHit)
			|| (var3.typeOfHit == EnumMovingObjectType.TILE
			// && !res.canInteract(var3.blockX, var3.blockY, var3.blockZ, Permissions.Build)
			&& !canBuild(owner, var3.blockX, var3.blockY, var3.blockZ))
			) {
		    return false; // "Target in MyTown protected area";
		}

		if (explosive) {
		    // 4 corner check
		    int x, y, z;

		    if (var3.entityHit != null) {
			x = (int) var3.entityHit.posX;
			y = (int) var3.entityHit.posY;
			z = (int) var3.entityHit.posZ;
		    } else {
			x = var3.blockX;
			y = var3.blockY;
			z = var3.blockZ;
		    }

		    /*
                    if (!res.canInteract(x - explosionRadius, y
                            - explosionRadius, y + explosionRadius, z
                            - explosionRadius, Permissions.Build)
                            || !res.canInteract(x - explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    + explosionRadius, Permissions.Build)
                            || !res.canInteract(x + explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    - explosionRadius, Permissions.Build)
                            || !res.canInteract(x + explosionRadius, y
                                    - explosionRadius, y + explosionRadius, z
                                    + explosionRadius, Permissions.Build)) {
                        return "Explosion would hit a protected town";
                    }*/
		    if (!canBuild(owner, x - explosionRadius, y - explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y + explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y - explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x - explosionRadius, y + explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y - explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y + explosionRadius, z - explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y - explosionRadius, z + explosionRadius)
			    || !canBuild(owner, x + explosionRadius, y + explosionRadius, z + explosionRadius)
			    )
			return false;
		}
	    }

	    return true;
	} else {

	    int radius = 6;
	    int fuse = 0;
	    float power = 0;

	    if (c == clDynamite || c == clStickyDynamite) {
		fuse = fFuse2.getInt(e);
		radius = 1;
		power = 1.0F;
	    } else {
		fuse = fFuse1.getInt(e);
		if (clNuke.isInstance(e)) {
		    radius = 35;
		    power = fExplosivePower.getFloat(e);
		} else {
		    radius = 6;
		}
	    }

	    if (fuse > 1) {
		return true;
	    }

	    //DEBUG:
	    //Logger.getLogger("Minecraft").log(Level.INFO, "class: " + fTntOwner.get(e));


	    radius = radius + 2; // 2 for safety

	    /*int x1 = (int) e.posX - radius >> 4;
            int z1 = (int) e.posZ - radius >> 4;
            int x2 = (int) e.posX + radius >> 4;
            int z2 = (int) e.posZ + radius >> 4;

            boolean canBlow = canExplode(e, (int) e.posX, (int) e.posY, (int) e.posZ, power);

            for (int x = x1; x <= x2 && canBlow; x++) {
                for (int z = z1; z <= z2 && canBlow; z++) {
                    if (!canBlow(e.dimension, x << 4, (int) e.posY - radius,
                            (int) e.posY + radius, z << 4)) {
                        canBlow = false;
                    }
                }
            }*/

	    boolean canBlow = true;
	    EntityPlayer ply;
	    if(c == clDynamite || c == clStickyDynamite)
		ply = (EntityPlayer) fDynOwner.get(e);
	    else if(fTntOwner.get(e) instanceof EntityPlayer) {
		ply = (EntityPlayer)fTntOwner.get(e);
	    }
	    else 
		return true;

	    out:
		for(int x = -radius; x < radius; x++){
		    for(int y = -radius; y < radius; y++) {
			for(int z = -radius; z < radius; z++) {
			    if(!canBuild(ply, x + (int)e.posX, y + (int)e.posY, z + (int)e.posZ)) {
				canBlow = false;
				break out;
			    }
			}
		    }
		}


	    return canBlow; // ? null : "TNT explosion disabled here";


	    // Since all IC2 explosions are built on the Explosion class, GriefPrevention will be able to handle those.
	    //return true;
	}
    }

    /*
    private boolean canBlow(int dim, int x, int yFrom, int yTo, int z) {
        TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x,
                yFrom, yTo, z);

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).disableTNT;
        }

        return !b.settings.disableTNT;
    }*/

    @Override
    public String getMod() {
	return "IndustrialCraft2";
    }

    @Override
    public String getComment() {
	return "Town permission: disableTNT, Build & PVP check: EntityMiningLaser";
    }
}
