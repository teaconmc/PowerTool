package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.utils.VanillaUtils;
import org.teacon.powertool.utils.math.BezierCurve3f;
import org.teacon.powertool.utils.math.Line3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BezierCurveBlockEntity extends BlockEntity implements IClientUpdateBlockEntity{
    
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
    public BezierCurve3f bezierCurve;
    public Line3f line;
    
    private int sideCountOld;
    private float radiusOld;
    
    public BezierCurveBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.BEZIER_CURVE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public void setControlPoints(List<Vector3f> controlPoints) {
        this.controlPoints = controlPoints;
        if(steps < 2 || sideCount < 3){
            line = null;
            return;
        }
        var newCurve = new BezierCurve3f(steps,controlPoints);
        if((!newCurve.equals(bezierCurve) || sideCountOld != sideCount || radiusOld != radius) && getLevel() != null && getLevel().isClientSide){
            bezierCurve = newCurve;
            line = new Line3f(sideCount,radius,bezierCurve.getPoints());
        }
        if(getLevel() != null){
            this.setChanged();
            getLevel().sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(), Block.UPDATE_ALL);
        }
    }
    
    public void read(CompoundTag tag) {
        sideCountOld = sideCount;
        radiusOld = radius;
        if(tag.contains("steps")) steps = tag.getInt("steps");
        if(tag.contains("sideCount")) sideCount = tag.getInt("sideCount");
        if(tag.contains("radius")) radius = tag.getFloat("radius");
        if(tag.contains("uScale")) uScale = tag.getInt("uScale");
        if(tag.contains("vScale")) vScale = tag.getInt("vScale");
        if(tag.contains("texture")) texture = Objects.requireNonNullElse(Identifier.tryParse(tag.getString("texture")),VanillaUtils.MISSING_TEXTURE);
        if(tag.contains("clampMode")) clampMode = tag.getBoolean("clampMode");
        if(tag.contains("worldCoordinate")) worldCoordinate = tag.getBoolean("worldCoordinate");
        if(tag.contains("controlPointSize")){
            var size = tag.getInt("controlPointSize");
            controlPoints = new ArrayList<>();
            for(int i = 0; i < size; i++){
                var x = tag.getFloat("controlPoint"+i+"x");
                var y = tag.getFloat("controlPoint"+i+"y");
                var z = tag.getFloat("controlPoint"+i+"z");
                controlPoints.add(new Vector3f(x,y,z));
            }
            setControlPoints(controlPoints);
        }
    }
    
    public CompoundTag write(CompoundTag tag) {
        tag.putInt("steps", steps);
        tag.putInt("sideCount", sideCount);
        tag.putFloat("radius", radius);
        tag.putInt("controlPointSize", controlPoints.size());
        tag.putInt("uScale", uScale);
        tag.putInt("vScale", vScale);
        tag.putString("texture", texture.toString());
        tag.putBoolean("clampMode", clampMode);
        tag.putBoolean("worldCoordinate", worldCoordinate);
        for(int i = 0; i < controlPoints.size(); i++){
            tag.putFloat("controlPoint"+i+"x", controlPoints.get(i).x());
            tag.putFloat("controlPoint"+i+"y", controlPoints.get(i).y());
            tag.putFloat("controlPoint"+i+"z", controlPoints.get(i).z());
        }
        return tag;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        read(tag);
        super.loadAdditional(tag, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        write(tag);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        return write(result);
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        read(tag);
        super.handleUpdateTag(tag, registries);
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        this.handleUpdateTag(pkt.getTag(),lookupProvider);
    }
    
    @Override
    public void update(CompoundTag tag, HolderLookup.Provider registries) {
        read(tag);
    }
    
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider registries) {
        write(tag);
    }
}
