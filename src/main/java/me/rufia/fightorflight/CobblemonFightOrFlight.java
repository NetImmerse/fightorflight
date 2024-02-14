package me.rufia.fightorflight;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.rufia.fightorflight.config.FightOrFlightCommonConfigModel;
import me.rufia.fightorflight.goals.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.logging.LogUtils;

public class CobblemonFightOrFlight {
    public static final String MODID = "fightorflight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final float AUTO_AGGRO_THRESHOLD = 50.0f;

    private static FightOrFlightCommonConfigModel config;
    private static TriConsumer<PokemonEntity, Integer, Goal> goalAdder;



    public static FightOrFlightCommonConfigModel config() {
        return config;
    }



    public static void init(TriConsumer<PokemonEntity, Integer, Goal> goalAdder) {
        CobblemonFightOrFlight.goalAdder = goalAdder;
        AutoConfig.register(FightOrFlightCommonConfigModel.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(FightOrFlightCommonConfigModel.class).getConfig();

    }

    public static void addPokemonGoal(PokemonEntity pokemonEntity) {
        float minimum_movement_speed = CobblemonFightOrFlight.config().minimum_movement_speed;
        float maximum_movement_speed = CobblemonFightOrFlight.config().maximum_movement_speed;
        float speed_limit= CobblemonFightOrFlight.config().speed_stat_limit;
        float speed = pokemonEntity.getPokemon().getSpeed();
        float speedMultiplier = minimum_movement_speed + (maximum_movement_speed - minimum_movement_speed) * speed/speed_limit;
        float fleeSpeed = 1.5f * speedMultiplier;

        float pursuitSpeed = 1.2f * speedMultiplier;



        goalAdder.accept(pokemonEntity, 1, new PokemonOwnerHurtByTargetGoal(pokemonEntity)); // Target entity that dared to hit Owner
        goalAdder.accept(pokemonEntity, 2, new PokemonOwnerHurtTargetGoal(pokemonEntity)); // Target entity hit by Owner
        goalAdder.accept(pokemonEntity, 3, new HurtByTargetGoal(pokemonEntity)); // Target entity pokemon is hit by
        goalAdder.accept(pokemonEntity, 3, new PokemonAvoidGoal(pokemonEntity, 48.0f, 1.0f, fleeSpeed)); // Avoid targeted entity
        // ------------------------------, 3 ,------------------------------------------- Melee/Ranged attack targeted entity goal
        goalAdder.accept(pokemonEntity, 4, new CaughtByTargetGoal(pokemonEntity)); // Target entity caught in pokeball by
        goalAdder.accept(pokemonEntity, 4, new PokemonPanicGoal(pokemonEntity, fleeSpeed)); // Panic conditions
        goalAdder.accept(pokemonEntity, 5, new PokemonNearestAttackableTargetGoal<>(pokemonEntity, Player.class, 48.0f, true, true)); // Seek player targets to attack
        goalAdder.accept(pokemonEntity, 6, new PokemonProactiveTargetGoal<>(pokemonEntity, Mob.class, 5, false, false, (arg) -> { // Seek Enemy targets (Owner only)
            return arg instanceof Enemy && !(arg instanceof Creeper);
        }));

    }

    public static boolean getRangedAttacker(PokemonEntity pokemonEntity){
        if (!CobblemonFightOrFlight.config().do_pokemon_attack) {
            return false;
        }
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.getAttack() < pokemon.getSpecialAttack()){
            return true;
        }
        else {return false;}


    }

    public static double getFightOrFlightCoefficient(PokemonEntity pokemonEntity) {
        if (!CobblemonFightOrFlight.config().do_pokemon_attack) {
            return -100;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();

        if (SpeciesAlwaysAggro(pokemon.getSpecies().getName().toLowerCase())) {
            //LogUtils.getLogger().info(pokemon.getSpecies().getName() + " Always Aggro");
            return 100;
        }
        if (SpeciesNeverAggro(pokemon.getSpecies().getName().toLowerCase())) {
            //LogUtils.getLogger().info(pokemon.getSpecies().getName() + " Never Aggro");
            return -100;
        }
        float levelMultiplier = CobblemonFightOrFlight.config().aggression_level_multiplier;
        double pkmnLevel = levelMultiplier * pokemon.getLevel();
        //double levelAggressionCoefficient = (pokemon.getLevel() - 20);
        double lowStatPenalty = (pkmnLevel * 1.5) + 30;
        double levelAggressionCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - lowStatPenalty;
        double atkDefRatioCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - (pokemon.getDefence() + pokemon.getSpecialDefence());
        double natureAggressionCoefficient = 0;
        double darknessAggressionCoefficient = 0;
        switch (pokemon.getNature().getDisplayName().toLowerCase()) {
            case "cobblemon.nature.docile":
            case "cobblemon.nature.timid":
            case "cobblemon.nature.gentle":
            case "cobblemon.nature.careful":
                natureAggressionCoefficient = -2;
                break;
            case "cobblemon.nature.relaxed":
            case "cobblemon.nature.lax":
            case "cobblemon.nature.quiet":
            case "cobblemon.nature.bashful":
            case "cobblemon.nature.calm":
                natureAggressionCoefficient = -1;
                break;
            case "cobblemon.nature.sassy":
            case "cobblemon.nature.hardy":
            case "cobblemon.nature.bold":
            case "cobblemon.nature.impish":
            case "cobblemon.nature.hasty":
                natureAggressionCoefficient = 1;
                break;
            case "cobblemon.nature.brave":
            case "cobblemon.nature.rash":
            case "cobblemon.nature.adamant":
            case "cobblemon.nature.naughty":
                natureAggressionCoefficient = 2;
                break;
            default:
                natureAggressionCoefficient = 0;
                break;
        }

        ElementalType typePrimary = pokemon.getPrimaryType();
        ElementalType typeSecondary = pokemon.getSecondaryType();
        if (typeSecondary == null) {
            typeSecondary = typePrimary;
        }

        boolean ghostLightLevelModifier = CobblemonFightOrFlight.config().ghost_light_level_aggro && (typePrimary.getName() == "ghost" || typeSecondary.getName() == "ghost");
        boolean darkLightLevelModifier = CobblemonFightOrFlight.config().dark_light_level_aggro && (typePrimary.getName() == "dark" || typeSecondary.getName() == "dark");

        if (ghostLightLevelModifier || darkLightLevelModifier) {
            int skyDarken = ((Entity) pokemonEntity).level().getSkyDarken();
            //LogUtils.getLogger().info(pokemon.getSpecies().getName() + " skyDarken: " + skyDarken);
            int lightLevel = ((Entity) pokemonEntity).level().getRawBrightness(pokemonEntity.blockPosition(), skyDarken);
            //LogUtils.getLogger().info(pokemon.getSpecies().getName() + " Raw Brightness: " + lightLevel);
            if (lightLevel <= 7) {
                darknessAggressionCoefficient = pkmnLevel;
            } else if (lightLevel >= 12) {
                darknessAggressionCoefficient -= pkmnLevel;
            }
        }

        //Weights and Clamps:
        levelAggressionCoefficient = Math.max(-(pkmnLevel + 5), Math.min(pkmnLevel, 1.5d * levelAggressionCoefficient));//5.0d * levelAggressionCoefficient;
        atkDefRatioCoefficient = Math.max(-pkmnLevel, 1.0d * atkDefRatioCoefficient);
        natureAggressionCoefficient = (pkmnLevel * 0.5) * natureAggressionCoefficient;//25.0d * natureAggressionCoefficient;

        double finalResult = levelAggressionCoefficient + atkDefRatioCoefficient + natureAggressionCoefficient + darknessAggressionCoefficient;

//        var pkmnString = "[" + pokemon.getSpecies().getName() + "]";
//        LOGGER.info(pkmnString + " levelAggressionCoefficient: " + levelAggressionCoefficient);
//        LOGGER.info(pkmnString + " atkDefRatioCoefficient: " + atkDefRatioCoefficient);
//        LOGGER.info(pkmnString + " natureAggressionCoefficient: " + natureAggressionCoefficient
//                + " (" + pokemon.getNature().getDisplayName().toLowerCase() + ")");
//
//        LOGGER.info("final FightOrFlightCoefficient: "
//                + levelAggressionCoefficient + "+" + atkDefRatioCoefficient + "+" + natureAggressionCoefficient
//                + " = " + finalResult);
        return finalResult;
    }

    public static boolean SpeciesAlwaysAggro(String speciesName) {
        //LogUtils.getLogger().info("Are " + speciesName + " always aggro?");
        for (String aggroSpecies : CobblemonFightOrFlight.config().always_aggro) {
            if (aggroSpecies.equals(speciesName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean SpeciesNeverAggro(String speciesName) {
        for (String passiveSpecies : CobblemonFightOrFlight.config().never_aggro) {
            if (passiveSpecies.equals(speciesName)) {
                return true;
            }
        }
        return false;
    }

    public static void PokemonEmoteAngry(Mob mob) {
        double particleSpeed = Math.random();
        double particleAngle = Math.random() * 2 * Math.PI;
        double particleXSpeed = Math.cos(particleAngle) * particleSpeed;
        double particleYSpeed = Math.sin(particleAngle) * particleSpeed;

        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
                    1, //Amount?
                    particleXSpeed, 0.5d, particleYSpeed,
                    1.0f); //Scale?
        } else {
            mob.level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                    mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
                    particleXSpeed, 0.5d, particleYSpeed);
        }
    }
}