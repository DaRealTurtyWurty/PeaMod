package io.github.darealturtywurty.peasmod.common.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import io.github.darealturtywurty.peasmod.PeasMod;
import io.github.darealturtywurty.peasmod.core.init.BlockInit;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PortalInfo;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.ITeleporter;

public class PeaTeleporter implements ITeleporter {

    private static final Map<ResourceLocation, Map<ColumnPos, PortalPosition>> DEST_COORD_MAP = new HashMap<>();

    private static final Object2LongMap<ColumnPos> COLUMN_MAP = new Object2LongOpenHashMap<>();
    private final int scanRadius;

    public PeaTeleporter(final int scanRadius) {
        this.scanRadius = scanRadius;
    }

    public static boolean isSafeAround(World world, BlockPos pos, Entity entity) {
        if (!isSafe(world, pos, entity))
            return false;

        for (final Direction facing : Direction.Plane.HORIZONTAL) {
            if (!isSafe(world, pos.relative(facing, 16), entity))
                return false;
        }

        return true;
    }

    private static void cachePortalCoords(ServerWorld world, Vector3d loc, BlockPos pos) {
        final int x = MathHelper.floor(loc.x), z = MathHelper.floor(loc.z);
        DEST_COORD_MAP.putIfAbsent(world.dimension().location(), Maps.newHashMapWithExpectedSize(4096));
        DEST_COORD_MAP.get(world.dimension().location()).put(new ColumnPos(x, z),
                new PortalPosition(pos, world.getGameTime()));
    }

    private static void checkAdjacent(ServerWorld world, BlockPos pos, Set<BlockPos> checked,
            Set<BlockPos> result) {
        for (final Direction facing : Direction.Plane.HORIZONTAL) {
            final BlockPos offset = pos.relative(facing);
            if (!checked.add(offset)) {
                continue;
            }
            if (isPortalAt(world, offset)) {
                checkAdjacent(world, offset, checked, result);
            } else {
                result.add(offset);
            }
        }
    }

    private static boolean checkPos(World world, BlockPos pos) {
        return world.getWorldBorder().isWithinBounds(pos);
    }

    @Nullable
    private static BlockPos findPortalCoords(ServerWorld world, Vector3d loc, Predicate<BlockPos> predicate) {
        // adjust the height based on what world we're traveling to
        final double yFactor = getYFactor(world);
        // modified copy of base Teleporter method:
        final int entityX = MathHelper.floor(loc.x);
        final int entityZ = MathHelper.floor(loc.z);

        final BlockPos.Mutable pos = new BlockPos.Mutable();

        double spotWeight = -1D;
        BlockPos spot = null;

        final int range = 16;
        for (int rx = entityX - range; rx <= entityX + range; rx++) {
            final double xWeight = rx + 0.5D - loc.x;
            for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
                final double zWeight = rz + 0.5D - loc.z;

                for (int ry = getScanHeight(world, rx, rz); ry >= 0; ry--) {

                    if (!world.isEmptyBlock(pos.set(rx, ry, rz))) {
                        continue;
                    }

                    while (ry > 0 && world.isEmptyBlock(pos.set(rx, ry - 1, rz))) {
                        ry--;
                    }

                    final double yWeight = ry + 0.5D - loc.y * yFactor;
                    final double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;

                    // check from the "in ground" pos
                    if ((spotWeight < 0.0D || rPosWeight < spotWeight) && predicate.test(pos)) {
                        spotWeight = rPosWeight;
                        spot = pos.immutable();
                    }
                }
            }
        }

        return spot;
    }

    @Nullable
    private static BlockPos findSafeCoords(ServerWorld world, int range, BlockPos pos, Entity entity) {
        final int attempts = range / 8;
        for (int x = 0; x < attempts; x++) {
            for (int z = 0; z < attempts; z++) {
                final BlockPos dPos = new BlockPos(pos.getX() + x * attempts - range / 2, 100,
                        pos.getZ() + z * attempts - range / 2);

                if (isSafeAround(world, dPos, entity))
                    return dPos;
            }
        }
        return null;
    }

    // from the start point, builds a set of all directly adjacent non-portal blocks
    private static Set<BlockPos> getBoundaryPositions(ServerWorld world, BlockPos start) {
        final Set<BlockPos> result = new HashSet<>(), checked = new HashSet<>();
        checked.add(start);
        checkAdjacent(world, start, checked, result);
        return result;
    }

    private static int getScanHeight(ServerWorld world, BlockPos pos) {
        return getScanHeight(world, pos.getX(), pos.getZ());
    }

    private static int getScanHeight(ServerWorld world, int x, int z) {
        final int worldHeight = world.getHeight() - 1;
        final int chunkHeight = world.getChunk(x >> 4, z >> 4).getHighestSectionPosition() + 15;
        return Math.min(worldHeight, chunkHeight);
    }

    private static double getYFactor(ServerWorld world) {
        return world.dimension().location().equals(World.OVERWORLD.location()) ? 2.0 : 0.5;
    }

    private static boolean isIdealForPortal(ServerWorld world, BlockPos pos) {
        for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
            for (int potentialX = 0; potentialX < 4; potentialX++) {
                for (int potentialY = 0; potentialY < 4; potentialY++) {
                    final BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
                    final Material material = world.getBlockState(tPos).getMaterial();
                    if (potentialY == 0 && material != Material.PLANT
                            || potentialY >= 1 && !material.isReplaceable())
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean isOkayForPortal(ServerWorld world, BlockPos pos) {
        for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
            for (int potentialX = 0; potentialX < 4; potentialX++) {
                for (int potentialY = 0; potentialY < 4; potentialY++) {
                    final BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
                    final Material material = world.getBlockState(tPos).getMaterial();
                    if (potentialY == 0 && !material.isSolid() && !material.isLiquid()
                            || potentialY >= 1 && !material.isReplaceable())
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean isPortal(BlockState state) {
        return state.getBlock() == BlockInit.MUSHY_PEAS.get();
    }

    private static boolean isPortalAt(ServerWorld world, BlockPos pos) {
        return isPortal(world.getBlockState(pos));
    }

    private static boolean isSafe(World world, BlockPos pos, Entity entity) {
        return checkPos(world, pos);
    }

    private static void loadSurroundingArea(ServerWorld world, Vector3d pos) {

        final int x = MathHelper.floor(pos.x) >> 4;
        final int z = MathHelper.floor(pos.y) >> 4;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getChunk(x + dx, z + dz);
            }
        }
    }

    private static void makePortal(Entity entity, ServerWorld world, Vector3d pos) {
        // ensure area is populated first
        loadSurroundingArea(world, pos);

        BlockPos spot = findPortalCoords(world, pos, blockPos -> isPortalAt(world, blockPos));
        final String name = entity.getName().getString();

        if (spot != null) {
            PeasMod.LOGGER.debug("Found existing portal for {} at {}", name, spot);
            cachePortalCoords(world, pos, spot);
            return;
        }

        spot = findPortalCoords(world, pos, blockpos -> isIdealForPortal(world, blockpos));

        if (spot != null) {
            PeasMod.LOGGER.debug("Found ideal portal spot for {} at {}", name, spot);
            cachePortalCoords(world, pos, makePortalAt(world, spot));
            return;
        }

        PeasMod.LOGGER.debug("Did not find ideal portal spot, shooting for okay one for {}", name);
        spot = findPortalCoords(world, pos, blockPos -> isOkayForPortal(world, blockPos));

        if (spot != null) {
            PeasMod.LOGGER.debug("Found okay portal spot for {} at {}", name, spot);
            cachePortalCoords(world, pos, makePortalAt(world, spot));
            return;
        }

        // well I don't think we can actually just return and fail here
        PeasMod.LOGGER.debug("Did not even find an okay portal spot, just making a random one for {}", name);

        // adjust the portal height based on what world we're traveling to
        final double yFactor = getYFactor(world);
        // modified copy of base Teleporter method:
        cachePortalCoords(world, pos, makePortalAt(world,
                new BlockPos(entity.getX(), entity.getY() * yFactor - 1.0, entity.getZ())));
    }

    private static BlockPos makePortalAt(World world, BlockPos pos) {
        if (pos.getY() < 30) {
            pos = new BlockPos(pos.getX(), 30, pos.getZ());
        } else if (pos.getY() > 128 - 10) {
            pos = new BlockPos(pos.getX(), 128 - 10, pos.getZ());
        }

        // grass all around it
        final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        world.setBlockAndUpdate(pos.west().north(), grass);
        world.setBlockAndUpdate(pos.north(), grass);
        world.setBlockAndUpdate(pos.east().north(), grass);
        world.setBlockAndUpdate(pos.east(2).north(), grass);

        world.setBlockAndUpdate(pos.west(), grass);
        world.setBlockAndUpdate(pos.east(2), grass);

        world.setBlockAndUpdate(pos.west().south(), grass);
        world.setBlockAndUpdate(pos.east(2).south(), grass);

        world.setBlockAndUpdate(pos.west().south(2), grass);
        world.setBlockAndUpdate(pos.south(2), grass);
        world.setBlockAndUpdate(pos.east().south(2), grass);
        world.setBlockAndUpdate(pos.east(2).south(2), grass);

        // dirt under it
        final BlockState dirt = Blocks.DIRT.defaultBlockState();

        world.setBlockAndUpdate(pos.below(), dirt);
        world.setBlockAndUpdate(pos.east().below(), dirt);
        world.setBlockAndUpdate(pos.south().below(), dirt);
        world.setBlockAndUpdate(pos.east().south().below(), dirt);

        // portal in it
        final BlockState portal = BlockInit.MUSHY_PEAS.get().defaultBlockState()
                .with(PeaPortalBlock.DISALLOW_RETURN, false);

        world.setBlockAndUpdate(pos, portal);
        world.setBlockAndUpdate(pos.east(), portal);
        world.setBlockAndUpdate(pos.south(), portal);
        world.setBlockAndUpdate(pos.east().south(), portal);

        // meh, let's just make a bunch of air over it for 4 squares
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    world.removeBlock(pos.offset(dx, dy, dz), false);
                }
            }
        }

        // finally, "nature decorations"!
        world.setBlockAndUpdate(pos.west().north().above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.north().above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east().north().above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east(2).north().above(), randNatureBlock(world.random));

        world.setBlockAndUpdate(pos.west().above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east(2).above(), randNatureBlock(world.random));

        world.setBlockAndUpdate(pos.west().south().above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east(2).south().above(), randNatureBlock(world.random));

        world.setBlockAndUpdate(pos.west().south(2).above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.south(2).above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east().south(2).above(), randNatureBlock(world.random));
        world.setBlockAndUpdate(pos.east(2).south(2).above(), randNatureBlock(world.random));

        return pos;
    }

    private static PortalInfo makePortalInfo(Entity entity, double x, double y, double z) {
        return makePortalInfo(entity, new Vector3d(x, y, z));
    }

    private static PortalInfo makePortalInfo(Entity entity, Vector3d pos) {
        return new PortalInfo(pos, Vector3d.ZERO, entity.yRot, entity.xRot);
    }

    private static PortalInfo moveToSafeCoords(ServerWorld world, Entity entity) {
        final BlockPos pos = entity.blockPosition();
        if (isSafeAround(world, pos, entity)) {
            PeasMod.LOGGER.debug("Portal destination looks safe!");
            return makePortalInfo(entity, entity.position());
        }

        PeasMod.LOGGER.debug("Portal destination looks unsafe, rerouting!");

        BlockPos safeCoords = findSafeCoords(world, 200, pos, entity);
        if (safeCoords != null) {
            PeasMod.LOGGER.debug("Safely rerouted!");
            return makePortalInfo(entity, safeCoords.getX(), entity.getY(), safeCoords.getZ());
        }

        PeasMod.LOGGER.info("Did not find a safe portal spot at first try, trying again with longer range.");
        safeCoords = findSafeCoords(world, 400, pos, entity);

        if (safeCoords != null) {
            PeasMod.LOGGER.info("Safely rerouted to long range portal.  Return trip not guaranteed.");
            return makePortalInfo(entity, safeCoords.getX(), entity.getY(), safeCoords.getZ());
        }

        PeasMod.LOGGER.warn("Still did not find a safe portal spot.");

        return makePortalInfo(entity, entity.position());
    }

    private static BlockState randNatureBlock(Random random) {
        final Block[] blocks = { Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.GRASS, Blocks.POPPY,
                Blocks.DANDELION };
        return blocks[random.nextInt(blocks.length)].defaultBlockState();
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(final Entity entity, final ServerWorld destWorld,
            final Function<ServerWorld, PortalInfo> defaultPortalInfo) {
        PortalInfo info = placeInExistingPortal(destWorld, entity, entity.blockPosition(),
                entity instanceof PlayerEntity);
        if (info == null) {
            info = moveToSafeCoords(destWorld, entity);
            makePortal(entity, destWorld, info.pos);
            info = placeInExistingPortal(destWorld, entity, new BlockPos(info.pos),
                    entity instanceof PlayerEntity);
        }
        return info;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw,
            Function<Boolean, Entity> repositionEntity) {
        entity.fallDistance = 0;
        return repositionEntity.apply(false);
    }

    private PortalInfo placeInExistingPortal(final ServerWorld destWorld, final Entity entity,
            final BlockPos pos, final boolean isPlayer) {
        int currentRadius = this.scanRadius;
        BlockPos blockpos = BlockPos.ZERO;
        boolean flag = true;
        final ColumnPos columnPos = new ColumnPos(pos);
        if (!isPlayer && COLUMN_MAP.containsKey(columnPos))
            return null;

        final PortalPosition portalPos = DEST_COORD_MAP.containsKey(destWorld.dimension().location())
                ? DEST_COORD_MAP.get(destWorld.dimension().location()).get(columnPos)
                : null;
        if (portalPos != null) {
            blockpos = portalPos.pos;
            portalPos.lastUpdateTime = destWorld.getGameTime();
            flag = false;
        } else {
            double d = Double.MAX_VALUE;
            for (int xPos = -currentRadius; xPos < currentRadius; xPos++) {
                BlockPos pos1;
                for (int yPos = -currentRadius; yPos < currentRadius; yPos++) {
                    // Skip positions outside world border
                    if (!destWorld.getWorldBorder().isWithinBounds(pos.offset(xPos, 0, yPos))) {
                        continue;
                    }

                    // Skip un-generated chunks
                    final ChunkPos chunkPos = new ChunkPos(pos.offset(xPos, 0, yPos));
                    if (!destWorld.getChunkSource().chunkMap.isExistingChunkFull(chunkPos)) {
                        continue;
                    }

                    // Fetch chunk so it can be unloaded if needed
                    final Chunk chunk = destWorld.getChunk(chunkPos.x, chunkPos.z);
                    for (BlockPos pos2 = pos.offset(xPos, getScanHeight(destWorld, pos) - pos.getY(),
                            yPos); pos2.getY() >= 0; pos2 = pos1) {
                        pos1 = pos2.below();

                        if (d > 0 && pos1.distSqr(pos) > d) {
                            continue;
                        }

                        if (isPortal(chunk.getBlockState(pos2))) {
                            for (pos1 = pos2.below(); isPortal(chunk.getBlockState(pos1)); pos1.below()) {
                                pos2 = pos1;
                            }

                            final double d1 = pos2.distSqr(pos);
                            if (d < 0 || d1 < d) {
                                d = d1;
                                pos = pos2;
                                currentRadius = MathHelper.ceil(MathHelper.sqrt(d1));
                            }
                        }
                    }

                    if (!destWorld.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) {
                        // queue unload
                    }
                }
            }
        }

        if (blockpos.equals(BlockPos.ZERO)) {
            final long factor = destWorld.getGameTime() + 300L;
            COLUMN_MAP.put(columnPos, factor);
            return null;
        }
        if (flag) {
            DEST_COORD_MAP.putIfAbsent(destWorld.dimension().location(),
                    Maps.newHashMapWithExpectedSize(4096));
            DEST_COORD_MAP.get(destWorld.dimension().location()).put(columnPos,
                    new PortalPosition(blockpos, destWorld.getGameTime()));
            destWorld.getChunkSource().registerTickingTicket(TicketType.PORTAL, new ChunkPos(blockpos), 3,
                    new BlockPos(columnPos.x, blockpos.getY(), columnPos.z));
        }

        // replace with our own placement logic
        final BlockPos[] portalBorder = getBoundaryPositions(destWorld, blockpos).toArray(new BlockPos[0]);
        final BlockPos borderPos = portalBorder[0/* random.nextInt(portalBorder.length) */];

        final double portalX = borderPos.getX() + 0.5;
        final double portalY = borderPos.getY() + 1.0;
        final double portalZ = borderPos.getZ() + 0.5;

        return makePortalInfo(entity, portalX, portalY, portalZ);
    }

    private static class PortalPosition {
        public final BlockPos pos;
        public long lastUpdateTime;

        PortalPosition(final BlockPos pos, final long time) {
            this.pos = pos;
            this.lastUpdateTime = time;
        }
    }
}
