package io.github.darealturtywurty.peasmod.common.portal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PeaPortalBlock extends Block {

    private static final ResourceLocation PEA_DIM_RES = new ResourceLocation(PeasMod.MODID, "pea_dim");
    private static final VoxelShape SHAPE = VoxelShapes.box(0F, 0F, 0F, 1F, 0.8125F, 1F);
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 64;

    public PeaPortalBlock(final Properties properties) {
        super(properties);
    }

    public static RegistryKey<World> getDestination(final Entity entity) {
        final RegistryKey<World> peaDim = RegistryKey.create(Registry.DIMENSION_REGISTRY, PEA_DIM_RES);
        return !entity.level.dimension().location().equals(peaDim.location()) ? peaDim : World.OVERWORLD;
    }

    public static void tryTeleportEntity(final Entity entity) {
        if (!entity.isAlive() || entity.level.isClientSide)
            return;
        if (entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions())
            return;
        if (entity.isOnPortalCooldown())
            return;

        // entity.portalTime = 10;
        final RegistryKey<World> dest = getDestination(entity);
        final ServerWorld serverWorld = entity.level.getServer().getLevel(dest);
        if (serverWorld == null)
            return;

        entity.changeDimension(serverWorld, new PeaTeleporter(200));
        if (dest == RegistryKey.create(Registry.DIMENSION_REGISTRY, PEA_DIM_RES)
                && entity instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
            serverPlayer.setRespawnPosition(dest, serverPlayer.blockPosition(), serverPlayer.yRot, true,
                    false);
        }
    }

    public static boolean validatePortal(final World world, final BlockPos pos,
            final Map<BlockPos, Boolean> blockValidation, final AtomicInteger size, final BlockState state) {
        if (size.incrementAndGet() > MAX_SIZE)
            return false;

        boolean isEnclosed = true;

        for (int posIndex = 0; posIndex < Plane.values().length && size.get() < MAX_SIZE; posIndex++) {
            final BlockPos checkPos = pos.relative(Direction.from2DDataValue(posIndex));
            if (!blockValidation.containsKey(checkPos)) {
                final BlockState checkState = world.getBlockState(checkPos);
                if (checkState == state && world.getBlockState(checkPos.below()).canOcclude()) {
                    blockValidation.put(checkPos, true);
                    if (isEnclosed) {
                        isEnclosed = validatePortal(world, checkPos, blockValidation, size, state);
                    }
                } else if (state.getBlock() == BlockInit.PEA_BLOCK.get()) {
                    blockValidation.put(checkPos, false);
                } else
                    return false;
            }
        }
        return isEnclosed;
    }

    @Override
    public void entityInside(final BlockState state, final World world, final BlockPos pos,
            final Entity entity) {
        if (state == defaultBlockState()) {
            tryTeleportEntity(entity);
        }
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader world, final BlockPos pos,
            final ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos,
            final Block block, final BlockPos fromPos, final boolean isMoving) {
        boolean allowed = world.getBlockState(pos.below()).canOcclude();
        for (final Direction offset : Direction.Plane.HORIZONTAL) {
            if (!allowed) {
                break;
            }
            final BlockState neighbourState = world.getBlockState(pos.relative(offset));
            allowed = neighbourState.getBlock() == this || neighbourState == state;
        }

        if (!allowed) {
            world.setBlockAndUpdate(pos, defaultBlockState());
        }
    }

    public boolean tryMakePortal(final World world, final BlockPos pos, final ItemEntity thrown) {
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() == this && world.getBlockState(pos.below()).canOcclude()) {
            final Map<BlockPos, Boolean> blockValidation = new HashMap<>();
            blockValidation.put(pos, true);
            final AtomicInteger size = new AtomicInteger();
            if (validatePortal(world, pos, blockValidation, size, state) && size.get() >= MIN_SIZE) {
                thrown.getItem().shrink(1);
                // some cool effect
                for (final Entry<BlockPos, Boolean> entry : blockValidation.entrySet()) {
                    if (entry.getValue()) {
                        world.setBlockAndUpdate(entry.getKey(), defaultBlockState());
                    }
                }
                return true;
            }
        }
        return false;
    }
}
