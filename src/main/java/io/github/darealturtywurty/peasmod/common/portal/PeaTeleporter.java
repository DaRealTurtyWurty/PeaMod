package io.github.darealturtywurty.peasmod.common.portal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class PeaTeleporter implements ITeleporter {

	private static class PortalPosition {
		public final BlockPos pos;
		public long lastUpdateTime;

		PortalPosition(final BlockPos pos, final long time) {
			this.pos = pos;
			this.lastUpdateTime = time;
		}
	}

	private static final Map<ResourceLocation, Map<ColumnPos, PortalPosition>> DEST_COORD_MAP = new HashMap<>();
	private static final Object2LongMap<ColumnPos> COLUMN_MAP = new Object2LongOpenHashMap<>();

	private final int scanRadius;

	public PeaTeleporter(final int scanRadius) {
		currentRadius = scanRadius;
	}

	@Nullable
	@Override
	public PortalInfo getPortalInfo(final Entity entity, final ServerWorld destWorld,
			final Function<ServerWorld, PortalInfo> defaultPortalInfo) {
		PortalInfo info = placeInExistingPortal(destWorld, entity, entity.blockPosition(), entity instanceof PlayerEntity);
		if (info == null) {
			info = moveToSafeCoords(destWorld, entity);
			makePortal(entity, destWorld, info.pos);
			info = placeInExistingPortal(destWorld, entity, new BlockPos(info.pos), entity instanceof PlayerEntity);
		}
		return info;
	}

	@Override
	public Entity placeEntity(final Entity entity, final ServerWorld currentWorld, final ServerWorld destWorld,
			final float yaw, final Function<Boolean, Entity> repositionEntity) {
		return ITeleporter.super.placeEntity(entity, currentWorld, destWorld, yaw, repositionEntity);
	}

	private PortalInfo placeInExistingPortal(final ServerWorld destWorld, final Entity entity, final BlockPos pos,
			final boolean isPlayer) {
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
					for (BlockPos pos2 = pos.offset(xPos, getScanHeight(destWorld, pos) - pos.getY(), yPos); pos2
							.getY() >= 0; pos2 = pos1) {
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
	}
}
