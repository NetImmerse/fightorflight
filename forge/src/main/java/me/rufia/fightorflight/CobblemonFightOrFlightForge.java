package me.rufia.fightorflight;


import me.rufia.fightorflight.goals.*;
import me.rufia.fightorflight.init.FoFEntityTypes;
import me.rufia.fightorflight.init.FoFParticleTypes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;


import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.util.TriConsumer;


@Mod(CobblemonFightOrFlight.MODID)
@Mod.EventBusSubscriber
public class CobblemonFightOrFlightForge {

    private static TriConsumer<PokemonEntity, Integer, Goal> goalAdder;

    public static void init(TriConsumer<PokemonEntity, Integer, Goal> goalAdder) {
        CobblemonFightOrFlightForge.goalAdder = goalAdder;
    }

    public CobblemonFightOrFlightForge() {
        CobblemonFightOrFlight.init((pokemonEntity, priority, goal) -> pokemonEntity.goalSelector.addGoal(priority, goal));
        CobblemonFightOrFlightForge.init((pokemonEntity, priority, goal) -> pokemonEntity.goalSelector.addGoal(priority, goal));
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // REGISTERS
        FoFEntityTypes.ENTITY_TYPES.register(modEventBus);
        FoFParticleTypes.PARTICLE_TYPES.register(modEventBus);
    }


    public static void addPokemonGoal(PokemonEntity pokemonEntity) {
        float minimum_movement_speed = CobblemonFightOrFlight.config().minimum_movement_speed;
        float maximum_movement_speed = CobblemonFightOrFlight.config().maximum_movement_speed;
        float speed_limit= CobblemonFightOrFlight.config().speed_stat_limit;
        float speed = pokemonEntity.getPokemon().getSpeed();
        float speedMultiplier = minimum_movement_speed + (maximum_movement_speed - minimum_movement_speed) * speed/speed_limit;

        float fleeSpeed = 1.5f * speedMultiplier;
        float pursuitSpeed = 1.2f * speedMultiplier;
        goalAdder.accept(pokemonEntity, 3, new PokemonMeleeAttackGoal(pokemonEntity, pursuitSpeed, true));
        goalAdder.accept(pokemonEntity, 4, new PokemonRangedAttackGoal(pokemonEntity, pursuitSpeed, fleeSpeed, true));
    }

    @SubscribeEvent
    public static void onEntityJoined(EntityJoinLevelEvent event) {
        //LOGGER.info("onEntityJoined");

        if (event.getEntity() instanceof PokemonEntity) {
            PokemonEntity pokemonEntity = (PokemonEntity)event.getEntity();

            CobblemonFightOrFlight.addPokemonGoal(pokemonEntity);
            CobblemonFightOrFlightForge.addPokemonGoal(pokemonEntity);
        }
    }



    }


    //    @SubscribeEvent
//    public static void onEntityAttributes(EntityAttributeModificationEvent event){
//        LOGGER.info("onEntityAttributes");
//        event.add(CobblemonEntities.POKEMON.get(), Attributes.ATTACK_DAMAGE, 2.0D);
//        //event.add(CobblemonEntities.POKEMON.get(), Attributes.ATTACK_KNOCKBACK, 2.0D);
//    }



//    @SubscribeEvent
//    public void onUseEntity(PlayerInteractEvent.EntityInteract event) {
//        LOGGER.info("onUseEntity");
//        if (event.getTarget() instanceof PokemonEntity) {
//            Player player = event.getEntity();
//            ItemStack itemStack = player.getItemInHand(event.getHand());
//            PokemonEntity pokemonEntity = (PokemonEntity)event.getTarget();
//
//            LOGGER.info("instanceOf PokemonEntity");
//
//            if (itemStack.is(Items.BUCKET)) {
//                LOGGER.info("itemStack.is(Items.BUCKET)");
//                LOGGER.info("pokemon of species: " + pokemonEntity.getPokemon().getSpecies().getName());
//                if (pokemonEntity.getPokemon().getSpecies().getName().toLowerCase().equals("magmar")) {
//                    LOGGER.info("pokemon is Magmar");
//                    player.level.playSound(null, pokemonEntity, SoundEvents.BUCKET_FILL_LAVA, SoundSource.PLAYERS, 1.0f, 1.0f);
//                    //ItemStack itemstackLava = new ItemStack(Items.LAVA_BUCKET, 1);
//                    ////ItemStack itemstackLava2 = ItemUtils.createFilledResult(itemStack, player, itemstackLava1);
//                    ItemUtils.fi.createFilledResult(itemStack, player, itemstackLava);
//                    //if (!player.level.isClientSide) {
//                    //    CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemstackLava);
//                    //}
//                    //p_28298_.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
//                    ItemStack itemstackLava = ItemUtils.createFilledResult(itemStack, player, Items.LAVA_BUCKET.getDefaultInstance());
//                    player.setItemInHand(event.getHand(), itemstackLava);
//
//                }
//            }
//        }
//    }

