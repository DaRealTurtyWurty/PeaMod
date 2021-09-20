package io.github.darealturtywurty.peasmod.client.entity;

import io.github.darealturtywurty.peasmod.core.init.EntityInit;
import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class PeaEntity extends ProjectileItemEntity {

	public static final float DEFAULT_DAMAGE = 3f;
	private float damage = DEFAULT_DAMAGE;
	private float critical = 0f;
	private int knockback = 0;

	public PeaEntity(final EntityType<? extends PeaEntity> type, final World world) {
		super(type, world);
	}

	public PeaEntity(final World world, final double x, final double y, final double z) {
		super(EntityInit.PEA.get(), x, y, z, world);
	}

	public PeaEntity(final World world, final LivingEntity entity) {
		super(EntityInit.PEA.get(), entity, world);
	}

	@Override
	public boolean equals(final Object object) {
		return super.equals(object) && object instanceof PeaEntity && ((PeaEntity) object).damage == this.damage
				&& ((PeaEntity) object).critical == this.critical && ((PeaEntity) object).knockback == this.knockback;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public float getCritical() {
		return this.critical;
	}

	public float getDamage() {
		return this.damage;
	}

	@Override
	protected Item getDefaultItem() {
		return ItemInit.PEA.get();
	}

	public int getKnockback() {
		return this.knockback;
	}

	private IParticleData getParticle() {
		return new ItemParticleData(ParticleTypes.ITEM, getItem());
	}

	@Override
	public void handleEntityEvent(final byte id) {
		if (id == 3) {
			final IParticleData particleData = getParticle();

			for (int i = 0; i < 8; ++i) {
				this.level.addParticle(particleData, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	protected void onHit(final RayTraceResult result) {
		super.onHit(result);
		if (!this.level.isClientSide) {
			this.level.broadcastEntityEvent(this, (byte) 3);
			this.remove();
		}
	}

	@Override
	protected void onHitEntity(final EntityRayTraceResult entity) {
		super.onHitEntity(entity);
		entity.getEntity().hurt(DamageSource.thrown(this, getOwner()), this.damage + this.critical);
		if (this.knockback > 0) {
			final Vector3d vector3d = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(this.knockback * 0.6D);
			if (vector3d.lengthSqr() > 0.0D) {
				entity.getEntity().push(vector3d.x, 0.1D, vector3d.z);
			}
		}
	}

	public void setBaseDamage(final float value) {
		if (value < 0f) {
			this.damage = 0f;
			return;
		}
		this.damage = value;
	}

	public void setCritical(final float value) {
		if (value < 0f) {
			this.critical = 0f;
			return;
		}
		this.critical = value;
	}

	public void setKnockback(final int value) {
		if (value < 0) {
			this.knockback = value;
			return;
		}
		this.knockback = value;
	}
}
