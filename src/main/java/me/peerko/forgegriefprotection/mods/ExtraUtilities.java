package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import me.peerko.forgegriefprotection.DefaultFakePlayer;
import me.peerko.forgegriefprotection.ProtBase;

public class ExtraUtilities extends ProtBase {

    public static ExtraUtilities instance = new ExtraUtilities();

    Class<?> clEnderQuarry = null;

    Field fXBlock, fYBlock, fZBlock, fXChunk, fZChunk, fFinished, fOwner, fProgress;
    HashMap<Object,Integer> map;

    @Override
    public void load() throws Exception {
	clEnderQuarry = Class.forName("extrautils.tileentity.enderquarry.TileEntityEnderQuarry");

	fXBlock = clEnderQuarry.getDeclaredField("dx");
	fYBlock = clEnderQuarry.getDeclaredField("dy");
	fZBlock = clEnderQuarry.getDeclaredField("dz");
	fXChunk = clEnderQuarry.getDeclaredField("chunk_x");
	fZChunk = clEnderQuarry.getDeclaredField("chunk_z");
	fFinished = clEnderQuarry.getDeclaredField("finished");
	fOwner = clEnderQuarry.getDeclaredField("fakePlayer");
	fProgress = clEnderQuarry.getDeclaredField("progress");
	fOwner.setAccessible(true);
	fXChunk.setAccessible(true);
	fZChunk.setAccessible(true);

	map = new HashMap<Object,Integer>();
    }

    @Override
    public void reload() {
	if(map != null)
	    map.clear();
	else
	    map = new HashMap<Object,Integer>();
    }

    @Override
    public boolean loaded() {
	return clEnderQuarry != null;
    }

    @Override
    public boolean update(TileEntity e) throws Exception {

	if (!isEntityInstance(e))
	    return true;


	int prevProgress;
	int progress = fProgress.getInt(e);

	//Check to see if anything has changed
	if(map.containsKey(e) && (prevProgress = map.get(e)) == progress)
	    return true;

	//If it has update prevProgress stats
	map.put(e, new Integer(progress));

	//EnderQuarry mines the block at (chunk_x << 4 + x) by (y) by (chunk_z << 4 + z)

	EntityPlayer player = (EntityPlayer)fOwner.get(e);

	if(player == null)
	    player = DefaultFakePlayer.getPlayer(e.worldObj);

	int x = (fXChunk.getInt(e) << 4) + fXBlock.getInt(e);
	int y = fYBlock.getInt(e);
	int z = (fZChunk.getInt(e) << 4) + fZBlock.getInt(e);

	try {
	    if(canBuild(player, x,y,z))
		return true;
	} catch (Exception ex) {
	    //Silence you fool!
	}

	fFinished.setBoolean(e, true);
	Logger.getLogger("Minecraft").log(Level.INFO, "DERP: " + x +" " + y +" " + z);


	return true;
    }

    public boolean isEntityInstance(TileEntity e) {
	Class<?> c = e.getClass();
	return c == clEnderQuarry;
    }

    @Override
    public String getMod() {
	// TODO Auto-generated method stub
	return "ExtraUtilities";
    }

    @Override
    public String getComment() {
	// TODO Auto-generated method stub
	return "Protection for Enderquarry";
    }

}
