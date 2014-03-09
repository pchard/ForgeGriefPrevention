package me.peerko.forgegriefprotection;

import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class DefaultFakePlayer extends EntityPlayer {

    private static WeakHashMap<World, DefaultFakePlayer> fakePlayers; 
    
    public static DefaultFakePlayer getPlayer(World world) {
	if(fakePlayers ==  null) {
	    fakePlayers = new WeakHashMap<World, DefaultFakePlayer>();
	}
	
	DefaultFakePlayer player = fakePlayers.get(world);
	
	if(player == null) {
	    player = new DefaultFakePlayer(world);
	    fakePlayers.put(world, player);
	}
	
	return player;
    }
    
    public DefaultFakePlayer(World world) {
	super(world, "Unknown");
	// TODO Auto-generated constructor stub
    }

    @Override
    public void sendChatToPlayer(ChatMessageComponent chatmessagecomponent) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean canCommandSenderUseCommand(int i, String s) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
	// TODO Auto-generated method stub
	return null;
    }

}
