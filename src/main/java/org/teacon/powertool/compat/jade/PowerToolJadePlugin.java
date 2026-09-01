package org.teacon.powertool.compat.jade;

import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.ItemDisplayBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@NonNullByDefault
@WailaPlugin
public final class PowerToolJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ItemDisplayProvider.INSTANCE, ItemDisplayBlock.class);
    }
}
