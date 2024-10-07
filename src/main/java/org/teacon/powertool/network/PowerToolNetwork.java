package org.teacon.powertool.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.network.client.*;
import org.teacon.powertool.network.server.SetCommandBlockPacket;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.network.server.UpdateItemStackData;
import org.teacon.powertool.network.server.UpdatePowerSupplyData;

@EventBusSubscriber(modid = PowerTool.MODID,bus = EventBusSubscriber.Bus.MOD)
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
