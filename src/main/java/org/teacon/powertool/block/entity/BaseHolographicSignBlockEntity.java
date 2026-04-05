/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.block.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.holo_sign.HoloSignBEFlag;
import org.teacon.powertool.block.holo_sign.HolographicSignBlock;

@NonNullByDefault
public class BaseHolographicSignBlockEntity extends BlockEntity implements HoloSignBEFlag,IClientUpdateBlockEntity {
    
    /** Controls how text are aligned: left-align, centered, or right-align. */
    public enum Align  implements StringRepresentable {
        LEFT(Component.translatable("powertool.gui.holographic_sign.align_left")),
        CENTER(Component.translatable("powertool.gui.holographic_sign.align_center")),
        RIGHT(Component.translatable("powertool.gui.holographic_sign.align_right"));
        
        
        public static final Codec<Align> CODEC = StringRepresentable.fromEnum(Align::values);
        public static final StreamCodec<ByteBuf,Align> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
        private static final Align[] VALUES = Align.values();
        public final Component displayName;

        Align(Component displayName) {
            this.displayName = displayName;
        }

        public static Align byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
        }
        
        @Override
        @NonNull
        public String getSerializedName() {
            return name();
        }
        
    }

    /** Represents the text shadow. */
    public enum Shadow implements StringRepresentable  {
        NONE(Component.translatable("powertool.gui.holographic_sign.shadow_none")),
        DROP(Component.translatable("powertool.gui.holographic_sign.shadow_drop")),
        PLATE(Component.translatable("powertool.gui.holographic_sign.shadow_plate"));
        
        public static final Codec<Shadow> CODEC = StringRepresentable.fromEnum(Shadow::values);
        public static final StreamCodec<ByteBuf,Shadow> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
        private static final Shadow[] VALUES = Shadow.values();
        public final Component displayName;

        Shadow(Component displayName) {
            this.displayName = displayName;
        }

        public static Shadow byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : PLATE;
        }
        
        @Override
        @NonNull
        public String getSerializedName() {
    return name();
}}

    /** Represents the Z-offset of the text: above things, same layer or below things. */
    public enum LayerArrange implements StringRepresentable  {
        FRONT(Component.translatable("powertool.gui.holographic_sign.arrange_front"),-0.45f),
        CENTER(Component.translatable("powertool.gui.holographic_sign.arrange_center"),0f),
        BACK(Component.translatable("powertool.gui.holographic_sign.arrange_back"),0.45f),
        CUSTOM(Component.translatable("powertool.gui.holographic_sign.arrange_custom"),Float.NaN);
        
        public static final Codec<LayerArrange> CODEC = StringRepresentable.fromEnum(LayerArrange::values);
        public static final StreamCodec<ByteBuf, LayerArrange> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
        private static final LayerArrange[] VALUES = LayerArrange.values();
        public final Component displayName;
        public final float offsetValue;

        LayerArrange(Component displayName, float offsetValue) {
            this.displayName = displayName;
            this.offsetValue = offsetValue;
        }

        public static LayerArrange byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
        }
        
        public static LayerArrange formOffset(float offset) {
            for(var arr : LayerArrange.VALUES) {
                if(arr.offsetValue == offset) {
                    return arr;
                }
            }
            return CUSTOM;
        }
        
        @Override
        @NonNull
        public String getSerializedName() {
    return name();
}}
    
    public int colorInARGB = 0xFFFFFFFF;
    //public int bgColorInARGB = VanillaUtils.getColor(255,255,255,0);
    public float scale = 1.0F;
    public Align align = Align.CENTER;
    //public Shadow shadow = Shadow.PLATE;
    public boolean renderBackground = true;
    public boolean dropShadow = false;
    //public LayerArrange arrange = LayerArrange.CENTER;
    public boolean lock = false;
    public int yRotate = 0;
    public int xRotate = 0;
    public float zOffset = 0F;
    
    public boolean bidirectional = false;
    public boolean lit = true;

    public BaseHolographicSignBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
    }

    public void writeTo(ValueOutput output) {
        output.putInt("color", this.colorInARGB);
        output.putFloat("scale", this.scale);
        output.putInt("align", this.align.ordinal());
        output.putBoolean("lock",lock);
        output.putInt("rotate", yRotate);
        output.putBoolean("bidirectional",bidirectional);
        output.putBoolean("renderBackground",renderBackground);
        output.putBoolean("dropShadow",dropShadow);
        output.putInt("xRotate", xRotate);
        output.putFloat("zOffset", zOffset);
        output.putBoolean("lit", lit);
    }

    public void readFrom(ValueInput input) {
        this.colorInARGB = input.getIntOr("color",-1);
        this.scale = input.getFloatOr("scale",1.0F);
        this.align = Align.byOrdinal(input.getIntOr("align",0));
        this.lock = input.getBooleanOr("lock",false);
        this.yRotate = input.getIntOr("rotate",0);
        this.bidirectional = input.getBooleanOr("bidirectional",false);
        this.renderBackground = input.getBooleanOr("renderBackground",false);
        this.dropShadow = input.getBooleanOr("dropShadow",false);
        this.xRotate = input.getIntOr("xRotate",0);
        this.zOffset = input.getFloatOr("zOffset",0F);
        this.lit = input.getBooleanOr("lit",false);
        if(this.getLevel() != null){
            this.getLevel().setBlock(getBlockPos(),getBlockState().setValue(HolographicSignBlock.LIT,lit), Block.UPDATE_ALL);
        }
    }
    
    @Override
    public void updateFromClient(ValueInput input) {
        this.readFrom(input);
    }
    
    @Override
    public void writeFromClient(ValueOutput output) {
        this.writeTo(output);
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.readFrom(input);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.writeTo(output);
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
    
    public void filterMessage(ServerPlayer player){
    
    }
}
