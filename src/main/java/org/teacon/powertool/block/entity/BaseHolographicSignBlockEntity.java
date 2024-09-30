/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.block.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.holo_sign.HoloSignBEFlag;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
        @NotNull
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
        @NotNull
        public String getSerializedName() {
    return name();
}}

    /** Represents the Z-offset of the text: above things, same layer or below things. */
    public enum LayerArrange implements StringRepresentable  {
        FRONT(Component.translatable("powertool.gui.holographic_sign.arrange_front")),
        CENTER(Component.translatable("powertool.gui.holographic_sign.arrange_center")),
        BACK(Component.translatable("powertool.gui.holographic_sign.arrange_back"));
        
        public static final Codec<LayerArrange> CODEC = StringRepresentable.fromEnum(LayerArrange::values);
        public static final StreamCodec<ByteBuf, LayerArrange> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
        private static final LayerArrange[] VALUES = LayerArrange.values();
        public final Component displayName;

        LayerArrange(Component displayName) {
            this.displayName = displayName;
        }

        public static LayerArrange byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
        }
        
        @Override
        @NotNull
        public String getSerializedName() {
    return name();
}}
    
    public int colorInARGB = 0xFFFFFFFF;

    public int bgColorInARGB = 0x40000000;
    public float scale = 1.0F;
    public Align align = Align.CENTER;
    public Shadow shadow = Shadow.DROP;
    public LayerArrange arrange = LayerArrange.CENTER;
    
    public boolean lock = false;
    public int rotate = 0;
    
    public boolean bidirectional = false;

    public BaseHolographicSignBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
    }

    public void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
       
        tag.putInt("color", this.colorInARGB);
        tag.putInt("backgroundColor", this.bgColorInARGB);
        tag.putFloat("scale", this.scale);
        tag.putInt("align", this.align.ordinal());
        tag.putInt("shadow", this.shadow.ordinal());
        tag.putInt("arrange", this.arrange.ordinal());
        tag.putBoolean("lock",lock);
        tag.putInt("rotate",rotate);
        tag.putBoolean("bidirectional",bidirectional);
    }

    public void readFrom(CompoundTag tag,HolderLookup.Provider registries) {
     
        if (tag.contains("color", Tag.TAG_INT)) {
            this.colorInARGB = tag.getInt("color");
        }
        if (tag.contains("backgroundColor", Tag.TAG_INT)) {
            this.bgColorInARGB = tag.getInt("backgroundColor");
        }
        if (tag.contains("scale", Tag.TAG_FLOAT)) {
            this.scale = tag.getFloat("scale");
        }
        if (tag.contains("align", Tag.TAG_INT)) {
            this.align = Align.byOrdinal(tag.getInt("align"));
        }
        if (tag.contains("shadow", Tag.TAG_INT)) {
            this.shadow = Shadow.byOrdinal(tag.getInt("shadow"));
        }
        if (tag.contains("arrange", Tag.TAG_INT)) {
            this.arrange = LayerArrange.byOrdinal(tag.getInt("arrange"));
        }
        //Tag.TAG_BOOLEAN does not exist. I don’t know what to fill in the latter parameter.
        if(tag.contains("lock")){
            this.lock = tag.getBoolean("lock");
        }
        if(tag.contains("rotate",Tag.TAG_INT)){
            this.rotate = tag.getInt("rotate");
        }
        if(tag.contains("bidirectional")){
            this.bidirectional = tag.getBoolean("bidirectional");
        }
    }
    
    @Override
    public void update(CompoundTag tag, HolderLookup.Provider registries) {
        readFrom(tag, registries);
    }
    
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider registries) {
        writeTo(tag, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.writeTo(tag,registries);
        super.saveAdditional(tag, registries);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.readFrom(tag,registries);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        this.writeTo(result,registries);
        return result;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.readFrom(tag,lookupProvider);
        super.handleUpdateTag(tag, lookupProvider);
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
    
    public void filterMessage(ServerPlayer player){
    
    }
}
