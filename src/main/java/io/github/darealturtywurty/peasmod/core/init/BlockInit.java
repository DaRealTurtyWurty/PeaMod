package io.github.darealturtywurty.peasmod.core.init;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.common.blocks.PeaPlantBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockInit {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PeasMod.MODID);

	public static final RegistryObject<PeaPlantBlock> PEA_PLANT = BLOCKS.register("pea_plant",
			() -> new PeaPlantBlock(AbstractBlock.Properties.copy(Blocks.CARROTS)));

	public static final RegistryObject<Block> PEA_BLOCK = BLOCKS.register("pea_block",
			() -> new Block(AbstractBlock.Properties.of(Material.VEGETABLE).strength(2.5f, 10f).harvestLevel(1)
					.harvestTool(ToolType.SHOVEL).requiresCorrectToolForDrops()));

	public static final RegistryObject<FlowingFluidBlock> MUSHY_PEAS = BLOCKS.register("mushy_peas",
			() -> new FlowingFluidBlock(FluidInit.MUSHY_PEAS_SOURCE,
					Properties.copy(Blocks.WATER).isViewBlocking((state, world, pos) -> true)));

	private BlockInit() {
		throw new IllegalAccessError("Attempted to construct initialization class.");
	}
}
