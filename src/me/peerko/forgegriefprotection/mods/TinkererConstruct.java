package me.peerko.forgegriefprotection.mods;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import me.peerko.forgegriefprotection.ProtBase;

public class TinkererConstruct extends ProtBase {

    Class clExcavator, clHammer;
    
    Method mExcavatorMats, mHammerMats;

    public static TinkererConstruct instance = new TinkererConstruct();

    @Override
    public void load() throws Exception {
	clExcavator = Class.forName("tconstruct.items.tools.Excavator");
	clHammer = Class.forName("tconstruct.items.tools.Hammer");

	mExcavatorMats = clExcavator.getDeclaredMethod("getEffectiveMaterials");
	mExcavatorMats.setAccessible(true);
	
	mHammerMats = clHammer.getDeclaredMethod("getEffectiveMaterials");
	mHammerMats.setAccessible(true);
    }

    @Override
    public boolean loaded() {
	return clExcavator != null;
    }

    @Override
    public boolean update(EntityPlayer player, Item tool, ItemStack item, PlayerInteractEvent ev) {

	if(ev == null || ev.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
	    return true;

	if(clExcavator.isInstance(tool) || clHammer.isInstance(tool)) {
	    float yaw = (float)Math.toRadians(player.rotationYaw);
	    float pitch = (float)Math.toRadians(player.rotationPitch);
	    final float DIST = 10;

	    ForgeDirection dir = ForgeDirection.getOrientation(ev.face).getOpposite();

	    boolean canDestroy = false;
	    
	    Object matArray;
	    try {
		if(clExcavator.isInstance(item))
		    matArray = mExcavatorMats.invoke(tool);
		else //(clHammer.isInstance(item))
		    matArray = mHammerMats.invoke(tool);
		
	    } catch (Exception e) {
		e.printStackTrace();
		
		return true;
	    }
	    
	    ArrayList<Material> breakableMats = new ArrayList<Material>(Array.getLength(matArray));
	    
	    for(int i = 0; i < Array.getLength(matArray); i++) {
		breakableMats.add((Material)Array.get(matArray, i));
	    }

	    for(int x = -1; x <= 1; x++) {
		for(int y = -1; y <= 1; y++) {

		    if(x == 0 && y == 0)
			continue;

		    if(dir.offsetX != 0) {
			if(breakableMats.contains(player.worldObj.getBlockMaterial(ev.x, ev.y + x, ev.z + y)) && !canBuild(player, ev.x, ev.y + x, ev.z + y))
			    return false;
		    }
		    else if(dir.offsetY != 0) {
			if(breakableMats.contains(player.worldObj.getBlockMaterial(ev.x + x, ev.y, ev.z + y)) && !canBuild(player, ev.x + x, ev.y, ev.z + y))
			    return false;
		    }
		    else {
			if(breakableMats.contains(player.worldObj.getBlockMaterial(ev.x + x, ev.y + y, ev.z)) && !canBuild(player, ev.x + x, ev.y + y, ev.z))
			    return false;
		    }
		}
	    }
	}
	return true;
    }

    @Override
    public boolean isEntityInstance(Item item) {
	return clExcavator.isInstance(item) || clHammer.isInstance(item);
    }

    @Override
    public String getMod() {
	return "Tinkerer's construct";
    }

    @Override
    public String getComment() {
	return "Makes some tools obey bukkit";
    }
}
