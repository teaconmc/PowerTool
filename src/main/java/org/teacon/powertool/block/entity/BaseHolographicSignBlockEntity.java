/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.block.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.holo_sign.HoloSignBEFlag;
import org.teacon.powertool.block.holo_sign.HolographicSignBlock;

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

    public void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("color", this.colorInARGB);
        tag.putFloat("scale", this.scale);
        tag.putInt("align", this.align.ordinal());
        tag.putBoolean("lock",lock);
        tag.putInt("rotate", yRotate);
        tag.putBoolean("bidirectional",bidirectional);
        tag.putBoolean("renderBackground",renderBackground);
        tag.putBoolean("dropShadow",dropShadow);
        tag.putInt("xRotate", xRotate);
        tag.putFloat("zOffset", zOffset);
        tag.putBoolean("lit", lit);
    }
    
    public void readHistory(CompoundTag tag){
        if (tag.contains("shadow", Tag.TAG_INT)) {
            var shadow = Shadow.byOrdinal(tag.getInt("shadow"));
            if(shadow == Shadow.PLATE){
                this.renderBackground = true;
                this.dropShadow = false;
            }
            if(shadow == Shadow.DROP){
                this.renderBackground = false;
                this.dropShadow = true;
            }
            if(shadow == Shadow.NONE){
                this.renderBackground = false;
                this.dropShadow = false;
            }
        }
        if (tag.contains("arrange", Tag.TAG_INT)) {
            var arrange = LayerArrange.byOrdinal(tag.getInt("arrange"));
            if(arrange == LayerArrange.FRONT) this.zOffset = -0.45f;
            else if(arrange == LayerArrange.CENTER) this.zOffset = 0f;
            else if(arrange == LayerArrange.BACK) this.zOffset = 0.45f;
        }
    }

    public void readFrom(CompoundTag tag,HolderLookup.Provider registries) {
        this.readHistory(tag);
        if (tag.contains("color", Tag.TAG_INT)) {
            this.colorInARGB = tag.getInt("color");
        }
        if (tag.contains("scale", Tag.TAG_FLOAT)) {
            this.scale = tag.getFloat("scale");
        }
        if (tag.contains("align", Tag.TAG_INT)) {
            this.align = Align.byOrdinal(tag.getInt("align"));
        }
        //Tag.TAG_BOOLEAN does not exist. I don’t know what to fill in the latter parameter.
        if(tag.contains("lock")){
            this.lock = tag.getBoolean("lock");
        }
        if(tag.contains("rotate",Tag.TAG_INT)){
            this.yRotate = tag.getInt("rotate");
        }
        if(tag.contains("bidirectional")){
            this.bidirectional = tag.getBoolean("bidirectional");
        }
        if(tag.contains("renderBackground")){
            this.renderBackground = tag.getBoolean("renderBackground");
        }
        if(tag.contains("dropShadow")){
            this.dropShadow = tag.getBoolean("dropShadow");
        }
        if(tag.contains("xRotate",Tag.TAG_INT)){
            this.xRotate = tag.getInt("xRotate");
        }
        if(tag.contains("zOffset",Tag.TAG_FLOAT)){
            this.zOffset = tag.getFloat("zOffset");
        }
        if(tag.contains("lit")){
            this.lit = tag.getBoolean("lit");
            if(this.getLevel() != null){
                this.getLevel().setBlock(getBlockPos(),getBlockState().setValue(HolographicSignBlock.LIT,lit), Block.UPDATE_ALL);
            }
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
