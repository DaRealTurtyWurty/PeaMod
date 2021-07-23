package io.github.darealturtywurty.peasmod.core.init;

import java.util.function.Supplier;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.core.world.PeaPlantFeature;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class FeatureInit {

	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES,
			PeasMod.MODID);

	public static final RegistryObject<Feature<BlockClusterFeatureConfig>> PEA_PLANT = FEATURES.register("pea_plant",
			() -> new PeaPlantFeature(BlockClusterFeatureConfig.CODEC));

	public static final Supplier<ConfiguredFeature<?, ?>> CONFIGURED_PEA_PLANT = () -> PEA_PLANT.get()
			.configured(new BlockClusterFeatureConfig.Builder(
					new SimpleBlockStateProvider(BlockInit.PEA_PLANT.get().defaultBlockState()), new SimpleBlockPlacer())
							.build())
			.range(256).squared().count(1);

	private FeatureInit() {
		throw new IllegalAccessError("Attempted to construct initialization class.");
	}
}
