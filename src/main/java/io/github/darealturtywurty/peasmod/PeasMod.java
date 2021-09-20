package io.github.darealturtywurty.peasmod;

import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import io.github.darealturtywurty.peasmod.core.init.EntityInit;
import io.github.darealturtywurty.peasmod.core.init.FeatureInit;
import io.github.darealturtywurty.peasmod.core.init.FluidInit;
import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PeasMod.MODID)
public class PeasMod {

	public static final String MODID = "peasmod";

	public static final ItemGroup TAB = new ItemGroup("peasmod") {

		@Override
		public ItemStack makeIcon() {
			return ItemInit.PEA.get().getDefaultInstance();
		}
	};

	public PeasMod() {
		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		ItemInit.ITEMS.register(bus);
		BlockInit.BLOCKS.register(bus);
		FeatureInit.FEATURES.register(bus);
		EntityInit.ENTITIES.register(bus);
		FluidInit.FLUIDS.register(bus);
	}
}
