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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    
    public void read(ValueInput input) {
        sideCountOld = sideCount;
        radiusOld = radius;
        steps = input.getIntOr("steps",0);
        sideCount = input.getIntOr("sideCount",3);
        radius = input.getFloatOr("radius",1);
        uScale = input.getIntOr("uScale",1);
        vScale = input.getIntOr("vScale",1);
        texture = input.read("texture",Identifier.CODEC).orElse(VanillaUtils.MISSING_TEXTURE);
        clampMode = input.getBooleanOr("clampMode",false);
        worldCoordinate = input.getBooleanOr("worldCoordinate",false);
        color = input.getIntOr("color",-1);
        var size = input.getIntOr("controlPointSize",1);
        controlPoints = new ArrayList<>();
        for(int i = 0; i < size; i++){
            var x = input.getFloatOr("controlPoint"+i+"x",0);
            var y = input.getFloatOr("controlPoint"+i+"y",0);
            var z = input.getFloatOr("controlPoint"+i+"z",0);
            controlPoints.add(new Vector3f(x,y,z));
        }
        setControlPoints(controlPoints);
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
        for(int i = 0; i < controlPoints.size(); i++){
            output.putFloat("controlPoint"+i+"x", controlPoints.get(i).x());
            output.putFloat("controlPoint"+i+"y", controlPoints.get(i).y());
            output.putFloat("controlPoint"+i+"z", controlPoints.get(i).z());
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
    
}
