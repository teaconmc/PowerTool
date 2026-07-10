package org.teacon.powertool.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.network.attachment.Permission;

import java.util.ArrayList;
import java.util.List;

@NonNullByDefault
public class PowerToolAttachments {
    
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPE = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PowerTool.MODID);
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Permission>> PERMISSION = ATTACHMENT_TYPE.register(Permission.KEY.getPath(),
            () -> AttachmentType.builder(Permission::new).build());
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> CREATIVE_NO_CLIP = ATTACHMENT_TYPE.register(
            "creative_no_clip",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("enabled"))
                    .copyOnDeath()
                    .build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<BlockPos>>> DISPLAY_MODE = ATTACHMENT_TYPE.register(
            "display_mode",
            () -> AttachmentType.<List<BlockPos>>builder(() -> new ArrayList<>())
                    .serialize(BlockPos.CODEC.listOf().fieldOf("pos_list"))
                    .build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<BlockPos>>> CACHED_MODE = ATTACHMENT_TYPE.register(
            "cached_mode",
            () -> AttachmentType.<List<BlockPos>>builder(() -> new ArrayList<>())
                    .serialize(BlockPos.CODEC.listOf().fieldOf("pos_list"))
                    .build()
    );
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<BlockPos>>> STATIC_MODE = ATTACHMENT_TYPE.register(
            "static_mode",
            () -> AttachmentType.<List<BlockPos>>builder(() -> new ArrayList<>())
                    .serialize(BlockPos.CODEC.listOf().fieldOf("pos_list"))
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<BlockPos>>> BEZIER_CURVES = ATTACHMENT_TYPE.register(
            "bezier_curves",
            () -> AttachmentType.<List<BlockPos>>builder(() -> new ArrayList<>()).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FLY_NO_DRIFT = ATTACHMENT_TYPE.register(
        "fly_no_drift",
        () -> AttachmentType.builder(() -> false)
            .serialize(Codec.BOOL.fieldOf("enabled"))
            .copyOnDeath()
            .build()
    );
    
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPE.register(bus);
    }
}
