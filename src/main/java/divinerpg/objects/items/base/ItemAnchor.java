package divinerpg.objects.items.base;

import divinerpg.enums.BulletType;
import divinerpg.objects.entities.entity.projectiles.EntityAnchor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemAnchor extends Item {

    protected EntityAnchor anchorProjectile = null;

    public ItemAnchor(String registryName) {
        this.setRegistryName(registryName);
        this.setUnlocalizedName(registryName);

        this.setMaxDamage(64);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    protected boolean currentlyCast() {
        return this.anchorProjectile != null;
    }

    public void clearAnchorProjectile() {
        this.anchorProjectile = null;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (currentlyCast()) {
            int i = anchorProjectile.handleHookRetraction();
            itemstack.damageItem(i, playerIn);
            playerIn.swingArm(handIn);
            worldIn.playSound((EntityPlayer) null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        } else {
            worldIn.playSound((EntityPlayer) null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            if(!worldIn.isRemote) {
                EntityAnchor entityAnchor = new EntityAnchor(worldIn, playerIn);
                entityAnchor.setItem(this);
                this.anchorProjectile = entityAnchor;
                worldIn.spawnEntity(entityAnchor);

                playerIn.swingArm(handIn);
                playerIn.addStat(StatList.getObjectUseStats(this));
            }
        }

        return new ActionResult(EnumActionResult.SUCCESS, itemstack);
    }

    public int getItemEnchantability() {
        return 1;
    }
}
