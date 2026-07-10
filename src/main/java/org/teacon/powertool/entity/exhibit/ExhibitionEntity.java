package org.teacon.powertool.entity.exhibit;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.exhibition.ExhibitionNodeManager;
import org.teacon.powertool.exhibition.node.CommandNode;
import org.teacon.powertool.exhibition.node.EntityNode;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.exhibition.node.InteractNode;
import org.teacon.powertool.item.PowerToolDataComponents;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ExhibitionEntity extends PathfinderMob {

    protected static final EntityDataAccessor<ExhibitionNodeManager> DATA_NODE;
    private static final Logger log = LoggerFactory.getLogger(ExhibitionEntity.class);

    protected ExhibitionEntity(
            final EntityType<? extends ExhibitionEntity>    type,
            final Level                                     level
    ) {
        super(type, level);
        this.createExhibitionNode();
    }

    public ExhibitionNodeManager getExhibitionNode() {
        return this.entityData.get(DATA_NODE);
    }

    private void createExhibitionNode() {

        var list = new ArrayList<ExhibitionNode>();
        onCreateExhibitionNode(list::add);

        var root = new ExhibitionNodeManager(list);
        root.setup(this);

        this.entityData.set(DATA_NODE, root);

    }

    @Contract(pure = true)
    protected void onCreateExhibitionNode(Consumer<ExhibitionNode> consumer) {
        consumer.accept(EntityNode.of(this));
        consumer.accept(new InteractNode());
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void push(final double xa, final double ya, final double za) {}

    @Override
    public boolean hurtServer(
            final ServerLevel level,
            final DamageSource source,
            final float damage
    ) {
        final var success = source.is(DamageTypes.GENERIC_KILL);
        if (success) {
            this.setRemoved(RemovalReason.KILLED);
            return true;
        }

        if (source.is(DamageTypes.PLAYER_ATTACK) && source.isDirect()) {
            final var node = this.getExhibitionNode()
                    .getUnique(InteractNode.UNIQUE_KEY);

            final var left = node.getLeft();
            left.invoke(this, ((Player) source.getEntity()));
        };

        return false;
    }

    @Override
    public boolean hurtClient(final DamageSource source) {
        final var success = source.is(DamageTypes.GENERIC_KILL);
        if (success) {
            this.setRemoved(RemovalReason.KILLED);
            return true;
        }

        return false;
    }

    @Override
    protected InteractionResult mobInteract(
            final Player player,
            final InteractionHand hand
    ) {

        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        final var node = this.getExhibitionNode()
                        .getUnique(InteractNode.UNIQUE_KEY);

        final var right = node.getRight();
        if (right.invoke(this, player)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.store("exhibition_node", ExhibitionNodeManager.CODEC, this.getExhibitionNode());
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);

        final var node = input.read("exhibition_node", ExhibitionNodeManager.CODEC);
        if (node.isPresent()) {
            final var manager = node.get();
            manager.setup(this);
            this.entityData.set(DATA_NODE, manager);
        } else {
            this.createExhibitionNode();
        }
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NODE, new ExhibitionNodeManager(List.of()));
    }

    @Override
    public void onSyncedDataUpdated(final EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);

        if (DATA_NODE.equals(accessor)) {
            // debug output
            log.debug("Exhibition node updated: {}", this.getStringUUID());
            this.getExhibitionNode().apply(this);
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        final var result = super.getPickResult();
        if (result != null) {
            result.set(PowerToolDataComponents.EXHIBITION_NODES, this.getExhibitionNode().toImmutable());
            result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return result;
    }

    @Override
    public void aiStep() {
    }

    static {
        DATA_NODE = SynchedEntityData.defineId(ExhibitionEntity.class, PowerToolEntities.EXHIBITION_NODE.get());
    }

    public void update(final ExhibitionNodeManager manager) {
        final var node = this.getExhibitionNode();
        node.paste(manager);
        this.entityData.set(DATA_NODE, node, true);
    }

    public void init(final ExhibitionNodeManager.Immutable manager) {
        final var node = this.getExhibitionNode();
        node.paste(manager);
        node.setup(this);
        this.entityData.set(DATA_NODE, node, true);
    }
}
