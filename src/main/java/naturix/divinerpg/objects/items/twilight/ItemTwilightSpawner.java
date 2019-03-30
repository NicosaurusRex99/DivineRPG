package naturix.divinerpg.objects.items.twilight;

import naturix.divinerpg.objects.entities.entity.twilight.*;
import naturix.divinerpg.objects.items.base.ItemBase;
import naturix.divinerpg.registry.DRPGCreativeTabs;
import naturix.divinerpg.registry.ModDimensions;
import naturix.divinerpg.registry.ModItems;
import naturix.divinerpg.utils.log.Logging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemTwilightSpawner extends ItemBase {

	public ItemTwilightSpawner(String name) {
		super(name);
		setMaxStackSize(1);
		this.setCreativeTab(DRPGCreativeTabs.spawner);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
	        float hitX, float hitY, float hitZ) {
		ItemStack stack = new ItemStack(this);
		if (!world.isRemote && world.provider.getDimensionType().getId() == ModDimensions.mortumDimension.getId()) {
			if (this == ModItems.karotCrystal) {
				Karot e = new Karot(world);
				e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
				world.spawnEntity(e);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return EnumActionResult.PASS;
			}
			if (this == ModItems.densosCrystal || this == ModItems.reyvorCrystal) {
				Densos e = new Densos(world);
				Reyvor e1 = new Reyvor(world);

				e.setPosition(pos.getX() + 1, pos.getY() + 1, pos.getZ());
				e1.setPosition(pos.getX() - 1, pos.getY() + 1, pos.getZ());

				world.spawnEntity(e);
				world.spawnEntity(e1);

				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
					;
				}
				return EnumActionResult.PASS;
			}
			if (this == ModItems.soulFiendCrystal) {
				SoulFiend e = new SoulFiend(world);
				e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
				world.spawnEntity(e);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return EnumActionResult.PASS;
			}
			if (this == ModItems.twilightDemonCrystal) {
				TwilightDemon e = new TwilightDemon(world);
				e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
				world.spawnEntity(e);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return EnumActionResult.PASS;
			}
			if (this == ModItems.vamacheronCrystal) {
				Vamacheron e = new Vamacheron(world);
				e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
				world.spawnEntity(e);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return EnumActionResult.PASS;
			}
			if (this == ModItems.eternalArcherCrystal) {
				EternalArcher e = new EternalArcher(world);
				e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
				world.spawnEntity(e);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return EnumActionResult.PASS;
			}

			if (world.provider.getDimensionType().getId() != ModDimensions.mortumDimension.getId()) {
				Logging.message(player, TextFormatting.AQUA + "This item can only be used in Mortum.");
			}
		}
		return EnumActionResult.FAIL;
	}
}