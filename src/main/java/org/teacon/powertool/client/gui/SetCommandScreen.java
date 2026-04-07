package org.teacon.powertool.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.client.gui.widget.DelayCommandList;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.item.CommandRune;
import org.teacon.powertool.item.PowerToolDataComponents;
import org.teacon.powertool.network.server.UpdateItemStackData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class SetCommandScreen extends Screen {
    
    protected final ItemStack itemStack;
    protected final EquipmentSlot slot;
    protected ObjectInputBox<String> name;
    protected ObjectInputBox<String> input;
    protected Checkbox consume;
    protected Button appendCommand;
    protected DelayCommandList commandList;
    protected Button closeButton;
    public final List<CommandRune.DelayedCommandData> delayedCommands = new ArrayList<>();
    
    public SetCommandScreen(ItemStack stack, EquipmentSlot slot) {
        super(Component.translatable("powertool.setcommand.gui"));
        this.itemStack = stack;
        this.slot = slot;
    }
    
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        var box_l = (int) Math.max(100, width * 0.4);
        var startY = (int) (height * 0.05);
        delayedCommands.clear();
        if (itemStack.has(PowerToolDataComponents.DELAYED_COMMANDS))
            delayedCommands.addAll(Objects.requireNonNull(itemStack.get(PowerToolDataComponents.DELAYED_COMMANDS)));
        this.name = new ObjectInputBox<>(font, width / 2 - box_l / 2, startY, box_l, 20, Component.literal("name"), ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
        this.name.setMaxLength(114514);
        this.name.setRenderState(false);
        this.input = new ObjectInputBox<>(font, width / 2 - box_l / 2, startY + 25, box_l, 20, Component.literal("command"), ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
        this.input.setMaxLength(114514);
        this.input.setRenderState(false);
        this.appendCommand = Button.builder(Component.literal("+"), (b) -> {
            if (this.commandList != null) commandList.appendEntry();
            this.refreshContentPos();
        }).size(20, 20).pos(width / 2 + box_l / 2 - 25, startY + 52).build();
        this.commandList = new DelayCommandList(this, box_l, startY + 50);
        var listEndY = startY + 50 + commandList.getHeight_();
        this.consume = Checkbox.builder(Component.literal("consumable"), font).pos(width / 2 - box_l / 2, listEndY + 5).selected(Boolean.TRUE.equals(itemStack.get(PowerToolDataComponents.CONSUME))).build();
        this.closeButton = new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos((int) (this.width / 2f - box_l * 0.3f), listEndY + 30)
                .size((int) (box_l * 0.6), 20).build();
        
        String command = itemStack.get(PowerToolDataComponents.COMMAND);
        if (command != null) {
            this.input.setValue(command);
        }
        this.name.setValue(itemStack.getHoverName().getString());
        
        this.addRenderableWidget(this.name);
        this.addRenderableWidget(this.input);
        this.addRenderableWidget(this.consume);
        this.addRenderableWidget(this.commandList);
        this.addRenderableWidget(this.appendCommand);
        this.addRenderableWidget(this.closeButton);
        super.init();
    }
    
    public void refreshContentPos() {
        var startY = (int) (height * 0.05);
        this.commandList.resize();
        var listEndY = startY + 50 + commandList.getHeight_();
        this.consume.setPosition(consume.getX(), listEndY + 5);
        this.closeButton.setPosition(closeButton.getX(), listEndY + 30);
        refreshDelayedCommandsFromList();
    }
    
    public void refreshDelayedCommandsFromList() {
        this.delayedCommands.clear();
        for (var entry : this.commandList.entries()) {
            delayedCommands.add(new CommandRune.DelayedCommandData(entry.delay(), entry.command()));
        }
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void removed() {
        if (input == null || name == null || consume == null) return;
        refreshDelayedCommandsFromList();
        var patch = DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME, Component.literal(name.getValue()));
        if (!input.getValue().isEmpty()) patch.set(PowerToolDataComponents.COMMAND.get(), input.getValue());
        patch.set(PowerToolDataComponents.CONSUME.get(), consume.selected());
        patch.set(PowerToolDataComponents.DELAYED_COMMANDS.get(), delayedCommands);
        ClientPacketDistributor.sendToServer(new UpdateItemStackData(slot, patch.build()));
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        var box_l = (int) Math.max(100, width * 0.4);
        var startY = (int) (height * 0.15);
        var text = "delayed commands";
        graphics.text(font, text, width / 2 - box_l / 2 - font.width(text) - 2, startY + 52, 0xFFFFFF);
    }
}
