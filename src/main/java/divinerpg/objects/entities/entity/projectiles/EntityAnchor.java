package divinerpg.objects.entities.entity.projectiles;

import java.util.Iterator;
import java.util.List;

import divinerpg.objects.items.base.ItemAnchor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAnchor extends Entity {
    private static final DataParameter<Integer> DATA_HOOKED_ENTITY;
    private boolean inGround;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;

    private divinerpg.objects.entities.entity.projectiles.EntityAnchor.State currentState;

    private ItemAnchor anchorItem;
    private EntityPlayer angler;
    public Entity caughtEntity;

    public EntityAnchor(World worldIn) {
        this(worldIn, null);
    }

    @SideOnly(Side.CLIENT)
    public EntityAnchor(World worldIn, EntityPlayer p_i47290_2_, double x, double y, double z) {
        super(worldIn);
        this.currentState = divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.FLYING;
        this.init(p_i47290_2_);
        this.setPosition(x, y, z);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
    }

    public EntityAnchor(World worldIn, EntityPlayer fishingPlayer) {
        super(worldIn);
        this.currentState = divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.FLYING;
        this.init(fishingPlayer);
        this.shoot();
    }

    private void init(EntityPlayer p_190626_1_) {
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.angler = p_190626_1_;
    }

    public void setItem(ItemAnchor item) {
        this.anchorItem = item;
    }

    private void shoot() {
        float f = this.angler.prevRotationPitch + (this.angler.rotationPitch - this.angler.prevRotationPitch);
        float f1 = this.angler.prevRotationYaw + (this.angler.rotationYaw - this.angler.prevRotationYaw);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        double d0 = this.angler.prevPosX + (this.angler.posX - this.angler.prevPosX) - (double)f3 * 0.3D;
        double d1 = this.angler.prevPosY + (this.angler.posY - this.angler.prevPosY) + (double)this.angler.getEyeHeight();
        double d2 = this.angler.prevPosZ + (this.angler.posZ - this.angler.prevPosZ) - (double)f2 * 0.3D;
        this.setLocationAndAngles(d0, d1, d2, f1, f);
        this.motionX = (double)(-f3);
        this.motionY = (double)MathHelper.clamp(-(f5 / f4), -5.0F, 5.0F);
        this.motionZ = (double)(-f2);
        float f6 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.motionX *= 0.6D / (double)f6 + 0.5D + this.rand.nextGaussian() * 0.0045D;
        this.motionY *= 0.6D / (double)f6 + 0.5D + this.rand.nextGaussian() * 0.0045D;
        this.motionZ *= 0.6D / (double)f6 + 0.5D + this.rand.nextGaussian() * 0.0045D;
        float f7 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);
        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f7) * 57.29577951308232D);
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    protected void entityInit() {
        this.getDataManager().register(DATA_HOOKED_ENTITY, 0);
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        if (DATA_HOOKED_ENTITY.equals(key)) {
            int i = (Integer)this.getDataManager().get(DATA_HOOKED_ENTITY);
            this.caughtEntity = i > 0 ? this.world.getEntityByID(i - 1) : null;
        }

        super.notifyDataManagerChange(key);
    }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 4096.0D;
    }

    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
    }

    public void onUpdate() {
        super.onUpdate();
        if (this.angler == null) {
            System.out.println("No more angler");
            this.setDead();
        } else if (this.world.isRemote || !this.shouldStopFishing()) {
            if (this.inGround) {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200) {
                    this.setDead();
                    return;
                }
            }

            float f = 0.0F;
            BlockPos blockpos = new BlockPos(this);
            IBlockState iblockstate = this.world.getBlockState(blockpos);
            if (iblockstate.getMaterial() == Material.WATER) {
                f = BlockLiquid.getBlockLiquidHeight(iblockstate, this.world, blockpos);
            }

            double d1;
            if (this.currentState == divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.FLYING) {
                if (this.caughtEntity != null) {
                    this.motionX = 0.0D;
                    this.motionY = 0.0D;
                    this.motionZ = 0.0D;
                    this.currentState = divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (f > 0.0F) {
                    this.motionX *= 0.3D;
                    this.motionY *= 0.2D;
                    this.motionZ *= 0.3D;
                    this.currentState = divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.BOBBING;
                    return;
                }

                if (!this.world.isRemote) {
                    this.checkCollision();
                }

                if (!this.inGround && !this.onGround && !this.collidedHorizontally) {
                    ++this.ticksInAir;
                } else {
                    this.ticksInAir = 0;
                    this.motionX = 0.0D;
                    this.motionY = 0.0D;
                    this.motionZ = 0.0D;
                }
            } else {
                if (this.currentState == divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.HOOKED_IN_ENTITY) {
                    if (this.caughtEntity != null) {
                        if (this.caughtEntity.isDead) {
                            this.caughtEntity = null;
                            this.currentState = divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.FLYING;
                        } else {
                            this.posX = this.caughtEntity.posX;
                            d1 = (double)this.caughtEntity.height;
                            this.posY = this.caughtEntity.getEntityBoundingBox().minY + d1 * 0.8D;
                            this.posZ = this.caughtEntity.posZ;
                            this.setPosition(this.posX, this.posY, this.posZ);
                        }
                    }

                    return;
                }

                if (this.currentState == divinerpg.objects.entities.entity.projectiles.EntityAnchor.State.BOBBING) {
                    this.motionX *= 0.9D;
                    this.motionZ *= 0.9D;
                    d1 = this.posY + this.motionY - (double)blockpos.getY() - (double)f;
                    if (Math.abs(d1) < 0.01D) {
                        d1 += Math.signum(d1) * 0.1D;
                    }

                    this.motionY -= d1 * (double)this.rand.nextFloat() * 0.2D;
                    if (!this.world.isRemote && f > 0.0F) {
                        //this.catchingFish(blockpos);
                    }
                }
            }

            if (iblockstate.getMaterial() != Material.WATER) {
                this.motionY -= 0.03D;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.updateRotation();
            d1 = 0.92D;
            this.motionX *= 0.92D;
            this.motionY *= 0.92D;
            this.motionZ *= 0.92D;
            this.setPosition(this.posX, this.posY, this.posZ);
        }

    }

    private boolean shouldStopFishing() {
        ItemStack itemstack = this.angler.getHeldItemMainhand();
        ItemStack itemstack1 = this.angler.getHeldItemOffhand();
        boolean flag = itemstack.getItem() instanceof ItemAnchor;
        boolean flag1 = itemstack1.getItem() instanceof ItemAnchor;
        if (!this.angler.isDead && this.angler.isEntityAlive() && (flag || flag1) && this.getDistanceSq(this.angler) <= 1024.0D) {
            return false;
        } else {
            this.setDead();
            return true;
        }
    }

    private void updateRotation() {
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

        for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.29577951308232D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
        }

        while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
    }

    private void checkCollision() {
        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1, false, true, false);
        vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        if (raytraceresult != null) {
            vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
        }

        Entity entity = null;
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
        double d0 = 0.0D;
        Iterator var8 = list.iterator();

        while(true) {
            Entity entity1;
            double d1;
            do {
                RayTraceResult raytraceresult1;
                do {
                    do {
                        do {
                            if (!var8.hasNext()) {
                                if (entity != null) {
                                    raytraceresult = new RayTraceResult(entity);
                                }

                                if (raytraceresult != null && raytraceresult.typeOfHit != Type.MISS) {
                                    if (raytraceresult.typeOfHit == Type.ENTITY) {
                                        boolean damageEntity = false;
                                        if(this.caughtEntity == null) {
                                            damageEntity = true;
                                        }
                                        this.caughtEntity = raytraceresult.entityHit;
                                        this.setHookedEntity();
                                        if(damageEntity) {
                                            this.caughtEntity.attackEntityFrom(DamageSource.GENERIC, 15);
                                        }

                                    } else {
                                        this.inGround = true;
                                    }
                                }

                                return;
                            }

                            entity1 = (Entity)var8.next();
                        } while(!this.canBeHooked(entity1));
                    } while(entity1 == this.angler && this.ticksInAir < 5);

                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
                    raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
                } while(raytraceresult1 == null);

                d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);
            } while(d1 >= d0 && d0 != 0.0D);

            entity = entity1;
            d0 = d1;
        }
    }

    private void setHookedEntity() {
        this.getDataManager().set(DATA_HOOKED_ENTITY, this.caughtEntity.getEntityId() + 1);
    }

    protected boolean canBeHooked(Entity p_189739_1_) {
        return p_189739_1_.canBeCollidedWith() || p_189739_1_ instanceof EntityItem;
    }

    public void writeEntityToNBT(NBTTagCompound compound) {
    }

    public void readEntityFromNBT(NBTTagCompound compound) {
    }

    public int handleHookRetraction() {
        if (!this.world.isRemote && this.angler != null) {
            int i = 0;
            ItemFishedEvent event = null;
            if (this.caughtEntity != null) {
                this.bringInHookedEntity();
                this.world.setEntityState(this, (byte)31);
                i = this.caughtEntity instanceof EntityItem ? 3 : 5;
            } else if (this.ticksCatchable > 0) {
                if (event.isCanceled()) {
                    this.setDead();
                    return event.getRodDamage();
                }
            }

            if (this.inGround) {
                i = 2;
            }

            this.setDead();
            return event == null ? i : event.getRodDamage();
        } else {
            return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 31 && this.world.isRemote && this.caughtEntity instanceof EntityPlayer && ((EntityPlayer)this.caughtEntity).isUser()) {
            this.bringInHookedEntity();
        }

        super.handleStatusUpdate(id);
    }

    protected void bringInHookedEntity() {
        if (this.angler != null) {
            double d0 = this.angler.posX - this.posX;
            double d1 = this.angler.posY - this.posY;
            double d2 = this.angler.posZ - this.posZ;
            double d3 = 0.1D;
            Entity var10000 = this.caughtEntity;
            var10000.motionX += d0 * 0.1D;
            var10000 = this.caughtEntity;
            var10000.motionY += d1 * 0.1D;
            var10000 = this.caughtEntity;
            var10000.motionZ += d2 * 0.1D;
        }

    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public void setDead() {
        super.setDead();
        if(this.anchorItem != null) {
            this.anchorItem.clearAnchorProjectile();
        }
    }

    public EntityPlayer getAngler() {
        return this.angler;
    }

    static {
        DATA_HOOKED_ENTITY = EntityDataManager.createKey(divinerpg.objects.entities.entity.projectiles.EntityAnchor.class, DataSerializers.VARINT);
    }

    static enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;

        private State() {
        }
    }
}
