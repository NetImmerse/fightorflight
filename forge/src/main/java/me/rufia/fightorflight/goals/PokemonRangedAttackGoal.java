package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.math.UMathKt;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import me.rufia.fightorflight.projectile.FoFStoneProjectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

import java.util.EnumSet;
import java.util.Objects;

public class PokemonRangedAttackGoal extends Goal {

    protected final PathfinderMob mob;
    public int ticksUntilNewAngerParticle = 0;
    public int ticksUntilNewAngerCry = 0;
    private int ticksUntilRangedAttack = 0;
    private int ticksRecoil = 0;
    private int ticksUntilNextPathRecalculation = 0;
    private boolean followingTargetEvenIfNotSeen;

    private boolean GTFO = true;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private double speedModifier;

    private double fleeSpeedModifier;
    private long lastCanUseCheck;
    private int seeTime = 0;

    public PokemonRangedAttackGoal(PathfinderMob mob, double speedModifier, double fleeSpeedModifier, boolean followingTargetEvenIfNotSeen){
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.fleeSpeedModifier = fleeSpeedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public void tick(){
        super.tick();

        // emit anger particles for wild pokemon
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        LivingEntity owner = pokemonEntity.getOwner();
        if (owner == null){
            if (ticksUntilNewAngerParticle < 1) {
                CobblemonFightOrFlight.PokemonEmoteAngry(this.mob);
                ticksUntilNewAngerParticle = 10;
            }
            else { ticksUntilNewAngerParticle = ticksUntilNewAngerParticle - 1; }

            if (ticksUntilNewAngerCry < 1) {
                pokemonEntity.cry();
                ticksUntilNewAngerCry = 100 + (int)(Math.random() * 200);
            }
            else { ticksUntilNewAngerCry = ticksUntilNewAngerCry - 1; }
        }


        if (!CobblemonFightOrFlight.config().do_pokemon_attack_in_battle){
            if (isTargetInBattle()){
                this.mob.getNavigation().setSpeedModifier(0);
            }
        }

        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null) {
            this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
            double d = this.mob.getPerceivedTargetDistanceSquareForMeleeAttack(livingEntity);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingEntity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0 || livingEntity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = livingEntity.getX();
                this.pathedTargetY = livingEntity.getY();
                this.pathedTargetZ = livingEntity.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                if (d > 1024.0) { //32 blocks
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (d > 256.0) { //16 blocks
                    this.ticksUntilNextPathRecalculation += 5;
                }

                //if (!this.mob.getNavigation().moveTo(livingEntity, this.speedModifier)) {
                //    this.ticksUntilNextPathRecalculation += 15;
                //}

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

        }


        if ( livingEntity != null && this.mob.getSensing().hasLineOfSight(livingEntity)) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }
            if (ticksRecoil > 0){
                this.mob.getNavigation().stop();
                GTFO = true;
            }
            if (livingEntity != null) {
                if (!pokemonEntity.getPokemon().isPlayerOwned() && this.mob.distanceToSqr(livingEntity) < 25.0F && ticksUntilNextPathRecalculation <= 0 && !this.mob.getNavigation().isInProgress())
                {
                    GTFO = false;
                    PathfindAwayFromTarget(this.mob.getTarget(), fleeSpeedModifier);
                    ticksUntilNextPathRecalculation += 5;
                }
                else if (this.mob.distanceToSqr(livingEntity) > 100.0F && this.mob.getNavigation().isDone()) {
                    PathfindToTarget(this.mob.getTarget(), speedModifier);
                    GTFO = true;
                } else if (this.mob.distanceToSqr(livingEntity) < 64.0F && this.seeTime >= 10 && GTFO) {
                    this.mob.getNavigation().stop();


                }
            }

        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);


        // conditions for ranged attack
        if (ticksUntilRangedAttack <= 0 && this.mob.getTarget() != null && this.seeTime >= 10 && GTFO) {
            performRangedAttack(this.mob.getTarget());
            ticksUntilRangedAttack = 30;
            ticksRecoil = 10;
        }
        else
        {
            ticksUntilRangedAttack = Math.max(this.ticksUntilRangedAttack - 1, 0);
            ticksRecoil = Math.max(this.ticksRecoil - 1, 0);
        }


    } ////// tick spaghetti end

    public boolean canUse() {
        long l = this.mob.level().getGameTime(); // get
        if (l - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = l;
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity == null) {
                return false;
            } else if (!livingEntity.isAlive()) {
                return false;
            } else {
                //this.path = this.mob.getNavigation().createPath(livingEntity, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return shouldFightTarget();
                }
            }
        }
    }

    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        } else if (!livingEntity.isAlive()) {
            return false;
        }
        else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(livingEntity.blockPosition())) {
            return false;
        } else {
            return shouldFightTarget() && !(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player)livingEntity).isCreative();
        }
    }

    public void start() {
        this.GTFO = true;
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilRangedAttack = 10;
        this.ticksRecoil = 0;
    }

    public void stop() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
            this.mob.setTarget((LivingEntity)null);
        }

        this.mob.setAggressive(false);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void PathfindToTarget(LivingEntity target, double speedModifier)
    {
        this.path = this.mob.getNavigation().createPath(target, 0); // set pokemon path TO TARGET
        this.mob.getNavigation().moveTo(this.path, speedModifier); // make pokemon navigate TO TARGET
    }

    public void PathfindAwayFromTarget(LivingEntity target, double speedModifier)
    {
        Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 9, 7, target.position()); //get position coords away from target
        if (vec3 != null) {
            this.path = this.mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0); // set path to position coords
            this.mob.getNavigation().moveTo(this.path, speedModifier); // make pokemon navigate AWAY FROM TARGET
        }

    }

    private boolean isAggressiveEnoughToMeleeAttack(){
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        if (!pokemonEntity.getPokemon().isPlayerOwned() && this.mob.getTarget().distanceToSqr(this.mob) < 4.0F && CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) >= 40.0F ) {
            return true;
        }
        else return pokemonEntity.getPokemon().isPlayerOwned();
    }

    public void performRangedAttack(Entity target) {
        //  double x = this.mob.getX();
        //  double y = this.mob.getX();
        //  double z = this.mob.getX();
        FoFStoneProjectile PokemonProjectile = new FoFStoneProjectile(this.mob, this.mob.level());
        double d0 = target.getY() + target.getEyeHeight() - 1.1;
        double d1 = target.getX() - this.mob.getX();
        double d3 = target.getZ() - this.mob.getZ();
        PokemonProjectile.shoot(d1, d0 - PokemonProjectile.getY() + Math.sqrt(d1 * d1 + d3 * d3) * 0.6F, d3, 0.57F, 6.0F);
        this.mob.level().addFreshEntity(PokemonProjectile);
    }

    public boolean isTargetInBattle(){
        if (this.mob.getTarget() instanceof ServerPlayer targetAsPlayer){
            return BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(targetAsPlayer) != null;
        }
        return false;
    }

    public boolean shouldFightTarget(){

        // CHECK MINIMAL LEVEL THRESHOLD
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        if (pokemonEntity.getPokemon().getLevel() < CobblemonFightOrFlight.config().minimum_attack_level) { return false; }

        // CHECK FOR OWNER
        LivingEntity owner = pokemonEntity.getOwner(); // get pokemon's owner
        if (owner != null){ // if owner exists
            if (!CobblemonFightOrFlight.config().do_pokemon_defend_owner) { return false; } // config: do pokemon defend owner = false
            if (this.mob.getTarget() == null || this.mob.getTarget() == owner) { return false; } // target is null OR target is owner
            if (this.mob.getTarget() instanceof PokemonEntity targetPokemon){ // if target is pokemon...
                LivingEntity targetOwner = targetPokemon.getOwner(); // get target pokemon's owner
                if (targetOwner != null){ // if pokemon is owned by another player...
                    if (targetOwner == owner) { return false; } // target - pokemon's owner
                    if (!CobblemonFightOrFlight.config().do_player_pokemon_attack_other_player_pokemon) {return false;}
                }
            }
            if (!CobblemonFightOrFlight.getRangedAttacker(pokemonEntity)) { // pokemon is NOT a ranged attacker
                return false;
            }

            if (this.mob.getTarget() instanceof Player){
                if (!CobblemonFightOrFlight.config().do_player_pokemon_attack_other_players){
                    return false;
                }
            }

        } else { // if no owner
            if (this.mob.getTarget() != null){ //if target is not null
                if (CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) <= 0) { return false; } // Fight/Flight coeff. is less than 0

                if (isAggressiveEnoughToMeleeAttack()) {
                    return false;
                }

                LivingEntity targetEntity = this.mob.getTarget(); // get target
                if (this.mob.distanceToSqr(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()) > 400) { return false; } // Distance to the target is greater than 400
            }
        }


        return !pokemonEntity.isBusy(); // true if pokemon is not in battle
    }



}
