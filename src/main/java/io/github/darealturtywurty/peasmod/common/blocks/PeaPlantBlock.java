package io.github.darealturtywurty.peasmod.common.blocks;

import java.util.Random;

import io.github.darealturtywurty.peasmod.core.init.ItemInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.PlantType;

public class PeaPlantBlock extends BushBlock implements IGrowable {
	public static final EnumProperty<PeaGrowthStage> STAGE = EnumProperty.create("stage", PeaGrowthStage.class);
	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] { Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D), VoxelShapes.block(), Block.box(0, 0, 0, 16, 11, 16),
			VoxelShapes.block(), VoxelShapes.block(), VoxelShapes.block(),
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D) };

	public PeaPlantBlock(final Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(final BlockState state, final IWorldReader world, final BlockPos pos) {
		boolean b = super.canSurvive(state, world, pos);
		if (state.getValue(STAGE) == PeaGrowthStage.TOP0) {
			final BlockState stateBelow = world.getBlockState(pos.below());
			b = stateBelow.getBlock() instanceof PeaPlantBlock
					&& stateBelow.getValue(STAGE).ordinal() >= PeaGrowthStage.BOTTOM3.ordinal();
		}
		return b;
	}

	@Override
	protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(STAGE);
	}

	public float getGrowthSpeed(final World world, final BlockPos pos, final float light) {
		float growth = 0.125f * (light - 11);
		if (world.canSeeSky(pos)) {
			growth += 2f;
		}

		final BlockState soil = world.getBlockState(pos.below());
		if (soil.getBlock().isFertile(soil, world, pos.below())) {
			growth *= 1.5f;
		}

		return 1f + growth;
	}

	public PeaGrowthStage getMaxStage() {
		return PeaGrowthStage.BOTTOM6;
	}

	public PeaGrowthStage getMinStage() {
		return PeaGrowthStage.BOTTOM0;
	}

	@Override
	public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world,
			final BlockPos pos, final PlayerEntity player) {
		return ItemInit.PEA.get().getDefaultInstance();
	}

	@Override
	public PlantType getPlantType(final IBlockReader world, final BlockPos pos) {
		return PlantType.CROP;
	}

	@Override
	public VoxelShape getShape(final BlockState state, final IBlockReader world, final BlockPos pos,
			final ISelectionContext context) {
		return SHAPE_BY_AGE[state.getValue(STAGE).ordinal()];
	}

	@Override
	public float getSpeedFactor() {
		return 0.75f;
	}

	@Override
	public boolean isBonemealSuccess(final World world, final Random rand, final BlockPos pos, final BlockState state) {
		return isValidBonemealTarget(world, pos, state, world.isClientSide);
	}

	@Override
	public boolean isValidBonemealTarget(final IBlockReader world, final BlockPos pos, final BlockState state,
			final boolean isClient) {
		final PeaGrowthStage stage = state.getValue(STAGE);
		if (stage != getMaxStage() && stage != PeaGrowthStage.TOP0)
			return true;
		return stage == PeaGrowthStage.BOTTOM6 && world.getBlockState(pos.above()).getBlock() != this;
	}

	@Override
	protected boolean mayPlaceOn(final BlockState state, final IBlockReader world, final BlockPos pos) {
		return state.getBlock() == Blocks.FARMLAND;
	}

	@Override
	public void onNeighborChange(final BlockState state, final IWorldReader world, final BlockPos pos,
			final BlockPos neighbor) {
		super.onNeighborChange(state, world, pos, neighbor);
		if (world instanceof World) {
			final World realWorld = (World) world;
			if (pos.getX() == neighbor.getX() && pos.getZ() == neighbor.getZ() && pos.getY() == neighbor.getY() + 1
					&& realWorld.getBlockState(neighbor).getValue(STAGE) == PeaGrowthStage.TOP0) {
				realWorld.destroyBlock(pos, true);
				realWorld.destroyBlock(pos.above(), true);
			}

			if (realWorld.getBlockState(pos).getValue(STAGE) != PeaGrowthStage.TOP0) {
				realWorld.updateNeighborsAt(pos.above(), this);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(final BlockState state, final World world, final BlockPos pos, final BlockState newState,
			final boolean isMoving) {
		super.onRemove(state, world, pos, newState, isMoving);
		if (state.getValue(STAGE) == PeaGrowthStage.TOP0) {
			world.destroyBlock(pos.below(), true);
		}
	}

	@Override
	public void performBonemeal(final ServerWorld world, final Random rand, final BlockPos pos, final BlockState state) {
		PeaGrowthStage stage = state.getValue(STAGE);
		if (stage != PeaGrowthStage.TOP0 && stage != PeaGrowthStage.BOTTOM6) {
			final int span = getMaxStage().ordinal() - stage.ordinal();
			final int growBy = this.RANDOM.nextInt(span) + 1;
			PeaGrowthStage newStage = stage;
			for (int stageAdd = 0; stageAdd < growBy; stageAdd++) {
				newStage = newStage.next();
			}

			world.setBlockAndUpdate(pos, state.setValue(STAGE, newStage));
			stage = newStage;
		}

		if (world.isEmptyBlock(pos.above()) && stage.ordinal() >= PeaGrowthStage.BOTTOM3.ordinal()
				&& stage != PeaGrowthStage.TOP0) {
			world.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(STAGE, PeaGrowthStage.TOP0));
		}
	}

	@Override
	public void randomTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
		final int light = world.getMaxLocalRawBrightness(pos);
		if (light >= 12) {
			final PeaGrowthStage stage = state.getValue(STAGE);
			final float speed = getGrowthSpeed(world, pos, light);
			if (random.nextInt((int) (50f / speed) + 1) == 0) {
				world.setBlockAndUpdate(pos, state.setValue(STAGE, stage.next()));

				if (stage.ordinal() >= PeaGrowthStage.BOTTOM3.ordinal() && world.isEmptyBlock(pos.above())
						&& stage != PeaGrowthStage.TOP0) {
					world.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(STAGE, PeaGrowthStage.TOP0));
				}
			}
		}
	}
}
