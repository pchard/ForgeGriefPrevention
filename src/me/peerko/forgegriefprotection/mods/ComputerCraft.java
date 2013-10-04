package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import me.peerko.forgegriefprotection.ProtBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ComputerCraft extends ProtBase {
    public static ComputerCraft instance = new ComputerCraft();

    Class<?> clTurtle = null, clTurtlePlayer;
    Method mTerminate, mIsOn, mGetPlayer;
    Field fMoved, fClientState;

    public HashMap<Object, Object> turtles = new HashMap<Object, Object>();
    public HashMap<ChunkCoordinates, Long> anti_spam = new HashMap<ChunkCoordinates, Long>();
    public int anti_spam_counter = 0;

    @Override
    public void reload() {
        anti_spam_counter = 0;
        turtles = new HashMap<Object, Object>();
        anti_spam = new HashMap<ChunkCoordinates, Long>();
    }

    @Override
    public void load() throws Exception {
        clTurtle = Class.forName("dan200.turtle.shared.TileEntityTurtle");
        mTerminate = clTurtle.getDeclaredMethod("terminate");
        mIsOn = clTurtle.getDeclaredMethod("isOn");
        fMoved = clTurtle.getDeclaredField("m_moved");
        fClientState = clTurtle.getDeclaredField("m_clientState");
        fClientState.setAccessible(true);
        clTurtlePlayer = Class.forName("dan200.turtle.shared.TurtlePlayer");
        mGetPlayer = clTurtlePlayer.getDeclaredMethod("getPlayer", World.class);
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

        Object state = fClientState.get(e);
        Object prev_turtle = turtles.get(state);

        if (prev_turtle == e || !(Boolean) mIsOn.invoke(e)) {
            return true;
        }

        turtles.put(state, e);

        int radius = 1;
        int dim = e.worldObj.provider.dimensionId;
        if (canRoam(dim, e.xCoord - radius, e.yCoord, e.zCoord)
                && canRoam(dim, e.xCoord + radius, e.yCoord, e.zCoord)
                && canRoam(dim, e.xCoord, e.yCoord, e.zCoord - radius)
                && canRoam(dim, e.xCoord, e.yCoord, e.zCoord + radius)
                && canRoam(dim, e.xCoord, e.yCoord - radius, e.zCoord)
                && canRoam(dim, e.xCoord, e.yCoord + radius, e.zCoord)) {
            return true;
        }

        turtles.put(state, null);
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
