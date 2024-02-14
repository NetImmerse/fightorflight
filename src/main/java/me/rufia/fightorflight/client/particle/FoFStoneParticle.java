package me.rufia.fightorflight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoFStoneParticle extends BreakingItemParticle {
    public FoFStoneParticle(ClientLevel level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, new ItemStack(Items.COBBLESTONE));
    }
}