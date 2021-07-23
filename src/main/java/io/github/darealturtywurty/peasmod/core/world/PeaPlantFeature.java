package io.github.darealturtywurty.peasmod.core.world;

import java.util.Random;

import com.mojang.serialization.Codec;

import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.util.Constants.BlockFlags;

public class PeaPlantFeature extends Feature<BlockClusterFeatureConfig> {

	private static boolean isNearWater(final IWorldReader world, final BlockPos pos) {
		for (final BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
			if (world.getFluidState(blockpos).is(FluidTags.WATER))
				return true;
		}

		return FarmlandWaterManager.hasBlockWaterTicket(world, pos);
	}

	public PeaPlantFeature(final Codec<BlockClusterFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(final ISeedReader world, final ChunkGenerator chunkGenerator, final Random rand, final BlockPos pos,
			final BlockClusterFeatureConfig config) {
		if (rand.nextInt(5) == 0) {
			BlockPos blockpos = null;
			if (config.project) {
				blockpos = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE_WG, pos);
			} else {
				blockpos = pos;
			}

			int quantity = 0;
			final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

			for (int j = 0; j < config.tries; ++j) {
				mutablePos.setWithOffset(blockpos, rand.nextInt(config.xspread + 1) - rand.nextInt(config.xspread + 1),
						rand.nextInt(config.yspread + 1) - rand.nextInt(config.yspread + 1),
						rand.nextInt(config.zspread + 1) - rand.nextInt(config.zspread + 1));
				final BlockPos cropPos = mutablePos;
				final BlockPos belowPos = mutablePos.below();
				final BlockState crop = world.getBlockState(cropPos);
				final BlockState below = world.getBlockState(belowPos);
				if (!crop.getMaterial().isSolid() && crop.getFluidState().isEmpty() && below.is(Blocks.DIRT)
						&& isNearWater(world, belowPos)) {
					world.setBlock(belowPos, net.minecraft.block.Blocks.FARMLAND.defaultBlockState(),
							BlockFlags.BLOCK_UPDATE);
					world.setBlock(cropPos, BlockInit.PEA_PLANT.get().defaultBlockState(), BlockFlags.BLOCK_UPDATE);
					++quantity;
				}
			}

			return quantity > 0;
		}
		return false;
	}
}
