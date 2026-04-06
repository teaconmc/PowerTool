package org.teacon.powertool.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.datagen.PowerToolItemTagsProvider;
import org.teacon.powertool.item.PowerToolDataComponents;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NonNullByDefault
public class FenceKnotEntity extends HangingEntity {
    
    private static final EntityDataAccessor<Set<BlockPos>> CONNECT_TO = SynchedEntityData.defineId(FenceKnotEntity.class, PowerToolEntities.BLOCK_POS_LIST.get());
    private static final EntityDataAccessor<FenceKnotEntity.Type> TYPE = SynchedEntityData.defineId(FenceKnotEntity.class, PowerToolEntities.FENCE_KNOT_TYPE.get());
    
    public FenceKnotEntity(Level level, BlockPos pos) {
        super(PowerToolEntities.FENCE_KNOT.get(), level, pos);
        this.pos = BlockPos.containing(pos.getX(), pos.getY(), pos.getZ());
        this.recalculateBoundingBox();
    }
    
    public FenceKnotEntity(EntityType<FenceKnotEntity> type, Level level) {
        super(type, level);
    }
    
    @Override
    public void dropItem(ServerLevel level, @Nullable Entity causedBy) {
        this.playSound(SoundEvents.LEAD_UNTIED, 1.0F, 1.0F);
    }
    
    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.LEAD_TIED, 1.0F, 1.0F);
    }
    
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        var list = output.list("pos", BlockPos.CODEC);
        for (var pos : this.getEntityData().get(CONNECT_TO)) {
            list.add(pos);
        }
        output.putString("Type", this.getEntityData().get(TYPE).name());
    }
    
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        var list = input.listOrEmpty("pos", BlockPos.CODEC);
        this.getEntityData().set(CONNECT_TO, list.stream().collect(Collectors.toSet()));
        this.getEntityData().set(TYPE, FenceKnotEntity.Type.valueOf(input.getStringOr("Type", "Normal")));
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, 0, this.getPos());
    }
    
    @Override
    public InteractionResult interact(Player p, InteractionHand hand, Vec3 location) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        var held = p.getItemInHand(hand);
        if (held.is(PowerToolItemTagsProvider.TONK)) {
            var data = held.get(PowerToolDataComponents.KNOT_DATA);
            if (data == null) {
                // Connection start.
                held.set(PowerToolDataComponents.KNOT_DATA, new PowerToolKnotData(this.pos));
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
                held.set(PowerToolDataComponents.KNOT_DATA, null);
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
        builder.define(CONNECT_TO, new LinkedHashSet<>());
        builder.define(TYPE, Type.Normal);
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
        double width = (double) this.getType().getWidth() / 2.0D;
        double height = this.getType().getHeight();
        return new AABB(x_ - width, y_, z_ - width, x_ + width, y_ + height, z_ + width);
    }
    
    public Set<BlockPos> getConnectTo() {
        return this.entityData.get(CONNECT_TO);
    }
    
    public void setType(Type type) {
        this.entityData.set(TYPE, type);
    }
    
    //entity有getType方法了
    public Type getTypeForRender() {
        return this.entityData.get(TYPE);
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
    
    public enum Type implements StringRepresentable {
        Thin(0.025f),
        Normal(0.05f),
        Thick(0.08f);
        
        private final float width;
        
        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
        Type(float width) {
            this.width = width;
        }
        
        @Override
        @NonNull
        public String getSerializedName() {
            return name();
        }
        
        public float getWidth() {
            return this.width;
        }
    }
}
