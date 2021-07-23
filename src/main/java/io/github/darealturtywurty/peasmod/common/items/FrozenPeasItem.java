package io.github.darealturtywurty.peasmod.common.items;

import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class FrozenPeasItem extends Item {

	private static void spawnItemParticles(final ItemStack stack, final LivingEntity living, final int amount) {
		for (int i = 0; i < amount; ++i) {
			Vector3d vector3d = new Vector3d((random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
			vector3d = vector3d.xRot(-living.xRot * ((float) Math.PI / 180F));
			vector3d = vector3d.yRot(-living.yRot * ((float) Math.PI / 180F));
			final double d0 = -random.nextFloat() * 0.6D - 0.3D;
			Vector3d vector3d1 = new Vector3d((random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
			vector3d1 = vector3d1.xRot(-living.xRot * ((float) Math.PI / 180F));
			vector3d1 = vector3d1.yRot(-living.yRot * ((float) Math.PI / 180F));
			vector3d1 = vector3d1.add(living.getX(), living.getEyeY(), living.getZ());
			if (living.level instanceof ServerWorld) {
				((ServerWorld) living.level).sendParticles(new ItemParticleData(ParticleTypes.ITEM, stack), vector3d1.x,
						vector3d1.y, vector3d1.z, 1, vector3d.x, vector3d.y + 0.05D, vector3d.z, 0.0D);
			} else {
				living.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vector3d1.x, vector3d1.y,
						vector3d1.z, vector3d.x, vector3d.y + 0.05D, vector3d.z);
			}
		}
	}

	public FrozenPeasItem(final Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
		if (player.isHurt()) {
			player.setHealth(player.getHealth() + player.getMaxHealth() / 2f);
			final ItemStack stack = player.getMainHandItem();
			player.getMainHandItem().hurtAndBreak(1, player, plr -> {
				if (!plr.isSilent() && world.isClientSide()) {
					plr.level.playLocalSound(plr.getX(), plr.getY(), plr.getZ(), SoundEvents.EGG_THROW, plr.getSoundSource(),
							0.8F, 0.8F + plr.level.random.nextFloat() * 0.4F, false);
				}
				spawnItemParticles(stack, plr, 5);
				plr.setItemInHand(hand, new ItemStack(ItemInit.PEA.get(), 8));
				System.out.println("im here!");
			});
			return ActionResult.success(stack);
		}
		return super.use(world, player, hand);
	}
}
