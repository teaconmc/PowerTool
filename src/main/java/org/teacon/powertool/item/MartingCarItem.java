package org.teacon.powertool.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.entity.PowerToolEntities;

import java.util.function.Consumer;

public class MartingCarItem extends Item {
    public static final String TOOLTIP1 = "tooltip.powertool.marting";
    public static final String TOOLTIP2 = "tooltip.powertool.marting2";

    private final MartingCarEntity.Variant variant;

    public MartingCarItem(Properties properties, MartingCarEntity.Variant variant) {
        super(properties);
        this.variant = variant;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable(TOOLTIP1));
        builder.accept(Component.translatable(TOOLTIP2));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        var level = context.getLevel();
        if (!level.isClientSide()) {
            var pos = context.getClickedPos().relative(context.getClickedFace());
            var entity = new MartingCarEntity(PowerToolEntities.MARTING.get(), level);
            entity.setVariant(variant);
            entity.setPos(pos.getCenter());
            if(context.getPlayer() != null) entity.setYRot(context.getPlayer().getYRot());
            context.getItemInHand().shrink(1);
            level.addFreshEntity(entity);
        }
        return InteractionResult.SUCCESS;
    }
}
