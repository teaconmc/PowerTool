package org.teacon.powertool.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.network.client.OpenBlockScreen;
import org.teacon.powertool.network.client.OpenHolographicSignEditor;
import org.teacon.powertool.network.client.OpenItemScreen;
import org.teacon.powertool.network.client.RecordDebugData;
import org.teacon.powertool.network.client.UpdateCachedModeChunkDataPacket;
import org.teacon.powertool.network.client.UpdateCreativeNoClipPacket;
import org.teacon.powertool.network.client.UpdateDisplayChunkDataPacket;
import org.teacon.powertool.network.client.UpdateFlyNoDriftPacket;
import org.teacon.powertool.network.client.UpdateOpenMenuSourcePacket;
import org.teacon.powertool.network.client.UpdatePermissionPacket;
import org.teacon.powertool.network.client.UpdatePlayerMovement;
import org.teacon.powertool.network.client.UpdateStaticModeChunkDataPacket;
import org.teacon.powertool.network.server.SetCommandBlockPacket;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.network.server.UpdateItemStackData;
import org.teacon.powertool.network.server.UpdatePowerSupplyData;

@NonNullByDefault
@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolNetwork {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var register = event.registrar(PowerTool.MODID);
        
        register.commonToClient(
                UpdatePermissionPacket.TYPE,
                UpdatePermissionPacket.STREAM_CODEC,
                UpdatePermissionPacket::handle
        );
        register.commonToClient(
                OpenHolographicSignEditor.TYPE,
                OpenHolographicSignEditor.STREAM_CODEC,
                OpenHolographicSignEditor::handle
        );
        register.commonToClient(
                UpdateCreativeNoClipPacket.TYPE,
                UpdateCreativeNoClipPacket.STREAM_CODEC,
                UpdateCreativeNoClipPacket::handle
        );
        register.playToClient(
                UpdateFlyNoDriftPacket.TYPE,
                UpdateFlyNoDriftPacket.STREAM_CODEC,
                UpdateFlyNoDriftPacket::handle
        );
        register.playToClient(
                OpenItemScreen.TYPE,
                OpenItemScreen.STREAM_CODEC,
                OpenItemScreen::handle
        );
        register.playToClient(
                OpenBlockScreen.TYPE,
                OpenBlockScreen.STREAM_CODEC,
                OpenBlockScreen::handle
        );
        register.playToClient(
                UpdatePlayerMovement.TYPE,
                UpdatePlayerMovement.STREAM_CODEC,
                UpdatePlayerMovement::handle
        );
        register.playToClient(
                UpdateOpenMenuSourcePacket.TYPE,
                UpdateOpenMenuSourcePacket.STREAM_CODEC,
                UpdateOpenMenuSourcePacket::handle
        );
        register.playToClient(
                UpdateDisplayChunkDataPacket.TYPE,
                UpdateDisplayChunkDataPacket.STREAM_CODEC,
                UpdateDisplayChunkDataPacket::handle
        );
        register.playToClient(
                UpdateCachedModeChunkDataPacket.TYPE,
                UpdateCachedModeChunkDataPacket.STREAM_CODEC,
                UpdateCachedModeChunkDataPacket::handle
        );
        register.playToClient(
                RecordDebugData.TYPE,
                RecordDebugData.STREAM_CODEC,
                RecordDebugData::handle
        );
        register.playToClient(
                UpdateStaticModeChunkDataPacket.TYPE,
                UpdateStaticModeChunkDataPacket.STREAM_CODEC,
                UpdateStaticModeChunkDataPacket::handle
        );
        register.commonToServer(
                SetCommandBlockPacket.TYPE,
                SetCommandBlockPacket.STREAM_CODEC,
                SetCommandBlockPacket::handle
        );
        register.commonToServer(
                UpdatePowerSupplyData.TYPE,
                UpdatePowerSupplyData.STREAM_CODEC,
                UpdatePowerSupplyData::handle
        );
        register.commonToServer(
                UpdateBlockEntityData.TYPE,
                UpdateBlockEntityData.STREAM_CODEC,
                UpdateBlockEntityData::handle
        );
        register.playToServer(
                UpdateItemStackData.TYPE,
                UpdateItemStackData.STREAM_CODEC,
                UpdateItemStackData::handle
        );
    }
    
}
