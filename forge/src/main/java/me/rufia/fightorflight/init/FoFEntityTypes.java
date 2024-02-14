package me.rufia.fightorflight.init;

import me.rufia.fightorflight.CobblemonFightOrFlight;
import me.rufia.fightorflight.projectile.FoFStoneProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FoFEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CobblemonFightOrFlight.MODID);
    public static final RegistryObject<EntityType<FoFStoneProjectile>> FOF_STONE_PROJECTILE = ENTITY_TYPES.register("fof_stone_projectile", () -> EntityType.Builder.of((EntityType.EntityFactory<FoFStoneProjectile>) FoFStoneProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).build("fof_stone_projectile"));


}
