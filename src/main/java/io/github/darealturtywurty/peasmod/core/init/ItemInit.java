package io.github.darealturtywurty.peasmod.core.init;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.common.items.FrozenPeasItem;
import io.github.darealturtywurty.peasmod.common.items.ModBlockNamedItem;
import io.github.darealturtywurty.peasmod.common.items.MushyPeasItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemInit {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PeasMod.MODID);

	public static final RegistryObject<Item> PEA = ITEMS.register("pea",
			() -> new ModBlockNamedItem(BlockInit.PEA_PLANT, new Item.Properties().tab(PeasMod.TAB)
					.food(new Food.Builder().nutrition(1).saturationMod(0.2f).fast().build())));

	public static final RegistryObject<Item> PEA_POD = ITEMS.register("pea_pod",
			() -> new Item(new Item.Properties().tab(PeasMod.TAB)));

	public static final RegistryObject<Item> FROZEN_PEAS = ITEMS.register("frozen_peas",
			() -> new FrozenPeasItem(new Item.Properties().tab(PeasMod.TAB).durability(5)));

	public static final RegistryObject<Item> MUSHY_PEAS = ITEMS.register("mushy_peas", () -> new MushyPeasItem(
			new Item.Properties().tab(PeasMod.TAB).food(new Food.Builder().nutrition(8).saturationMod(1.8f).build())));

	private ItemInit() {
		throw new IllegalAccessError("Attempted to construct initialization class.");
	}
}
