/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HolographicSignBlockEntity extends BlockEntity {

    /** Controls how text are aligned: left-align, centered, or right-align. */
    public enum Align {
        LEFT(Component.translatable("powertool.gui.holographic_sign.align_left")),
        CENTER(Component.translatable("powertool.gui.holographic_sign.align_center")),
        RIGHT(Component.translatable("powertool.gui.holographic_sign.align_right"));

        private static final Align[] VALUES = Align.values();
        public final Component displayName;

        Align(Component displayName) {
            this.displayName = displayName;
        }

        public static Align byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
        }
    }

    /** Represents the text shadow. */
    public enum Shadow {
        NONE(Component.translatable("powertool.gui.holographic_sign.shadow_none")),
        DROP(Component.translatable("powertool.gui.holographic_sign.shadow_drop")),
        PLATE(Component.translatable("powertool.gui.holographic_sign.shadow_plate"));

        private static final Shadow[] VALUES = Shadow.values();
        public final Component displayName;

        Shadow(Component displayName) {
            this.displayName = displayName;
        }

        public static Shadow byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : PLATE;
        }
    }

    /** Represents the Z-offset of the text: above things, same layer or below things. */
    public enum LayerArrange {
        FRONT(Component.translatable("powertool.gui.holographic_sign.arrange_front")),
        CENTER(Component.translatable("powertool.gui.holographic_sign.arrange_center")),
        BACK(Component.translatable("powertool.gui.holographic_sign.arrange_back"));

        private static final LayerArrange[] VALUES = LayerArrange.values();
        public final Component displayName;

        LayerArrange(Component displayName) {
            this.displayName = displayName;
        }

        public static LayerArrange byOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
        }
    }

    public List<? extends Component> contents = Collections.emptyList();
    public int colorInARGB = 0xFFFFFFFF;

    public int bgColorInARGB = 0x40000000;
    public float scale = 1.0F;
    public Align align = Align.CENTER;
    public Shadow shadow = Shadow.DROP;
    public LayerArrange arrange = LayerArrange.CENTER;
    
    public boolean lock = false;
    public int rotate = 0;
    
    public boolean bidirectional = false;

    public HolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    private void writeTo(CompoundTag tag) {
        var list = new ListTag();
        for (var text : this.contents) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        tag.put("content", list);
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

    private void readFrom(CompoundTag tag) {
        var loaded = new ArrayList<Component>();
        for (var entry : tag.getList("content", Tag.TAG_STRING)) {
            loaded.add(Component.Serializer.fromJson(entry.getAsString()));
        }
        this.contents = loaded;
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
    protected void saveAdditional(CompoundTag tag) {
        this.writeTo(tag);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.readFrom(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        this.writeTo(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.readFrom(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.handleUpdateTag(pkt.getTag());
    }
}
