package io.github.darealturtywurty.peasmod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import io.github.darealturtywurty.peasmod.core.init.EntityInit;
import io.github.darealturtywurty.peasmod.core.init.FluidInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientTools {

	@SuppressWarnings({ "resource", "deprecation" })
	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
	public static final class ForgeEvents {

		private static final ResourceLocation MUSHY_PEAS_LOC = new ResourceLocation(PeasMod.MODID,
				"textures/blocks/mushy_peas.png");

		@SubscribeEvent
		public static void fogColors(final EntityViewRenderEvent.FogColors event) {
			if (event.getInfo().getFluidInCamera().is(FluidInit.MUSHY_PEAS_TAG)) {
				event.setRed(0.294117647f);
				event.setGreen(0.380392157f);
				event.setBlue(0.149019608f);
			}
		}

		@SubscribeEvent
		public static void fogDensity(final EntityViewRenderEvent.FogDensity event) {
			if (event.getInfo().getFluidInCamera().is(FluidInit.MUSHY_PEAS_TAG)) {
				event.setDensity(1f);
				event.setCanceled(true);
			}
		}

		@SubscribeEvent
		public static void renderOverlay(final RenderGameOverlayEvent.Pre event) {
			if (event.getType() == ElementType.ALL) {
				final PlayerEntity player = Minecraft.getInstance().player;
				final BlockPos position = player.blockPosition();
				final FluidState fluidState = player.level.getFluidState(position.above());
				final double aboveEyes = player.getEyeY() - 0.11111111D;
				final double eyePos = position.getY() + fluidState.getHeight(player.level, position);
				if (aboveEyes > eyePos && fluidState.is(FluidInit.MUSHY_PEAS_TAG)) {
					Minecraft.getInstance().textureManager.bind(MUSHY_PEAS_LOC);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.disableAlphaTest();
					draw(0, 0, 0, 0, event.getWindow().getWidth(), event.getWindow().getHeight(), 255, 255, 255, 200);
					RenderSystem.disableBlend();
					RenderSystem.enableAlphaTest();
				}
			}
		}

		private ForgeEvents() {
			throw new IllegalAccessError("Attempted to construct event subscriber class!");
		}
	}

	@EventBusSubscriber(modid = PeasMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	public static final class ModEvents {
		@SubscribeEvent
		public static void clientSetup(final FMLClientSetupEvent event) {
			RenderTypeLookup.setRenderLayer(BlockInit.PEA_PLANT.get(), RenderType.cutout());
			RenderingRegistry.registerEntityRenderingHandler(EntityInit.PEA.get(),
					factory -> new SpriteRenderer<>(factory, Minecraft.getInstance().getItemRenderer()));
		}

		private ModEvents() {
			throw new IllegalAccessError("Attempted to construct event subscriber class!");
		}
	}

	@SuppressWarnings("deprecation")
	public static void draw(final int posX, final int posY, final int texU, final int texV, final int width,
			final int height, final int red, final int green, final int blue, final int alpha) {
		final BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.vertex(posX, posY + height, 0.0D).uv(texU * 0.00390625F, (texV + height) * 0.00390625F)
				.color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(posX + width, posY + height, 0.0D)
				.uv((texU + width) * 0.00390625F, (texV + height) * 0.00390625F).color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(posX + width, posY, 0.0D).uv((texU + width) * 0.00390625F, texV * 0.00390625F)
				.color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(posX, posY, 0.0D).uv(texU * 0.00390625F, texV * 0.00390625F).color(red, green, blue, alpha)
				.endVertex();
		Tessellator.getInstance().end();
	}

	private ClientTools() {
		throw new IllegalAccessError("Attempted to construct event subscriber's parent class!");
	}
}
