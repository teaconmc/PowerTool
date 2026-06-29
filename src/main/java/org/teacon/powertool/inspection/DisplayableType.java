package org.teacon.powertool.inspection;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

@MethodsReturnNonnullByDefault
public interface DisplayableType {
    Component getTypeDisplayName();

    Component getDisplayName();
}
