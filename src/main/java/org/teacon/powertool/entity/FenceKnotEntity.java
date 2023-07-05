package org.teacon.powertool.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
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

import java.util.LinkedHashSet;
import java.util.Set;

public class FenceKnotEntity extends HangingEntity {

    private static final EntityDataAccessor<Set<BlockPos>> CONNECT_TO = SynchedEntityData.defineId(FenceKnotEntity.class, PowerToolEntities.BLOCK_POS_LIST.get());

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
    protected void defineSynchedData() {
        this.getEntityData().define(CONNECT_TO, new LinkedHashSet<>());
    }

    @Override
    protected Vec3 getLeashOffset() {
        return Vec3.ZERO;
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
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
                list.add(NbtUtils.readBlockPos((CompoundTag) tag));
            }
            this.getEntityData().set(CONNECT_TO, list);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag data) {
        var toPos = new ListTag();
        for (var pos : this.getEntityData().get(CONNECT_TO)) {
            toPos.add(NbtUtils.writeBlockPos(pos));
        }
        data.put("ConnectTo", toPos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, 0, this.getPos());
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand hand) {
        if (this.level().isClientSide()) {
            p.sendSystemMessage(Component.literal("Hello there"));
            return InteractionResult.SUCCESS;
        }

        var held = p.getItemInHand(hand);
        if (held.getItem() == Items.LEAD) {
            var itemNbt = held.getTagElement("PowerToolKnot");
            if (itemNbt == null) {
                // Connection start.
                held.getOrCreateTag().put("PowerToolKnot", NbtUtils.writeBlockPos(this.pos));
            } else {
                var fromPos = NbtUtils.readBlockPos(itemNbt);
                var knots = this.level().getEntitiesOfClass(FenceKnotEntity.class, new AABB(fromPos.getX() - 1, fromPos.getY() - 1, fromPos.getZ() - 1, fromPos.getX() + 1, fromPos.getY() + 1, fromPos.getZ() + 1));
                if (!knots.isEmpty()) {
                    var fromKnot = knots.get(0);
                    var thisConnectTo = this.getEntityData().get(CONNECT_TO);
                    var otherConnectTo = fromKnot.getEntityData().get(CONNECT_TO);
                    if (otherConnectTo.contains(this.pos)) {
                        // Link exists as 1 -> 2, remove link
                        otherConnectTo.remove(this.pos);
                        // Update 1 (fromKnot)
                        fromKnot.getEntityData().set(CONNECT_TO, otherConnectTo, true);
                    } else if (thisConnectTo.contains(fromPos)) {
                        // Link exists as 1 <- 2, remove link
                        thisConnectTo.remove(fromPos);
                        // Update 2 (this knot)
                        this.getEntityData().set(CONNECT_TO, thisConnectTo, true);
                    } else {
                        // Link does not exist, add link as 1 -> 2
                        otherConnectTo.add(this.pos);
                        // Update 1 (fromKnot)
                        fromKnot.getEntityData().set(CONNECT_TO, otherConnectTo, true);
                    }
                }
                var mainTag = held.getTag();
                if (mainTag != null) {
                    mainTag.remove("PowerToolKnot");
                    if (mainTag.isEmpty()) {
                        held.setTag(null);
                    }
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        return distanceSq < 1024.0;
    }

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.375D, (double)this.pos.getZ() + 0.5D);
        double width = (double)this.getType().getWidth() / 2.0D;
        double height = this.getType().getHeight();
        this.setBoundingBox(new AABB(this.getX() - width, this.getY(), this.getZ() - width, this.getX() + width, this.getY() + height, this.getZ() + width));
    }

    public Set<BlockPos> getConnectTo() {
        return this.entityData.get(CONNECT_TO);
    }
}
