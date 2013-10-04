package me.peerko.forgegriefprotection.mods;

import java.util.ArrayList;
import java.util.List;
import me.peerko.forgegriefprotection.ProtBase;
import me.peerko.forgegriefprotection.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ThaumicTinkerer extends ProtBase {
    public static ThaumicTinkerer instance = new ThaumicTinkerer();
    public int explosionRadius = 6;

    private Class<?> clItemWandDislocation = null, clItemGas, clItemGasRemover;

    @Override
    public void load() throws Exception {
        clItemWandDislocation = Class
                .forName("vazkii.tinkerer.item.ItemWandDislocation");
        clItemGas = Class
        		.forName("vazkii.tinkerer.item.ItemGas");
        clItemGasRemover = Class
        		.forName("vazkii.tinkerer.item.ItemGasRemover");
    }

    @Override
    public boolean loaded() {
        return clItemWandDislocation != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return false;
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        return false;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return (clItemWandDislocation.isInstance(e) && e.getClass().equals(clItemWandDislocation))
                || (clItemGas.isInstance(e) && e.getClass().equals(clItemGas))
                || (clItemGasRemover.isInstance(e) && e.getClass().equals(clItemGasRemover));
    }

    @Override
    public boolean update(Entity e) throws Exception {
        return true;
    }

    @Override
    public boolean update(EntityPlayer res, Item tool, ItemStack item) throws Exception {
        if (clItemGas.isInstance(tool) && tool.getClass().equals(clItemGas)) {
        	int x = (int) res.posX, y = (int) (res.posY + 1), z = (int) res.posZ;
        	if (res.worldObj.isAirBlock(x, y, z)) {
        		// check 3D diamond pattern
        		for (int xt = -4; xt < 4; xt++)
        			for (int yt = -4; yt < 4; yt++)
						for (int zt = -4; zt < 4; zt++)
							if ((xt + yt + zt) < 4 && !canBuild(res, x+xt, y+yt, z+zt))
								return false;
        	}
        } else if (clItemGasRemover.isInstance(tool) && tool.getClass().equals(clItemGasRemover)) {
        	int xs = (int) res.posX, ys = (int) (res.posY), zs = (int) res.posZ;
        	if (!res.worldObj.isAirBlock(xs, ys, zs)) {
        		// check 3x4x3 box around
				for (int x = xs - 2; x < xs + 2; x++)
					for (int y = ys - 3; y < ys + 3; y++)
						for (int z = zs - 2; z < zs + 2; z++)
							if (!canBuild(res, x, y, z))
								return false;
			}
        } else if (clItemWandDislocation.isInstance(tool) && tool.getClass().equals(clItemWandDislocation)) {
                MovingObjectPosition pos = Utils
                        .getMovingObjectPositionFromPlayer(
                                res.worldObj, res,
                                false, 10.0D);

                if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                    int x = pos.blockX;
                    int y = pos.blockY;
                    int z = pos.blockZ;

                    if (!canBuild(res, x, y, z))
                    	return false;
                }
        }

        return true;
    }

    @Override
	public boolean update(TileEntity e) throws Exception {
	    return true;
	}

	@SuppressWarnings("unused")
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
        return "Thaumic Tinkerer";
    }

    @Override
    public String getComment() {
        return "Build check: EntityAlumentum & ItemWandDislocation";
    }
}
