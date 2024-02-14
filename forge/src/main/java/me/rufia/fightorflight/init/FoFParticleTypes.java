package me.rufia.fightorflight.init;

import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FoFParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CobblemonFightOrFlight.MODID);
    public static final RegistryObject<SimpleParticleType> FOF_STONE_PARTICLE = PARTICLE_TYPES.register("fof_stone_particle", () -> new SimpleParticleType(false));
}
