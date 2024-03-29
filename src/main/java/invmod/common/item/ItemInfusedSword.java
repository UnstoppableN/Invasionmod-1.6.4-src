package invmod.common.item;

import java.util.Random;

import invmod.common.mod_Invasion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class ItemInfusedSword extends ItemSword
{
    public ItemInfusedSword(int i)
    {
        super(i, EnumToolMaterial.EMERALD);
        this.maxStackSize = 1;
        //amount of entity hits it takes to recharge sword.
        this.setMaxDamage(40);
        this.setCreativeTab(mod_Invasion.tabInvmod);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon("invmod:" + getUnlocalizedName().substring(5));
    }

    @Override
    public boolean isDamageable()
    {
        return false;
    }

    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase entityliving1)
    {
        if (this.isDamaged(itemstack))
        {
            this.setDamage(itemstack, this.getDamage(itemstack) - 1);
        }

        return true;
    }

    @Override
    public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block)
    {
        if (par2Block.blockID == Block.web.blockID)
        {
            return 15.0F;
        }

        Material material = par2Block.blockMaterial;
        return (material != Material.plants) && (material != Material.vine) && (material != Material.coral) && (material != Material.leaves) && (material != Material.pumpkin) ? 1.0F : 1.5F;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.none;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 0;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        if (itemstack.getItemDamage() == 0)
        {
            //if player isSneaking then refill hunger else refill health
            if (entityplayer.isSneaking())
            {
                entityplayer.getFoodStats().addStats(6, 0.5f);
                world.playSoundAtEntity(entityplayer, "random.burp", 0.5F, 1F * 0.1F + 0.9F);
            }
            else
            {
                entityplayer.heal(6.0F);
                //spawn heart particles around the player
                world.spawnParticle("heart", entityplayer.posX + 1.5D, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("heart", entityplayer.posX - 1.5D, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ + 1.5D, 0.0D, 0.0D, 0.0D);
                world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ - 1.5D, 0.0D, 0.0D, 0.0D);
            }

            itemstack.setItemDamage(this.getMaxDamage());
        }

        return itemstack;
    }

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return block.blockID == Block.web.blockID;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, int par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase)
    {
        return true;
    }
}