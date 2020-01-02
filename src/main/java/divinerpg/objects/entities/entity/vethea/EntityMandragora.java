package divinerpg.objects.entities.entity.vethea;

import divinerpg.objects.entities.entity.projectiles.EntityMandragoraProjectile;
import divinerpg.registry.DRPGLootTables;
import divinerpg.registry.ModSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityMandragora extends VetheaMob {

    public EntityMandragora(World var1) {
        super(var1);
        addAttackingAI();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(35);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20);
    }

    @Override
    public int getSpawnLayer() {
        return 2;
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        EntityPlayer target = this.world.getClosestPlayerToEntity(this, 16);
        if(!world.isRemote && target != null && this.ticksExisted%20 == 0) attackEntity(target);
    }

    public void attackEntity(EntityLivingBase e) {
        double tx = e.posX - this.posX;
        double ty = e.getEntityBoundingBox().minY - this.posY;
        double tz = e.posZ - this.posZ;
        EntityMandragoraProjectile p = new EntityMandragoraProjectile(this.world, this);
//        p.setThrowableHeading(tx, ty, tz, 1.3f, 15);
        this.playSound(ModSounds.MANDRAGORA, 2.0F, 2.0F);
        if(!world.isRemote)this.world.spawnEntity(p);
    }

    @Override
	protected ResourceLocation getLootTable() {
        return DRPGLootTables.ENTITIES_MANDRAGORA;
    }    
    
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MANDRAGORA;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MANDRAGORA;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MANDRAGORA;
    }

    

}