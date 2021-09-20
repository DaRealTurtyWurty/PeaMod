package io.github.darealturtywurty.peasmod.common.fluids;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class MushyPeasFluid extends ForgeFlowingFluid {

	public static class Flowing extends ForgeFlowingFluid.Flowing {

		public Flowing(final Properties properties) {
			super(properties);
		}

		@Override
		protected boolean canBeReplacedWith(final FluidState state, final IBlockReader world, final BlockPos pos,
				final Fluid fluidIn, final Direction direction) {
			return false;
		}
	}

	public static class Source extends ForgeFlowingFluid.Source {

		public Source(final Properties properties) {
			super(properties);
		}

		@Override
		protected boolean canBeReplacedWith(final FluidState state, final IBlockReader world, final BlockPos pos,
				final Fluid fluidIn, final Direction direction) {
			return false;
		}
	}

	protected MushyPeasFluid(final Properties properties) {
		super(properties);
	}

	@Override
	public int getAmount(final FluidState state) {
		return state.getType() instanceof Source || state.getType() instanceof Flowing ? state.getAmount() : 0;
	}

	@Override
	public boolean isSource(final FluidState state) {
		return state.getType() instanceof Source;
	}
}
