package org.teacon.powertool.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.item.PowerToolItems;
import org.teacon.powertool.network.server.UpdateItemStackData;

public class SetCommandScreen extends Screen {
    
    protected final ItemStack itemStack;
    protected final EquipmentSlot slot;
    protected EditBox input;
    
    public SetCommandScreen(ItemStack stack, EquipmentSlot slot) {
        super(Component.translatable("powertool.setcommand.gui"));
        this.itemStack = stack;
        this.slot = slot;
    }
    
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
                .size(200, 20).build());
        var box_l = (int)Math.max(100,width*0.4);
        this.input = new EditBox(font,width/2-box_l/2,height/2-35,box_l,20,Component.empty());
        this.input.setMaxLength(114514);
        this.input.setFocused(false);
        this.input.setCanLoseFocus(true);
        
        String command = itemStack.get(PowerToolItems.COMMAND);
        if (command != null) {
            this.input.setValue(command);
        }
        
        this.addRenderableWidget(this.input);
        super.init();
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void removed() {
        if(input == null) return;
        var patch = DataComponentPatch.builder()
                .set(PowerToolItems.COMMAND.get(),input.getValue())
                .build();
        PacketDistributor.sendToServer(new UpdateItemStackData(slot,patch));
    }
}
