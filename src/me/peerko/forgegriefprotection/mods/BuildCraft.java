/*
 * Adapted from http://www.minecraftforum.net/topic/1889473-legobear154s-mods/
 */
package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.peerko.forgegriefprotection.ProtBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class BuildCraft extends ProtBase {
    public static BuildCraft instance = new BuildCraft();
    public List<TileEntity> checkedEntitys = new ArrayList<TileEntity>();

    Class<?> clQuarry = null, clFiller, clBuilder, clBox;
    Field fBoxQ, fBoxF, fBoxB, fmx, fmy, fmz, fxx, fxy, fxz, fBoxInit,
            fQuarryOwner, fQuarryBuilderDone;

    @Override
    public void reload() {
        checkedEntitys.clear();
    }

    @Override
    public void load() throws Exception {
        clQuarry = Class.forName("buildcraft.factory.TileQuarry");
        clFiller = Class.forName("buildcraft.builders.TileFiller");
        clBuilder = Class.forName("buildcraft.builders.TileBuilder");

        clBox = Class.forName("buildcraft.core.Box");

        fBoxQ = clQuarry.getField("box");
        fQuarryOwner = clQuarry.getField("placedBy");
        fQuarryBuilderDone = clQuarry.getField("builderDone");
        fBoxF = clFiller.getField("box");
        fBoxB = clBuilder.getField("box");

        fmx = clBox.getField("xMin");
        fmy = clBox.getField("yMin");
        fmz = clBox.getField("zMin");
        fxx = clBox.getField("xMax");
        fxy = clBox.getField("yMax");
        fxz = clBox.getField("zMax");
        fBoxInit = clBox.getField("initialized");
    }

    @Override
    public boolean loaded() {
        return clBuilder != null;
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        Class<?> c = e.getClass();

        return c == clQuarry || c == clFiller || c == clBuilder;
    }

    @Override
    public boolean update(TileEntity e) throws Exception {
    	if (!isEntityInstance(e))
    		return true;
        if (checkedEntitys.contains(e)) {
            return true;
        }

        int s = updateSub(e);

        // Log.info(String.format("Checked BC '%s' resulted in '%s'", e, s));

        if (s == 0) {
            checkedEntitys.add(e);
            return true;
        }

        return s == 1 ? true : false; // "-" used to bypass caching
    }

    private int updateSub(TileEntity e) throws Exception {
        Object box = null;
        Class<?> clazz = e.getClass();

        if (clazz == clQuarry) {
            box = fBoxQ.get(e);
        } else if (clazz == clFiller) {
            box = fBoxF.get(e);
        } else if (clazz == clBuilder) {
            box = fBoxB.get(e);
        }

        if(fBoxInit == null)
            return 1;
        boolean init = fBoxInit.getBoolean(box);
        if (!init) {
            return 1;
        }

        if (clazz == clQuarry && fQuarryBuilderDone.getBoolean(e)) {
            return 0;
        }

        int ax = fmx.getInt(box);
        fmy.getInt(box);
        int az = fmz.getInt(box);

        int bx = fxx.getInt(box);
        int by = fxy.getInt(box);
        int bz = fxz.getInt(box);

        EntityPlayer owner = null;
        if (clazz == clQuarry) {
            owner = (EntityPlayer) fQuarryOwner.get(e);
            if (owner == null) {
                return 0; // owner = null then the block was there before
            }
        }

        // check from xMin,zMin to xMax,zMax
        for (int z = az; z <= bz; z++) {
            for (int x = ax; x <= bx; x++) {
                // just use yMax for y
                if (!canBuild(owner, x, by, z)) {
                	return 2;
                }

            }
        }

        return 0;
    }

    @Override
    public String getMod() {
        return "BuildCraft";
    }

    @Override
    public String getComment() {
        return "Town permission: allowBuildcraftMiners, Build perm for Quarry";
    }
}
