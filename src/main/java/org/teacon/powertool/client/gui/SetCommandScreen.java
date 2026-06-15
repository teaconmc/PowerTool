package org.teacon.powertool.client.gui;

import com.xkball.xklib.api.gui.widget.IInputWidget;
import com.xkball.xklib.api.gui.widget.ILayoutVariable;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.ListInputWidget;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.ui.widget.mc.ObjectInputBox;
import com.xkball.xklibmc.x3d.backend.b3d.gui.ComponentConverter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.item.CommandRune;
import org.teacon.powertool.item.PowerToolDataComponents;
import org.teacon.powertool.network.server.UpdateItemStackData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class SetCommandScreen extends XKLibBaseScreen {
    
    protected final ItemStack itemStack;
    protected final EquipmentSlot slot;
    protected EditBox name;
    protected EditBox input;
    protected Checkbox consume;
    protected ListInputWidget<CommandRune.DelayedCommandData, DelayedCommandInputWidget> commandList;
    public final List<CommandRune.DelayedCommandData> delayedCommands = new ArrayList<>();
    
    public SetCommandScreen(ItemStack stack, EquipmentSlot slot) {
        super(Component.translatable("powertool.setcommand.gui"));
        this.itemStack = stack;
        this.slot = slot;
        this.readData();
        this.addScreenLayer(this.createLayout());
    }
    
    protected void readData() {
        delayedCommands.clear();
        if (itemStack.has(PowerToolDataComponents.DELAYED_COMMANDS))
            delayedCommands.addAll(Objects.requireNonNull(itemStack.get(PowerToolDataComponents.DELAYED_COMMANDS)));
    }
    
    protected Widget createLayout() {
        var font = Minecraft.getInstance().font;
        var nameWrapper = WidgetWrapper.editBox(Component.translatable("powertool.setcommand.gui.name").getString(), 114514, _ -> {});
        var inputWrapper = WidgetWrapper.editBox(Component.translatable("powertool.setcommand.gui.command").getString(), 114514, _ -> {});
        this.name = (EditBox) nameWrapper.getWidget();
        this.input = (EditBox) inputWrapper.getWidget();
        this.applyInitialInputValues();
        this.commandList = new ListInputWidget<>(DelayedCommandInputWidget::new, (t, c) -> WidgetWrapper.button(ComponentConverter.toComponent(t), _ -> c.run()));
        this.commandList.setValue(this.delayedCommands);
        this.consume = Checkbox.builder(Component.translatable("powertool.setcommand.gui.consumable"), font)
                .selected(Boolean.TRUE.equals(itemStack.get(PowerToolDataComponents.CONSUME)))
                .build();
        var consumeWrapper = new WidgetWrapper(this.consume);
        var doneButton = WidgetWrapper.button(CommonComponents.GUI_DONE, _ -> this.onDone());
        var content = new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        size: 40%+72rpx 100%;
                        flex-shrink: 0;
                        """)
                .asRootStyle("""
                        .top_input_row {
                            size: 100% 20rpx;
                            flex-shrink: 0;
                            align-items: center;
                        }
                        .top_input_label {
                            size: 72rpx 100%;
                            flex-shrink: 0;
                            text-color: -1;
                            text-height: 10rpx;
                            text-align: left;
                        }
                        .top_input {
                            size: 100%-72rpx 100%;
                            flex-shrink: 0;
                        }
                        .delayed_command_list {
                            size: 100%-72rpx 45%;
                            margin-left: 72rpx;
                            border: 1rpx;
                            border-color: rgb(160,160,160);
                            flex-shrink: 0;
                        }
                        .list_input_btn {
                            size: 20rpx 20rpx;
                            flex-shrink: 0;
                            margin-left: 4rpx;
                        }
                        .list_input_rows {
                            flex-direction: column;
                            align-items: stretch;
                            justify-content: start;
                            overflow-y: scroll;
                            scrollbar-width: 8;
                            flex-grow: 1;
                            margin-top: 4rpx;
                        }
                        .list_input_row {
                            size: 100% 42rpx;
                            flex-shrink: 0;
                        }
                        .list_input_row_input {
                            size: 100% 100%;
                            flex-shrink: 0;
                        }
                        .list_input_dragging_row {
                            size: 100% 42rpx;
                            background-color: 0x5500AAFF;
                        }
                        .list_input_preview_row {
                            size: 100% 42rpx;
                            border: 1px;
                            border-color: 0x99FFFFFF;
                            background-color: 0x2200AAFF;
                            flex-shrink: 0;
                        }
                        .consume {
                            size: 100% 20rpx;
                            margin-top: 5rpx;
                            flex-shrink: 0;
                        }
                        .done {
                            size: 60% 20rpx;
                            margin-left: 20%;
                            margin-top: 5rpx;
                            flex-shrink: 0;
                        }
                        """)
                .addChild(labeledLine(IComponent.translatable("powertool.setcommand.gui.name"), nameWrapper.setCSSClassName("top_input")).setCSSClassName("top_input_row"))
                .addChild(labeledLine(IComponent.translatable("powertool.setcommand.gui.command"), inputWrapper.setCSSClassName("top_input")).setCSSClassName("top_input_row").inlineStyle("margin-top: 5rpx;"))
                .addChild(new Label(IComponent.translatable("powertool.setcommand.gui.delayed_commands")).inlineStyle("""
                        size: content 20rpx;
                        flex-shrink: 0;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(this.commandList.setCSSClassName("delayed_command_list"))
                .addChild(consumeWrapper.setCSSClassName("consume"))
                .addChild(doneButton.setCSSClassName("done"));
        return new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        size: 100% 100%;
                        align-items: center;
                        padding-top: 5%;
                        """)
                .addChild(content);
    }
    
    protected ContainerWidget labeledLine(IComponent label, Widget input) {
        return new ContainerWidget()
                .addChild(new Label(label).setCSSClassName("top_input_label"))
                .addChild(input);
    }
    
    protected void applyInitialInputValues() {
        if (this.name == null || this.input == null) {
            return;
        }
        String command = itemStack.get(PowerToolDataComponents.COMMAND);
        if (command != null) {
            setInputValue(this.input, command);
        }
        setInputValue(this.name, itemStack.getHoverName().getString());
    }
    
    protected static void setInputValue(EditBox input, String value) {
        input.setValue(value);
        input.displayPos = 0;
//        input.setCursorPosition(0);
    }
    
    public void refreshDelayedCommandsFromList() {
        this.delayedCommands.clear();
        for (var entry : this.commandList.getValue()) {
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
    
    public static class DelayedCommandInputWidget extends ContainerWidget implements IInputWidget<CommandRune.DelayedCommandData> {
        
        private final ObjectInputBox<Integer> delay;
        private final EditBox command;
        
        public DelayedCommandInputWidget(Widget removeButton) {
            var font = Minecraft.getInstance().font;
            this.delay = new ObjectInputBox<>(font, 0, 0, 0, 0, Component.translatable("powertool.setcommand.gui.tick_delay"), ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER);
            this.delay.setMaxLength(4);
            setInputValue(this.delay, "0");
            var commandWrapper = WidgetWrapper.editBox(Component.translatable("powertool.setcommand.gui.command").getString(), 114514, _ -> {});
            this.command = (EditBox) commandWrapper.getWidget();
            var delayWrapper = new WidgetWrapper(this.delay);
            delayWrapper.setUserInput(true);
            this.inlineStyle("""
                    flex-direction: column;
                    size: 100% 100%;
                    """);
            removeButton.inlineStyle("size: 20rpx 20rpx; flex-shrink: 0; margin-left: 4rpx;");
            this.addChild(delayInputLine(IComponent.translatable("powertool.setcommand.gui.tick_delay"), delayWrapper, removeButton));
            this.addChild(labeledInput(IComponent.translatable("powertool.setcommand.gui.command"), commandWrapper.inlineStyle("size: 100%-72rpx 20rpx; flex-shrink: 0;")).inlineStyle("margin-top: 2rpx;"));
        }
        
        private ContainerWidget delayInputLine(IComponent label, Widget input, Widget removeButton) {
            return new ContainerWidget()
                    .inlineStyle("""
                            size: 100% 20rpx;
                            flex-shrink: 0;
                            align-items: center;
                            """)
                    .addChild(new Label(label).inlineStyle("""
                            size: 72rpx 100%;
                            flex-shrink: 0;
                            text-color: -1;
                            text-height: 10rpx;
                            text-align: left;
                            """))
                    .addChild(new Widget().inlineStyle("width: 100%-136rpx; flex-shrink: 1;"))
                    .addChild(input.inlineStyle("size: 40rpx 20rpx; flex-shrink: 0;"))
                    .addChild(removeButton);
        }
        
        private ContainerWidget labeledInput(IComponent label, Widget input) {
            return new ContainerWidget()
                    .inlineStyle("""
                            size: 100% 20rpx;
                            flex-shrink: 0;
                            align-items: center;
                            """)
                    .addChild(new Label(label).inlineStyle("""
                            size: 72rpx 100%;
                            flex-shrink: 0;
                            text-color: -1;
                            text-height: 10rpx;
                            text-align: left;
                            """))
                    .addChild(input);
        }
        
        @Override
        public CommandRune.DelayedCommandData getValue() {
            return new CommandRune.DelayedCommandData(Objects.requireNonNullElse(this.delay.get(), 0), this.command.getValue());
        }
        
        @Override
        public void setValue(CommandRune.DelayedCommandData value) {
            if (value == null) {
                setInputValue(this.delay, "0");
                setInputValue(this.command, "");
            } else {
                setInputValue(this.delay, String.valueOf(value.delay()));
                setInputValue(this.command, value.command());
            }
        }
        
        @Override
        public DelayedCommandInputWidget bind(ILayoutVariable<CommandRune.DelayedCommandData> variable) {
            this.setValue(variable.get());
            return this;
        }
    }
}
