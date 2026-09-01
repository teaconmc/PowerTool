package org.teacon.powertool.compat.jade;

import net.minecraft.resources.Identifier;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

@NonNullByDefault
public final class ItemDisplayProvider implements IBlockComponentProvider {

    public static final ItemDisplayProvider INSTANCE = new ItemDisplayProvider();
    private static final Identifier UID = VanillaUtils.modRL("item_display");

    private ItemDisplayProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof ItemDisplayBlockEntity blockEntity && !blockEntity.itemToDisplay.isEmpty()) {
            tooltip.add(IDisplayHelper.get().stripColor(blockEntity.itemToDisplay.getHoverName()));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
