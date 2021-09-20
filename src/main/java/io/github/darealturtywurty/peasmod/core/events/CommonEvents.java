package io.github.darealturtywurty.peasmod.core.events;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.client.entity.PeaEntity;
import io.github.darealturtywurty.peasmod.core.config.ServerConfig;
import io.github.darealturtywurty.peasmod.core.init.FeatureInit;
import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CommonEvents {

	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.FORGE)
	public static final class ForgeEvents {
		@SubscribeEvent
		public static void biomeLoading(final BiomeLoadingEvent event) {
			event.getGeneration().getFeatures(Decoration.TOP_LAYER_MODIFICATION).add(FeatureInit.CONFIGURED_PEA_PLANT::get);
		}
	}

	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.MOD)
	public static final class ModEvents {
		@SubscribeEvent
		public static void commonSetup(final FMLCommonSetupEvent event) {
			event.enqueueWork(() -> {
				Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(PeasMod.MODID, "pea_plant"),
						FeatureInit.CONFIGURED_PEA_PLANT.get());
				DispenserBlock.registerBehavior(ItemInit.PEA.get(), new ProjectileDispenseBehavior() {
					@Override
					protected ProjectileEntity getProjectile(final World world, final IPosition position,
							final ItemStack stack) {
						return Util.make(new PeaEntity(world, position.x(), position.y(), position.z()),
								entity -> entity.setItem(stack));
					}
				});
			});
		}

		@SubscribeEvent
		public static void modConfig(final ModConfig.ModConfigEvent event) {
			final ModConfig config = event.getConfig();
			if (config.getSpec().equals(ServerConfig.SERVER_SPEC)) {
				ServerConfig.refreshServer();
			}
		}
	}

	private CommonEvents() {
		throw new IllegalAccessError("Attempted to construct event subscriber's parent class!");
	}
}
