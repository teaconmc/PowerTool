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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.client.renders.BezierCurveRenderingPipeline;
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
    private Set<SectionPos> affectedSections = Set.of();
    
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
            line = getLevel() == null || getLevel().isClientSide() ? new Line3f(sideCount, radius, bezierCurve.getPoints()) : null;
        }
        if (getLevel() instanceof ServerLevel serverLevel) {
            this.setChanged();
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        } else {
            updateClientRendering();
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
        updateClientRendering();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        removeClientRendering();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        removeClientRendering();
        super.setRemoved();
    }

    private void removeClientRendering() {
        var level = getLevel();
        if (level != null && level.isClientSide()) {
            updateAffectedChunkAttachments(level, Set.of());
            affectedSections = Set.of();
            var pipeline = BezierCurveRenderingPipeline.getInstance();
            if (pipeline != null) {
                pipeline.remove(this);
            }
        }
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

    private void updateClientRendering() {
        var level = getLevel();
        if (level != null && level.isClientSide()) {
            var newAffectedSections = calculateAffectedSections();
            updateAffectedChunkAttachments(level, newAffectedSections);
            affectedSections = newAffectedSections;
            var pipeline = BezierCurveRenderingPipeline.getInstance();
            if (pipeline != null) {
                pipeline.update(this);
            }
        }
    }

    public boolean affectsSection(SectionPos sectionPos) {
        return affectedSections.contains(sectionPos);
    }

    public boolean affectsChunk(ChunkPos chunkPos) {
        return affectedSections.stream().anyMatch(sectionPos -> sectionPos.x() == chunkPos.x() && sectionPos.z() == chunkPos.z());
    }

    private Set<SectionPos> calculateAffectedSections() {
        if (bezierCurve == null) return Set.of();
        var points = bezierCurve.getPoints();
        var result = new HashSet<SectionPos>();
        for (int i = 0; i < points.size() - 1; i++) {
            addAffectedSections(result, points.get(i), points.get(i + 1));
        }
        return result;
    }

    private void addAffectedSections(Set<SectionPos> result, Vector3f start, Vector3f end) {
        double offsetX = worldCoordinate ? 0 : getBlockPos().getX();
        double offsetY = worldCoordinate ? 0 : getBlockPos().getY();
        double offsetZ = worldCoordinate ? 0 : getBlockPos().getZ();
        double expansion = Math.abs(radius);
        int minSectionX = SectionPos.blockToSectionCoord((int) Math.floor(Math.min(start.x, end.x) + offsetX - expansion));
        int minSectionY = SectionPos.blockToSectionCoord((int) Math.floor(Math.min(start.y, end.y) + offsetY - expansion));
        int minSectionZ = SectionPos.blockToSectionCoord((int) Math.floor(Math.min(start.z, end.z) + offsetZ - expansion));
        int maxSectionX = SectionPos.blockToSectionCoord((int) Math.floor(Math.max(start.x, end.x) + offsetX + expansion));
        int maxSectionY = SectionPos.blockToSectionCoord((int) Math.floor(Math.max(start.y, end.y) + offsetY + expansion));
        int maxSectionZ = SectionPos.blockToSectionCoord((int) Math.floor(Math.max(start.z, end.z) + offsetZ + expansion));
        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                    result.add(SectionPos.of(sectionX, sectionY, sectionZ));
                }
            }
        }
    }

    private void updateAffectedChunkAttachments(Level level, Set<SectionPos> newAffectedSections) {
        var oldChunks = collectAffectedChunks(affectedSections);
        var newChunks = collectAffectedChunks(newAffectedSections);
        var chunksToUpdate = new HashSet<>(oldChunks);
        chunksToUpdate.addAll(newChunks);
        for (var chunkPos : chunksToUpdate) {
            var chunk = level.getChunkSource().getChunk(chunkPos.x(), chunkPos.z(), ChunkStatus.FULL, false);
            if (chunk == null) continue;
            var blockPositions = new ArrayList<>(chunk.getData(PowerToolAttachments.BEZIER_CURVES));
            blockPositions.removeIf(getBlockPos()::equals);
            if (newChunks.contains(chunkPos)) {
                blockPositions.add(getBlockPos());
            }
            chunk.setData(PowerToolAttachments.BEZIER_CURVES, List.copyOf(blockPositions));
        }
    }

    private Set<ChunkPos> collectAffectedChunks(Set<SectionPos> sections) {
        var result = new HashSet<ChunkPos>();
        for (var sectionPos : sections) {
            result.add(new ChunkPos(sectionPos.x(), sectionPos.z()));
        }
        return result;
    }
    
}
