package io.github.darealturtywurty.peasmod.core.init;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.common.fluids.MushyPeasFluid;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Rarity;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class FluidInit {

	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, PeasMod.MODID);

	public static final ResourceLocation RES_LOCATION = new ResourceLocation(PeasMod.MODID, "blocks/mushy_peas");

	public static final RegistryObject<FlowingFluid> MUSHY_PEAS_SOURCE = FLUIDS.register("mushy_peas_source",
			() -> new MushyPeasFluid.Source(FluidInit.PROPERTIES));

	public static final RegistryObject<FlowingFluid> MUSHY_PEAS_FLOWING = FLUIDS.register("mushy_peas_flowing",
			() -> new MushyPeasFluid.Flowing(FluidInit.PROPERTIES));

	private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(MUSHY_PEAS_SOURCE,
			MUSHY_PEAS_FLOWING,
			FluidAttributes.builder(RES_LOCATION, RES_LOCATION).density(10000).rarity(Rarity.EPIC)
					.sound(SoundEvents.AMBIENT_UNDERWATER_ENTER).overlay(RES_LOCATION).luminosity(1).viscosity(10000))
							.block(BlockInit.MUSHY_PEAS).bucket(ItemInit.BUCKET_MUSHY_PEAS).explosionResistance(5.0f)
							.levelDecreasePerBlock(2);

	public static final ITag.INamedTag<Fluid> MUSHY_PEAS_TAG = FluidTags
			.createOptional(new ResourceLocation(PeasMod.MODID, "mushy_peas"));

	private FluidInit() {
		throw new IllegalAccessError("Attempted to construct initialization class.");
	}
}
