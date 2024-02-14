package me.rufia.fightorflight.projectile;

import com.google.common.collect.Sets;
import me.rufia.fightorflight.init.FoFEntityTypes;
import me.rufia.fightorflight.init.FoFParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.minecraft.core.particles.ParticleTypes.*;
import static net.minecraft.sounds.SoundEvents.*;

public class FoFStoneProjectile extends ThrowableItemProjectile {

    public FoFStoneProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }
    public FoFStoneProjectile(EntityType<? extends ThrowableItemProjectile> entityType, double x, double y, double z, Level world) {
        super(entityType, x, y, z, world);
    }

    public FoFStoneProjectile(LivingEntity shooter, Level world) {
        super(FoFEntityTypes.FOF_STONE_PROJECTILE.get(), shooter, world);
    }
    // base item sprite
    protected Item getDefaultItem() {
        return Items.PLENTY_POTTERY_SHERD;
    }

    private ParticleOptions getParticle() {
        return (ParticleOptions)(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.COBBLESTONE)));
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3 = (new Vec3(x, y, z)).normalize().add(this.random.triangle(0.0, 0.0172275 * (double)inaccuracy), this.random.triangle(0.0, 0.0172275 * (double)inaccuracy), this.random.triangle(0.0, 0.0172275 * (double)inaccuracy)).scale((double)velocity);
        this.setDeltaMovement(vec3);
        double d = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3.y, d) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    protected static class HitProcedure {
        // particles/sounds on hit
        public static void emitParticles(LevelAccessor world, double x, double y, double z, double xOffset, double yOffset, double zOffset, int particleCount,  ParticleOptions particleOptions) {
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(particleOptions, x, y, z, particleCount, xOffset, yOffset, zOffset, 0.1);
            }
        }
        public static void emitSound(LevelAccessor world, double x, double y, double z) {
            if (world instanceof ServerLevel _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(x, y, z), NETHERRACK_BREAK , SoundSource.NEUTRAL, 1, 0.3F);
                } else {
                    _level.playLocalSound(x, y, z, GLASS_BREAK, SoundSource.NEUTRAL, 1, 0.3f, false);
                }

            }
        }

    }
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // on block hit: emit effects, splash damage
        HitProcedure.emitParticles(this.level(), this.getX(), this.getY(), this.getZ(),0.5,0.5,0.5, 15, this.getParticle());
        HitProcedure.emitSound(this.level(), this.getX(), this.getY(), this.getZ());
        AABB aabb = this.getBoundingBox().inflate(2.0D, 1.0D, 2.0D); // 2x1x2 hitbox
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aabb); // get living entities in hitbox
        if (!list.isEmpty()) { // for each living entity in hitbox:
           // Entity entity = this.getEffectSource(); // entity == proj. owner
            for(LivingEntity livingentity : list) {
                double d0 = this.distanceToSqr(livingentity);
                if (livingentity != this.getOwner()){
                if (d0 < 4.0D) {
                    double d1;
                    d1 = 1.0D - Math.sqrt(d0) / 2.0D;
                    float B1 = (float) d1 * 16;
                    livingentity.hurt(this.damageSources().thrown(this, this.getOwner()), B1);
                }
            }
            }
        }

        this.discard();
    }
	protected void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        HitProcedure.emitParticles(this.level(), this.getX(), this.getY(), this.getZ(),0.8,0.8,0.8, 15, this.getParticle());
        if (entity instanceof LivingEntity) {
            entity.hurt(this.damageSources().thrown(this, this.getOwner()), 7.0F);
        }
        this.discard();

   }

   @Override
   public void tick(){
        super.tick();
        float timeCount = this.tickCount / 2.0F;
        if (timeCount == (int)timeCount) {
            HitProcedure.emitParticles(this.level(), this.getX(), this.getY(), this.getZ(),0.2,0.2,0.2, 1, this.getParticle());
        }

 }

}
