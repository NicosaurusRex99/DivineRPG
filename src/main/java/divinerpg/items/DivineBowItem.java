package divinerpg.items;

import divinerpg.entities.projectiles.DivineArrowEntity;
import divinerpg.utils.properties.item.ExtendedItemProperties;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DivineBowItem extends BowItem {
    /**
     * Default Arrow is used for shooting if bow if infinite
     */
    private final List<ArrowItem> arrows = new ArrayList<>();
    private final boolean isInfinite;
    private final int damage;
    private final Consumer<RayTraceResult> hitAction;
    private final int duration;
    private final SoundEvent sound;

    public DivineBowItem(ExtendedItemProperties builder, int arrowDamage, Consumer<RayTraceResult> onHit, SoundEvent sound) {
        super(builder);
        this.damage = arrowDamage;
        this.isInfinite = builder.infiniteArrows;
        this.hitAction = onHit;
        duration = builder.bowLikeDuration;
        this.sound = sound;

        arrows.addAll(builder.possibleArrows);
        if (arrows.isEmpty()) {
            arrows.add((ArrowItem) Items.ARROW);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack bow, World worldIn, LivingEntity entityLiving, int timeLeft) {
        // Can work only with PLayerEntity
        if (!(entityLiving instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) entityLiving;
        boolean flag = player.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow) > 0;
        ItemStack ammo = player.findAmmo(bow);

        int power = this.getUseDuration(bow) - timeLeft;
        power = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(bow, worldIn, player, power, !ammo.isEmpty() || flag);
        if (power < 0) return;

        if (!ammo.isEmpty() || flag || isInfinite) {

            float f = getArrowVelocity(power);
            if (!((double) f < 0.1D)) {
                boolean infiniteArrow = player.abilities.isCreativeMode
                        || isInfinite
                        || (ammo.getItem() instanceof ArrowItem && ((ArrowItem) ammo.getItem()).isInfinite(ammo, bow, player));

                if (!worldIn.isRemote) {
                    // same re
                    AbstractArrowEntity arrowEntity = createNew(worldIn, ammo, player, getRegistryName().getPath());
                    arrowEntity = customeArrow(arrowEntity);
                    arrowEntity.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                    if (f == 1.0F) {
                        arrowEntity.setIsCritical(true);
                    }

                    int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, bow);
                    if (j > 0) {
                        arrowEntity.setDamage(arrowEntity.getDamage() + (double) j * 0.5D + 0.5D);
                    }

                    int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, bow);
                    if (k > 0) {
                        arrowEntity.setKnockbackStrength(k);
                    }

                    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, bow) > 0) {
                        arrowEntity.setFire(100);
                    }

                    bow.damageItem(1, player, (playerEntity) -> {
                        playerEntity.sendBreakAnimation(player.getActiveHand());
                    });

                    if (infiniteArrow || player.abilities.isCreativeMode && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                        arrowEntity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                    }

                    worldIn.addEntity(arrowEntity);
                }

                worldIn.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                if (!infiniteArrow && !player.abilities.isCreativeMode) {
                    ammo.shrink(1);
                    if (ammo.isEmpty()) {
                        player.inventory.deleteStack(ammo);
                    }
                }

                player.addStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    protected AbstractArrowEntity createNew(World world, ItemStack bow, PlayerEntity player, String arrowName) {
        DivineArrowEntity arrowEntity = new DivineArrowEntity(world, player, arrowName, this.damage, this.hitAction);
        arrowEntity.setPotionEffect(bow);
        return arrowEntity;
    }

    @Override
    public Predicate<ItemStack> getAmmoPredicate() {
        return itemStack -> arrows.contains(itemStack.getItem());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return duration;
    }
}
