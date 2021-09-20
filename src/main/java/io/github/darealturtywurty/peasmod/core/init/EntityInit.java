package io.github.darealturtywurty.peasmod.core.init;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.client.entity.PeaEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class EntityInit {

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES,
			PeasMod.MODID);

	public static final RegistryObject<EntityType<PeaEntity>> PEA = ENTITIES.register("pea",
			() -> EntityType.Builder.<PeaEntity>of(PeaEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F)
					.clientTrackingRange(4).updateInterval(10).build(new ResourceLocation(PeasMod.MODID, "pea").toString()));

	private EntityInit() {
		throw new IllegalAccessError("Attempted to construct initialization class.");
	}
}
