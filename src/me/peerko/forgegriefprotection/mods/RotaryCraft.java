package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraftforge.common.FakePlayer;
import me.peerko.forgegriefprotection.DefaultFakePlayer;
import me.peerko.forgegriefprotection.ProtBase;

public class RotaryCraft extends ProtBase {

    public static RotaryCraft instance = new RotaryCraft();

    Class<?> clBorer, clTileEntityPileDriver,clMachineRegistryEnum, clBeamMachine, clTileEntityBase, clTileEntityBedrockBreaker, clTileEntitySonicShot,clEntitySonicShot;

    Field fXstep, fYstep, fZstep, fCutShape, fPlacer, fStep, 
    fSonicBorerTile, fPressure, fSonicStepX, fSonicStepY, fSonicStepZ, fFOV,
    fPileDriverStep;

    Method mGetTargetPosn, mGetSonicRange;

    HashMap<Object, Integer> map;

    @Override
    public void reload() {
	if(map == null)
	    map = new HashMap<Object, Integer>();
	else
	    map.clear();
    }

    @Override
    public void load() throws Exception {
	clBorer = Class.forName("Reika.RotaryCraft.TileEntities.Production.TileEntityBorer");
	clBeamMachine = Class.forName("Reika.RotaryCraft.Base.TileEntity.TileEntityBeamMachine");
	clTileEntityBase = Class.forName("Reika.DragonAPI.Base.TileEntityBase");
	clTileEntityBedrockBreaker = Class.forName("Reika.RotaryCraft.TileEntities.Production.TileEntityBedrockBreaker");
	clTileEntitySonicShot = Class.forName("Reika.RotaryCraft.TileEntities.TileEntitySonicBorer");
	clEntitySonicShot = Class.forName("Reika.RotaryCraft.Entities.EntitySonicShot");
	clMachineRegistryEnum = Class.forName("Reika.RotaryCraft.Registry.MachineRegistry");
	clTileEntityPileDriver = Class.forName("Reika.RotaryCraft.TileEntities.TileEntityPileDriver");

	fStep = clBorer.getDeclaredField("step");
	fCutShape = clBorer.getDeclaredField("cutShape");

	fXstep = clBeamMachine.getDeclaredField("xstep");
	fYstep = clBeamMachine.getDeclaredField("ystep");
	fZstep = clBeamMachine.getDeclaredField("zstep");

	fPlacer = clTileEntityBase.getDeclaredField("placer");

	mGetTargetPosn = clTileEntitySonicShot.getDeclaredMethod("getTargetPosn");
	fSonicStepX = clTileEntitySonicShot.getDeclaredField("xstep");
	fSonicStepY = clTileEntitySonicShot.getDeclaredField("ystep");
	fSonicStepZ = clTileEntitySonicShot.getDeclaredField("zstep");
	fPressure = clTileEntitySonicShot.getDeclaredField("pressure");
	fFOV = clTileEntitySonicShot.getDeclaredField("FOV");
	fPressure.setAccessible(true);

	mGetSonicRange = clEntitySonicShot.getDeclaredMethod("getRange");
	fSonicBorerTile = clEntitySonicShot.getDeclaredField("te");
	fSonicBorerTile.setAccessible(true);
	mGetSonicRange.setAccessible(true);

	fPileDriverStep = clTileEntityPileDriver.getDeclaredField("step");

	map = new HashMap<Object, Integer>();
    }

    @Override
    public boolean loaded() {
	return clBorer != null;
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
	return e.getClass() == clBorer || e.getClass() == clTileEntityPileDriver;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
	return e.getClass() == clEntitySonicShot;
    }

    @Override
    public boolean update(TileEntity e) throws Exception {

	Class<?> clazz = e.getClass();

	if(clazz == clBorer) {

	    if(!map.containsKey(e) || fStep.getInt(e) != map.get(e)) {


		map.put(e, fStep.getInt(e));
		int x,y,z,a = 0,b, step = fStep.getInt(e);


		if(e.getBlockMetadata() > 1)
		    a = 1;

		b = 1-a;


		out:
		    for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 5; j++) {
			    x = e.xCoord + step*fXstep.getInt(e) + a*(i-3);
			    y = e.yCoord + step*fYstep.getInt(e) + (4-j);
			    z = e.zCoord + step*fZstep.getInt(e) + b*(i-3);

			    //The rotarycraft borer has a shape to cut into the rock so make sure we only log that shape
			    if((fStep.getInt(e) == 1 || Array.getBoolean(Array.get(fCutShape.get(e),i),j)) && !canBuild(DefaultFakePlayer.getPlayer(e.worldObj), x, y, z)) {

				//Set boring machine to have no shape to cut and reset location
				for(int g = 0; g < 7; g++) {
				    for(int c = 0; c < 5; c++) {
					Array.setBoolean(Array.get(fCutShape.get(e),g),c,false);
				    }
				}

				fStep.setInt(e, 2);
				break out;
			    }
			} 
		    }
	    }
	}
	else if(clazz == clTileEntityPileDriver) {

	    final int step = fPileDriverStep.getInt(e);
	    if(!map.containsKey(e) || step != map.get(e)) {

		map.put(e, step);
		
		//Smash does a radius of 2 but misses corners
		//Why he doesnt use r = sqrt(a^2 + b^2) idk... probably to save cpu time
		for(int i = -2; i < 3; i++) {
		    for(int g = -2; g < 3; g++) {
			if(Math.abs(i*g) != 4) {

			    if(!canBuild(DefaultFakePlayer.getPlayer(e.worldObj), e.xCoord+i, e.yCoord-step-1, e.zCoord+g)) {
				e.worldObj.destroyBlock(e.xCoord, e.yCoord, e.zCoord,true);
				map.remove(e);
				return true;
			    }
			}
		    }
		}
	    }
	    
	}
	return true;
    }

    @Override
    public boolean update(Entity e) throws Exception {

	if (e.isDead) {
	    return true;
	}

	if(e.getClass() == clEntitySonicShot) {

	    int x = (int)Math.floor(e.posX);
	    int y = (int)Math.floor(e.posY);
	    int z = (int)Math.floor(e.posZ);

	    TileEntity te = (TileEntity)fSonicBorerTile.get(e);
	    int range = (Integer) mGetSonicRange.invoke(e);
	    int dist = (int)(fSonicStepX.getInt(te)*(x-te.xCoord)+fSonicStepY.getInt(te)*(y-te.yCoord)+fSonicStepZ.getInt(te)*(z-te.zCoord));

	    if(dist >= range) {
		int[] target = (int[]) mGetTargetPosn.invoke(te);
		Vec3 vec = Vec3.fakePool.getVecFromPool(0, 0, 0);

		MovingObjectPosition mov = new MovingObjectPosition(target[0], target[1], target[2], -1, vec);

		int xstep = fSonicStepX.getInt(te);
		int ystep = fSonicStepY.getInt(te);
		int zstep = fSonicStepZ.getInt(te);
		int k = fFOV.getInt(te);

		x = mov.blockX;
		y = mov.blockY;
		z = mov.blockZ;



		if (xstep != 0) {
		    for (int i = z-k; i <= z+k; i++) {
			for (int j = y-k; j <= y+k; j++) {
			    Logger.getLogger("Minecraft").log(Level.INFO, "Pos: " + x + " " + j + " " + i);
			    if(!canBuild(DefaultFakePlayer.getPlayer(e.worldObj), x, j, i)){
				e.worldObj.destroyBlock(te.xCoord, te.yCoord, te.zCoord, true);
				return false;
			    }
			}
		    }
		}
		else if (zstep != 0) {
		    for (int i = x-k; i <= x+k; i++) {
			for (int j = y-k; j <= y+k; j++) {
			    Logger.getLogger("Minecraft").log(Level.INFO, "Pos: " + i + " " + j + " " + z);
			    if(!canBuild(DefaultFakePlayer.getPlayer(e.worldObj), i, j, z)) {
				e.worldObj.destroyBlock(te.xCoord, te.yCoord, te.zCoord, true);
				return false;
			    }
			}
		    }
		}
		else if (ystep != 0) {
		    for (int i = x-k; i <= x+k; i++) {
			for (int j = z-k; j <= z+k; j++) {
			    Logger.getLogger("Minecraft").log(Level.INFO, "Pos: " + i + " " + y + " " + j);
			    if(!canBuild(DefaultFakePlayer.getPlayer(e.worldObj), i, y, j)) {
				e.worldObj.destroyBlock(te.xCoord, te.yCoord, te.zCoord, true);
				return false;
			    }
			}
		    }
		}
	    }
	}

	return true;
    }

    @Override
    public String getMod() {
	// TODO Auto-generated method stub
	return "RotaryCraft";
    }

    @Override
    public String getComment() {
	// TODO Auto-generated method stub
	return "make things work";
    }

}
