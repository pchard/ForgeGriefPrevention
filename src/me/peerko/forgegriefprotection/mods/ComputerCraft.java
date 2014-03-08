package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.Util;

import me.peerko.forgegriefprotection.ProtBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeDirection;

public class ComputerCraft extends ProtBase {
    public static ComputerCraft instance = new ComputerCraft();

    Class<?> clTurtle = null, clTurtlePlayer, clState, clTurtleCommand;
    Method mTerminate, mIsOn, mGetPlayer,mGetDir;
    Field fMoved, fClientState, fState, fCommandQueue, fCommandType, fDig, fDigUp, fDigDown;

    public HashMap<Object, Object> turtles = new HashMap<Object, Object>();
    public HashMap<Object, Integer> states = new HashMap<Object, Integer>();
    public HashMap<ChunkCoordinates, Long> anti_spam = new HashMap<ChunkCoordinates, Long>();
    public int anti_spam_counter = 0;

    @Override
    public void reload() {
	anti_spam_counter = 0;
	turtles = new HashMap<Object, Object>();
	anti_spam = new HashMap<ChunkCoordinates, Long>();
	states = new HashMap<Object, Integer>();
    }

    @Override
    public void load() throws Exception {
	clTurtle = Class.forName("dan200.turtle.shared.TileEntityTurtle");
	mTerminate = clTurtle.getDeclaredMethod("terminate");
	mIsOn = clTurtle.getDeclaredMethod("isOn");
	fMoved = clTurtle.getDeclaredField("m_moved");
	fState = clTurtle.getDeclaredField("m_state");
	fState.setAccessible(true);
	fClientState = clTurtle.getDeclaredField("m_clientState");
	fClientState.setAccessible(true);
	clTurtlePlayer = Class.forName("dan200.turtle.shared.TurtlePlayer");
	mGetPlayer = clTurtlePlayer.getDeclaredMethod("getPlayer", World.class);
	mGetDir = clTurtle.getDeclaredMethod("getFacingDir");

	clState = Class.forName("dan200.turtle.shared.TileEntityTurtle$State");
	fCommandQueue = clState.getDeclaredField("commandQueue");
	fCommandQueue.setAccessible(true);

	clTurtleCommand = Class.forName("dan200.turtle.shared.TurtleCommand");
	fCommandType = clTurtleCommand.getDeclaredField("type");
	fDig = clTurtleCommand.getDeclaredField("Dig");
	fDigUp = clTurtleCommand.getDeclaredField("DigUp");
	fDigDown = clTurtleCommand.getDeclaredField("DigDown");
	fCommandType.setAccessible(true);
    }

    @Override
    public boolean loaded() {
	return clTurtle != null;
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
	return clTurtle.isInstance(e);
    }

    @Override
    public boolean update(TileEntity e) throws Exception {
	//cleanAntiSpam();

	Object state = fState.get(e);
	Object clientState = fClientState.get(e);
	Object prev_turtle = turtles.get(clientState);


	if (!(Boolean) mIsOn.invoke(e)) {
	    return true;
	}

	Object commandList = fCommandQueue.get(state);
	Object command = ((LinkedList<?>)commandList).peek();
	
	boolean allowed = true;
	
	int dim = e.worldObj.provider.dimensionId;
	int dir = (Integer) mGetDir.invoke(e);
	
	//Check if the command the turtle is about to execute has changed... if so
	// react accordingly
	if(command != null) {

	    int commandType = (Integer)fCommandType.get(command);
	    Integer amt = states.get(e);
	    if(amt == null || amt != commandType) {
		states.put(e, commandType);
		
		if(commandType == fDig.getInt(command)) {
		    allowed = canRoam(dim, e.xCoord + Facing.offsetsXForSide[dir], e.yCoord + Facing.offsetsYForSide[dir], e.zCoord + Facing.offsetsZForSide[dir]);
		}
		else if(commandType == fDigUp.getInt(command)) {
		    allowed = canRoam(dim, e.xCoord, e.yCoord + 1, e.zCoord);
		}
		else if(commandType == fDigDown.getInt(command)) {
		    allowed = canRoam(dim, e.xCoord, e.yCoord - 1, e.zCoord);
		}
		//Logger.getLogger("Minecraft").log(Level.INFO, "command: " + commandType);
	    }
	}

	
	if(allowed)
	    return true;
	
	((LinkedList<?>)commandList).clear();
	fCommandQueue.set(state, commandList);

	turtles.put(clientState, e);

	turtles.put(clientState, null);
	mTerminate.invoke(e);
	return true;
    }

    /*    private void blockAction(TileEntity e) throws Exception {
        mTerminate.invoke(e);

        ChunkCoordinates c = new ChunkCoordinates(e.xCoord, e.yCoord, e.zCoord);
        if (canScream(c)) {
            Log.severe(String
                    .format("§4Stopped a computercraft turtle found @ dim %s, %s,%s,%s",
                            e.worldObj.provider.dimensionId, e.xCoord,
                            e.yCoord, e.zCoord));

            String msg = String
                    .format("A turtle stopped @ %s,%s,%s because it wasn't allowed there",
                            e.xCoord, e.yCoord, e.zCoord);
            String formatted = Formatter.formatChatSystem(msg,
                    ChatChannel.Local);
            CmdChat.sendChatToAround(e.worldObj.provider.dimensionId, e.xCoord,
                    e.yCoord, e.zCoord, formatted, null);

            anti_spam.put(c, System.currentTimeMillis() + 60000);
        }
    }*/

    private boolean canRoam(int dim, int x, int y, int z) throws Exception {
	// get world from dim
	WorldServer w = null;
	Iterator<WorldServer> it = MinecraftServer.getServer().worlds.iterator();
	while (it.hasNext()) {
	    w = it.next();
	    if (w.worldInfo.getDimension() == dim)
		break;
	}
	EntityPlayer turtlePlayer = (EntityPlayer) mGetPlayer.invoke(null, w);
	return canBuild(turtlePlayer, x, y, z);
	/*
        TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, y,
                y2, z);

        if (b == null || b.town() == null) {
            return MyTown.instance.getWorldWildSettings(dim).allowCCTurtles;
        }

        return b.settings.allowCCTurtles;*/
    }

    /*
    private boolean canScream(ChunkCoordinates c) {
        return anti_spam.get(c) == null;
    }

    private void cleanAntiSpam() {
        anti_spam_counter++;

        if (anti_spam_counter > 1000) {
            anti_spam_counter = 0;
            long time = System.currentTimeMillis();

            for (Iterator<Entry<ChunkCoordinates, Long>> it = anti_spam
                    .entrySet().iterator(); it.hasNext();) {
                Entry<ChunkCoordinates, Long> kv = it.next();
                if (kv.getValue() < time) {
                    it.remove();
                }
            }
        }
    }*/

    @Override
    public String getMod() {
	return "ComputerCraft";
    }

    @Override
    public String getComment() {
	return "Town permission: ccturtles ";
    }
}
