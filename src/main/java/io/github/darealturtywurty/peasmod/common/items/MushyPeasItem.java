package io.github.darealturtywurty.peasmod.common.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.World;

public class MushyPeasItem extends Item {

	public MushyPeasItem(final Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(final ItemStack stack, final World world, final LivingEntity living) {
		if (!world.isClientSide() && living instanceof PlayerEntity) {
			final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) living;
			final Stat<?> thisStat = Stats.ITEM_USED.get(this);
			serverPlayer.awardStat(thisStat);
			if (serverPlayer.getStats().getValue(thisStat) % 5 == 0) {
				serverPlayer.addEffect(new EffectInstance(Effects.CONFUSION, 120, 5));
				serverPlayer.addEffect(new EffectInstance(Effects.REGENERATION, 90, 2));
			}
		}
		return super.finishUsingItem(stack, world, living);
	}

	@Override
	public int getUseDuration(final ItemStack stack) {
		return 48;
	}
}
