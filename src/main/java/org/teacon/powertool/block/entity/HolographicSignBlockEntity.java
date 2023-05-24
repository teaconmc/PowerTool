package org.teacon.powertool.block.entity;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HolographicSignBlockEntity extends BlockEntity {

    public List<? extends Component> contents = Collections.emptyList();

    public HolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    private void writeTo(CompoundTag tag) {
        var list = new ListTag();
        for (var text : this.contents) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        tag.put("content", list);
    }

    private void readFrom(CompoundTag tag) {
        var loaded = new ArrayList<Component>();
        for (var entry : tag.getList("content", Tag.TAG_STRING)) {
            loaded.add(Component.Serializer.fromJson(entry.getAsString()));
        }
        this.contents = loaded;
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
