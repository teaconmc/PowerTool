package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.network.client.UpdateBezierCurveChunkDataPacket;
import org.teacon.powertool.utils.VanillaUtils;
import org.teacon.powertool.utils.math.BezierCurve3f;
import org.teacon.powertool.utils.math.Line3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NonNullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BezierCurveBlockEntity extends BlockEntity implements IClientUpdateBlockEntity {
    
    public int steps;
    public int sideCount;
    public float radius = 1;
    public int uScale = 1;
    public int vScale = 1;
    public int color = -1;
    public boolean clampMode = false;
    public boolean worldCoordinate = false;
    public Identifier texture = VanillaUtils.MISSING_TEXTURE;
    public List<Vector3f> controlPoints = new ArrayList<>();
    @Nullable
    public BezierCurve3f bezierCurve;
    @Nullable
    public Line3f line;
    private Set<ChunkPos> affectedChunks = Set.of();
    
    public BezierCurveBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.BEZIER_CURVE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public void setControlPoints(List<Vector3f> controlPoints) {
        this.controlPoints = controlPoints.stream().map(Vector3f::new).toList();
        if (steps < 2 || sideCount < 3 || this.controlPoints.size() < 2) {
            bezierCurve = null;
            line = null;
        } else {
            bezierCurve = new BezierCurve3f(steps, this.controlPoints);
            line = getLevel() == null || getLevel().isClientSide()
                    ? new Line3f(sideCount, radius, bezierCurve.getPoints())
                    : null;
        }
        if (getLevel() instanceof ServerLevel serverLevel) {
            updateAffectedChunks(serverLevel, calculateAffectedChunks());
            this.setChanged();
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
    
    public void read(ValueInput input) {
        steps = input.getIntOr("steps", 0);
        sideCount = input.getIntOr("sideCount", 3);
        radius = input.getFloatOr("radius", 1);
        uScale = input.getIntOr("uScale", 1);
        vScale = input.getIntOr("vScale", 1);
        texture = input.read("texture", Identifier.CODEC).orElse(VanillaUtils.MISSING_TEXTURE);
        clampMode = input.getBooleanOr("clampMode", false);
        worldCoordinate = input.getBooleanOr("worldCoordinate", false);
        color = input.getIntOr("color", -1);
        var size = input.getIntOr("controlPointSize", 1);
        controlPoints = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var x = input.getFloatOr("controlPoint" + i + "x", 0);
            var y = input.getFloatOr("controlPoint" + i + "y", 0);
            var z = input.getFloatOr("controlPoint" + i + "z", 0);
            controlPoints.add(new Vector3f(x, y, z));
        }
        setControlPoints(controlPoints);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            updateAffectedChunks(serverLevel, calculateAffectedChunks());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            updateAffectedChunks(serverLevel, Set.of());
        }
        super.preRemoveSideEffects(pos, state);
    }
    
    public void write(ValueOutput output) {
        output.putInt("steps", steps);
        output.putInt("sideCount", sideCount);
        output.putFloat("radius", radius);
        output.putInt("controlPointSize", controlPoints.size());
        output.putInt("uScale", uScale);
        output.putInt("vScale", vScale);
        output.putString("texture", texture.toString());
        output.putBoolean("clampMode", clampMode);
        output.putBoolean("worldCoordinate", worldCoordinate);
        output.putInt("color", color);
        for (int i = 0; i < controlPoints.size(); i++) {
            output.putFloat("controlPoint" + i + "x", controlPoints.get(i).x());
            output.putFloat("controlPoint" + i + "y", controlPoints.get(i).y());
            output.putFloat("controlPoint" + i + "z", controlPoints.get(i).z());
        }
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.read(input);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.write(output);
    }
    
    @Override
    public void writeFromClient(ValueOutput output) {
        this.write(output);
    }
    
    @Override
    public void updateFromClient(ValueInput input) {
        this.read(input);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private Set<ChunkPos> calculateAffectedChunks() {
        if (bezierCurve == null) return Set.of();
        var points = bezierCurve.getPoints();
        var result = new HashSet<ChunkPos>();
        for (int i = 0; i < points.size() - 1; i++) {
            addAffectedChunks(result, points.get(i), points.get(i + 1));
        }
        return Set.copyOf(result);
    }

    private void addAffectedChunks(Set<ChunkPos> result, Vector3f start, Vector3f end) {
        double startX = start.x + (worldCoordinate ? 0 : getBlockPos().getX());
        double startZ = start.z + (worldCoordinate ? 0 : getBlockPos().getZ());
        double endX = end.x + (worldCoordinate ? 0 : getBlockPos().getX());
        double endZ = end.z + (worldCoordinate ? 0 : getBlockPos().getZ());
        int chunkX = SectionPos.blockToSectionCoord((int) Math.floor(startX));
        int chunkZ = SectionPos.blockToSectionCoord((int) Math.floor(startZ));
        int endChunkX = SectionPos.blockToSectionCoord((int) Math.floor(endX));
        int endChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(endZ));
        result.add(new ChunkPos(chunkX, chunkZ));
        double deltaX = endX - startX;
        double deltaZ = endZ - startZ;
        int stepX = Double.compare(deltaX, 0);
        int stepZ = Double.compare(deltaZ, 0);
        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 16.0 / Math.abs(deltaX);
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 16.0 / Math.abs(deltaZ);
        double boundaryX = stepX > 0 ? (chunkX + 1) * 16.0 : chunkX * 16.0;
        double boundaryZ = stepZ > 0 ? (chunkZ + 1) * 16.0 : chunkZ * 16.0;
        double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : (boundaryX - startX) / deltaX;
        double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : (boundaryZ - startZ) / deltaZ;
        while (chunkX != endChunkX || chunkZ != endChunkZ) {
            if (tMaxX < tMaxZ) {
                chunkX += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxZ < tMaxX) {
                chunkZ += stepZ;
                tMaxZ += tDeltaZ;
            } else {
                chunkX += stepX;
                chunkZ += stepZ;
                tMaxX += tDeltaX;
                tMaxZ += tDeltaZ;
            }
            result.add(new ChunkPos(chunkX, chunkZ));
        }
    }

    private void updateAffectedChunks(ServerLevel level, Set<ChunkPos> newAffectedChunks) {
        var chunksToUpdate = new HashSet<>(affectedChunks);
        chunksToUpdate.addAll(newAffectedChunks);
        for (var chunkPos : chunksToUpdate) {
            var chunk = level.getChunk(chunkPos.x(), chunkPos.z());
            var blockPositions = new ArrayList<>(chunk.getData(PowerToolAttachments.BEZIER_CURVES));
            blockPositions.removeIf(getBlockPos()::equals);
            if (newAffectedChunks.contains(chunkPos)) {
                blockPositions.add(getBlockPos());
            }
            var updatedBlockPositions = List.copyOf(blockPositions);
            chunk.setData(PowerToolAttachments.BEZIER_CURVES, updatedBlockPositions);
            PacketDistributor.sendToPlayersTrackingChunk(
                    level,
                    chunkPos,
                    new UpdateBezierCurveChunkDataPacket(chunkPos.x(), chunkPos.z(), updatedBlockPositions)
            );
        }
        affectedChunks = Set.copyOf(newAffectedChunks);
    }
    
}
