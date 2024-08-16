package org.teacon.powertool.block.holo_sign;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;

import java.util.function.BiFunction;

public enum SignType implements StringRepresentable  {
    COMMON(CommonHolographicSignBlockEntity::new),
    URL(LinkHolographicSignBlockEntity::new),
    RAW_JSON(RawJsonHolographicSignBlockEntity::new),
    ;
    
    private final BiFunction<BlockPos, BlockState, ? extends BaseHolographicSignBlockEntity> blockEntitySupplier;
    
    public static final Codec<SignType> CODEC = StringRepresentable.fromEnum(SignType::values);
    public static final StreamCodec<ByteBuf,SignType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    
    SignType(BiFunction<BlockPos, BlockState, ? extends BaseHolographicSignBlockEntity> blockEntitySupplier) {
        this.blockEntitySupplier = blockEntitySupplier;
    }
    
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntitySupplier.apply(pos, state);
    }
    
    
    @Override
    @NotNull
    public String getSerializedName() {
    return name();
}}
