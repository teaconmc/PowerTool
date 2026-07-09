package org.teacon.powertool.entity.exhibit;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.exhibition.ExhibitionNodeManager;
import org.teacon.powertool.exhibition.node.EntityNode;
import org.teacon.powertool.exhibition.node.ExhibitionNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ExhibitionEntity extends PathfinderMob {

    private static final EntityDataAccessor<ExhibitionNodeManager> DATA_NODE;

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
        this.entityData.set(DATA_NODE, root);

    }

    protected void onCreateExhibitionNode(Consumer<ExhibitionNode> consumer) {
        consumer.accept(EntityNode.of(this));
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
    protected void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.store("exhibition_node", ExhibitionNodeManager.CODEC, this.getExhibitionNode());
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);

        final var node = input.read("exhibition_node", ExhibitionNodeManager.CODEC);
        if (node.isPresent()) {
            this.entityData.set(DATA_NODE, node.get());
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
    public void aiStep() {}

    static {
        DATA_NODE = SynchedEntityData.defineId(ExhibitionEntity.class, PowerToolEntities.EXHIBITION_NODE.get());
    }

}
