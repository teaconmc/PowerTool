package org.teacon.powertool.attachment;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.network.attachment.Permission;

public class PowerToolAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPE = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PowerTool.MODID);
    
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Permission>> PERMISSION = ATTACHMENT_TYPE.register(Permission.KEY.getPath(),
            () -> AttachmentType.builder(Permission::new).build());
    
    public static void register(IEventBus bus){
        ATTACHMENT_TYPE.register(bus);
    }
}
