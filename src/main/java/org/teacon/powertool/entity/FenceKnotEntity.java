package org.teacon.powertool.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.item.PowerToolItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FenceKnotEntity extends HangingEntity {

    private static final EntityDataAccessor<Set<BlockPos>> CONNECT_TO = SynchedEntityData.defineId(FenceKnotEntity.class, PowerToolEntities.BLOCK_POS_LIST);

    public FenceKnotEntity(Level level, BlockPos pos) {
        super(PowerToolEntities.FENCE_KNOT.get(), level, pos);
        this.pos = BlockPos.containing(pos.getX(), pos.getY(), pos.getZ());
        this.recalculateBoundingBox();
        this.noCulling = true;
    }

    public FenceKnotEntity(EntityType<FenceKnotEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    protected Vec3 getLeashOffset() {
        return Vec3.ZERO;
    }

    @Override
    public void dropItem(@Nullable Entity e) {
        this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag data) {
        var toPos = data.getList("ConnectTo", Tag.TAG_COMPOUND);
        if (!toPos.isEmpty()) {
            var list = new LinkedHashSet<BlockPos>();
            for (var tag : toPos) {
                NbtUtils.readBlockPos((CompoundTag) tag, "pos").ifPresent(list::add);
            }
            this.getEntityData().set(CONNECT_TO, list);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag data) {
        var toPos = new ListTag();
        for (var pos : this.getEntityData().get(CONNECT_TO)) {
            var tag = new CompoundTag();
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            toPos.add(tag);
        }
        data.put("ConnectTo", toPos);
    }
    
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, 0, this.getPos());
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var held = p.getItemInHand(hand);
        if (held.getItem() == Items.LEAD) {
            var data = held.get(PowerToolItems.KNOT_DATA);
            if (data == null) {
                // Connection start.
                held.set(PowerToolItems.KNOT_DATA,new PowerToolKnotData(this.pos));
                p.sendSystemMessage(Component.translatable("entity.powertool.fence_knot.connecting", this.pos.toShortString()));
            } else {
                var fromPos = data.pos;
                var knots = this.level().getEntitiesOfClass(FenceKnotEntity.class, new AABB(fromPos.getX(), fromPos.getY(), fromPos.getZ(), fromPos.getX() + 1, fromPos.getY() + 1, fromPos.getZ() + 1));
                if (!knots.isEmpty()) {
                    var fromKnot = knots.getFirst();
                    var thisConnectTo = this.getEntityData().get(CONNECT_TO);
                    var otherConnectTo = fromKnot.getEntityData().get(CONNECT_TO);
                    if (otherConnectTo.contains(this.pos)) {
                        // Link exists as 1 -> 2, remove link
                        otherConnectTo.remove(this.pos);
                        // Update 1 (fromKnot)
                        fromKnot.getEntityData().set(CONNECT_TO, otherConnectTo, true);
                        p.sendSystemMessage(Component.translatable("entity.powertool.fence_knot.disconnected", fromKnot.pos.toShortString(), this.pos.toShortString()));
                    } else if (thisConnectTo.contains(fromPos)) {
                        // Link exists as 1 <- 2, remove link
                        thisConnectTo.remove(fromPos);
                        // Update 2 (this knot)
                        this.getEntityData().set(CONNECT_TO, thisConnectTo, true);
                        p.sendSystemMessage(Component.translatable("entity.powertool.fence_knot.disconnected", this.pos.toShortString(), fromKnot.pos.toShortString()));
                    } else {
                        // Link does not exist, add link as 1 -> 2
                        otherConnectTo.add(this.pos);
                        // Update 1 (fromKnot)
                        fromKnot.getEntityData().set(CONNECT_TO, otherConnectTo, true);
                        p.sendSystemMessage(Component.translatable("entity.powertool.fence_knot.connected", fromKnot.pos.toShortString(), this.pos.toShortString()));
                    }
                }
                held.set(PowerToolItems.KNOT_DATA,null);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CONNECT_TO,new LinkedHashSet<>());
    }
    
    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        return distanceSq < 1024.0;
    }
    
    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        var x_ = pos.getX() + 0.5D;
        var y_ = pos.getY() + 0.375D;
        var z_ = pos.getZ() + 0.5D;
        double width = (double)this.getType().getWidth() / 2.0D;
        double height = this.getType().getHeight();
        return new AABB(x_ - width, y_, z_ - width, x_ + width, y_ + height, z_ + width);
    }
    
    public Set<BlockPos> getConnectTo() {
        return this.entityData.get(CONNECT_TO);
    }
    
    public record PowerToolKnotData(BlockPos pos) {
        
        public static final Codec<PowerToolKnotData> CODEC = RecordCodecBuilder.create(
                ins -> ins.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(PowerToolKnotData::pos)
        ).apply(ins, PowerToolKnotData::new));
        
        public static final StreamCodec<ByteBuf, PowerToolKnotData> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                PowerToolKnotData::pos,
                PowerToolKnotData::new
        );
    }
}
