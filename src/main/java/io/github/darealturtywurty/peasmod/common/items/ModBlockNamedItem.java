package io.github.darealturtywurty.peasmod.common.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraftforge.fml.RegistryObject;

public class ModBlockNamedItem extends BlockItem {

	private final RegistryObject<? extends Block> block;

	public ModBlockNamedItem(final RegistryObject<? extends Block> block, final Properties properties) {
		super(block.get(), properties);
		this.block = block;
	}

	@Override
	public Block getBlock() {
		return this.block.get();
	}

	@Override
	public String getDescriptionId() {
		return getOrCreateDescriptionId();
	}
}
