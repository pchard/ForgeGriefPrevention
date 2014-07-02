package me.peerko.forgegriefprotection.mods;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import me.peerko.forgegriefprotection.DefaultFakePlayer;
import me.peerko.forgegriefprotection.ProtBase;

public class ThermalExpansion extends ProtBase {
    public static ThermalExpansion instance = new ThermalExpansion();

    Class<?> clFlorb;

    @Override
    public void load() throws Exception {
	clFlorb = Class.forName("thermalexpansion.entity.projectile.EntityFlorb");
	//getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean loaded() {
	return clFlorb != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
	Class<?> c = e.getClass();
	return c == clFlorb;
    }

    public boolean update(Entity e) throws Exception {

	if(e.isDead)
	    return true;

	if(e.getClass() == clFlorb) {
	    Vec3 vec3 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY, e.posZ);
	    Vec3 vec31 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
	    MovingObjectPosition movingobjectposition = e.worldObj.clip(vec3, vec31);
	    
	    // Check for Hits vs entites
	    List<Entity> list = e.worldObj.getEntitiesWithinAABBExcludingEntity(e, e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ).expand(1.0D, 1.0D, 1.0D));
	    EntityLivingBase entitylivingbase = ((EntityThrowable)e).getThrower();
	    
	    boolean canHit = false;
	    for(Entity ent : list) {
		if (ent.canBeCollidedWith() && (ent != entitylivingbase)) {
		    canHit = true;
		    break;
		}
	    }
	    
	    if(movingobjectposition != null || canHit) {
		e.setDead();
		boolean allowed;
		if(((EntityThrowable)e).thrower instanceof EntityPlayer)
		    allowed = canBuild((EntityPlayer)((EntityThrowable)e).thrower, (int)e.posX, (int)e.posY, (int)e.posZ);
		else
		    allowed = canBuild(DefaultFakePlayer.getPlayer(e.worldObj), (int)e.posX, (int)e.posY, (int)e.posZ);
		
		if(allowed) {
		    e.setDead();
		    return false;
		}
	    }
	}
	return true;
    }

    @Override
    public String getMod() {
	// TODO Auto-generated method stub
	return "Thermal Expansion";
    }

    @Override
    public String getComment() {
	// TODO Auto-generated method stub
	return "none";
    }
}
