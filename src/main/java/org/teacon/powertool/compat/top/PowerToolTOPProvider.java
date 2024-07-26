package org.teacon.powertool.compat.top;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.elements.ElementPadding;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.function.Function;

public final class PowerToolTOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {
    @Override
    public ResourceLocation getID() {
        return VanillaUtils.modResourceLocation( "the_one_probe");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData probeHitData) {
        var be = level.getBlockEntity(probeHitData.getPos());
        if (be instanceof ItemSupplierBlockEntity itemSupplier) {
            var theContent = itemSupplier.theItem.copy();
            if (!theContent.isEmpty()) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                        .item(theContent)
                        .element(new ElementPadding(6, 10))
                        .itemLabel(theContent);
            }
        }
    }

    @Override
    public Void apply(ITheOneProbe topAPI) {
        topAPI.registerProvider(this);
        return null;
    }
}
