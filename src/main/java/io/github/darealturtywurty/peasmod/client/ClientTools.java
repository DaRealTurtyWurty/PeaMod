package io.github.darealturtywurty.peasmod.client;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientTools {

	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
	public static final class ForgeEvents {
		private ForgeEvents() {
			throw new IllegalAccessError("Attempted to construct event subscriber class!");
		}
	}

	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	public static final class ModEvents {

		@SubscribeEvent
		public static void clientSetup(final FMLClientSetupEvent event) {
			RenderTypeLookup.setRenderLayer(BlockInit.PEA_PLANT.get(), RenderType.cutout());
		}

		private ModEvents() {
			throw new IllegalAccessError("Attempted to construct event subscriber class!");
		}
	}

	private ClientTools() {
		throw new IllegalAccessError("Attempted to construct event subscriber's parent class!");
	}
}
