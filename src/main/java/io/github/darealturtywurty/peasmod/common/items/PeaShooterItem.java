package io.github.darealturtywurty.peasmod.common.items;

import java.util.function.Predicate;

import io.github.darealturtywurty.peasmod.client.entity.PeaEntity;
import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class PeaShooterItem extends ShootableItem implements IVanishable {

	public PeaShooterItem(final Properties properties) {
		super(properties);
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return stack -> stack.getItem() == ItemInit.PEA.get();
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}

	public ItemStack getProjectile(final PlayerEntity player, final ItemStack stack) {
		if (!(stack.getItem() instanceof ShootableItem))
			return ItemStack.EMPTY;
		Predicate<ItemStack> predicate = ((ShootableItem) stack.getItem()).getSupportedHeldProjectiles();
		final ItemStack itemstack = ShootableItem.getHeldProjectile(player, predicate);
		if (!itemstack.isEmpty())
			return itemstack;
		predicate = ((ShootableItem) stack.getItem()).getAllSupportedProjectiles();

		for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
			final ItemStack itemstack1 = player.inventory.getItem(i);
			if (predicate.test(itemstack1))
				return itemstack1;
		}

		return player.abilities.instabuild ? ItemInit.PEA.get().getDefaultInstance() : ItemStack.EMPTY;
	}

	@Override
	public int getUseDuration(final ItemStack stack) {
		return 360;
	}

	@Override
	public void releaseUsing(final ItemStack stack, final World world, final LivingEntity shooter, final int ticksLeft) {
		if (shooter instanceof PlayerEntity) {
			final PlayerEntity player = (PlayerEntity) shooter;
			final boolean hasPeas = player.abilities.instabuild
					|| EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
			ItemStack projectileStack = getProjectile(player, stack);

			final int releaseTicks = getUseDuration(stack) - ticksLeft;
			if (releaseTicks < 0)
				return;
			if ((!projectileStack.isEmpty() || hasPeas) && projectileStack.isEmpty()) {
				projectileStack = ItemInit.PEA.get().getDefaultInstance();
			}

			final int criticalPea = random.nextInt(20);
			if (!world.isClientSide) {
				final PeaEntity pea = new PeaEntity(world, player);
				pea.setOwner(player);
				pea.setItem(projectileStack);
				pea.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 3.0f, 1.0f);
				if (criticalPea >= 20f) {
					pea.setCritical(criticalPea);
				}

				final int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
				final int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
				final int flamingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack);

				if (powerLevel > 0) {
					pea.setBaseDamage(PeaEntity.DEFAULT_DAMAGE + powerLevel * 0.5f + 0.5f);
				}
				pea.setKnockback(punchLevel);
				pea.setSecondsOnFire(100 * flamingLevel);

				if (!player.isCreative()) {
					stack.hurtAndBreak(1, player, p -> {
						p.broadcastBreakEvent(p.getUsedItemHand());
						p.setItemInHand(p.getUsedItemHand(), ItemInit.BROKEN_PEA_SHOOTER.get().getDefaultInstance());
					});
				}

				world.addFreshEntity(pea);
			}

			world.playSound((PlayerEntity) null, player.getX(), player.getY(), player.getZ(), SoundEvents.CAT_HISS,
					SoundCategory.PLAYERS, 1f, 1f / (random.nextFloat() * 0.4F + 1.2F) * 0.5F);
			if (!projectileStack.isEmpty()) {
				projectileStack.shrink(1);
				if (projectileStack.isEmpty()) {
					player.inventory.removeItem(projectileStack);
				}
			}
			player.awardStat(Stats.ITEM_USED.get(this));
		}
	}

	@Override
	public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
		final ItemStack itemstack = player.getItemInHand(hand);
		final boolean hasProj = !player.getProjectile(itemstack).isEmpty();
		if (!player.abilities.instabuild && !hasProj)
			return ActionResult.fail(itemstack);
		player.startUsingItem(hand);
		return ActionResult.consume(itemstack);
	}
}
