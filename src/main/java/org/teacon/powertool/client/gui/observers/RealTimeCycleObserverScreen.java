package org.teacon.powertool.client.gui.observers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.block.TimeObserverBlock;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;
import org.teacon.powertool.utils.time.DailyCycleTimeSection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.time.ZoneId;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RealTimeCycleObserverScreen extends Screen {
    
    protected final TimeObserverBlockEntity te;
    protected ObjectInputBox<Integer> offsetInput;
    protected ObjectInputBox<Integer> startInput;
    protected ObjectInputBox<Integer> endInput;
    
    
    public RealTimeCycleObserverScreen(TimeObserverBlockEntity te) {
        super(Component.translatable("powertool.realtime_cycle_observer.gui"));
        this.te = te;
    }
    
    //todo 把Component.literal都换成translatable
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
                .size(200, 20).build());
        if(te.getBlockType() != TimeObserverBlock.Type.REAL_DAILY_CYCLE){
            this.addRenderableWidget(new StringWidget(Component.translatable("powertool.gui.error_and_close"),font).alignCenter());
        }
        else {
            var box_l = (int)Math.max(100,width*0.2);
            this.offsetInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-85,box_l,20,Component.literal("Time Zone Offset(Hours): "),
                    ObjectInputBox.INT_VALIDATOR.and((str) -> Math.abs(Integer.parseInt(str)) <= 18),ObjectInputBox.INT_RESPONDER);
            this.offsetInput.setMaxLength(114);
            this.offsetInput.setValue("0");
            
            var buttonUTC = new SpriteIconButton.Builder(Component.empty(),btn -> offsetInput.setValue("0"),true)
                    .size(20,20)
                    .sprite(VanillaUtils.modRL("utc_time"),16,16)
                    .build();
            buttonUTC.setPosition(width/2+box_l/2,height/2-85);
            buttonUTC.setTooltip(Tooltip.create(Component.translatable("powertool.gui.button.utc_time")));
            var localHoursOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds()/3600;
            var buttonLocal = new SpriteIconButton.Builder(Component.empty(),btn -> offsetInput.setValue(String.valueOf(localHoursOffset)),true)
                    .size(20,20)
                    .sprite(VanillaUtils.modRL("local_time"),16,16)
                    .build();
            buttonLocal.setPosition(width/2+box_l/2+20,height/2-85);
            buttonLocal.setTooltip(Tooltip.create(Component.translatable("powertool.gui.button.local_time")));
            
            this.startInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-60,box_l,20,Component.literal("Start Time: "),
                    ObjectInputBox.LOCAL_TIME_VALIDATOR,ObjectInputBox.INT_RESPONDER);
            this.startInput.setMaxLength(114);
            this.startInput.setValue("000000000");
            this.endInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-35,box_l,20,Component.literal("End Time: "),
                    ObjectInputBox.LOCAL_TIME_VALIDATOR,ObjectInputBox.INT_RESPONDER);
            this.endInput.setMaxLength(114);
            this.endInput.setValue("000000000");
            
            var timeSection = te.getTimeSection() instanceof DailyCycleTimeSection ? (DailyCycleTimeSection) te.getTimeSection() : null;
            if(timeSection != null){
                this.offsetInput.setValue(String.valueOf(timeSection.getHourOffset()));
                this.startInput.setValue(fixDefaultInput(timeSection.getStart()));
                this.endInput.setValue(fixDefaultInput(timeSection.getEnd()));
            }
            
            this.addRenderableWidget(offsetInput);
            this.addRenderableWidget(buttonUTC);
            this.addRenderableWidget(buttonLocal);
            this.addRenderableWidget(startInput);
            this.addRenderableWidget(endInput);
        }
        super.init();
    }
    
    private static String fixDefaultInput(int input){
        var str = String.valueOf(input);
        if(str.length() > 9) return "000000000";
        if(str.length() == 9) return str;
        return "0".repeat(9 - str.length()) + str;
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void removed() {
        if(offsetInput == null || startInput == null || endInput == null) return;
        var offset = offsetInput.get();
        var start = startInput.get();
        var end = endInput.get();
        if(offset == null || start == null || end == null) return;
        te.setTimeSection(new DailyCycleTimeSection(start,end,offset));
        PacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(startInput == null || endInput == null) return;
        var start = startInput.get();
        var end = endInput.get();
        var textColor = VanillaUtils.getColor(255,255,255,255);
        var box_l = (int)Math.max(100,width*0.2);
        if(start != null){
            var startTime = DailyCycleTimeSection.fromFormatedInt(start);
            guiGraphics.drawString(font,startTime.toString(),width/2+box_l/2,height/2-50,textColor);
        }
        if(end != null){
            var endTime = DailyCycleTimeSection.fromFormatedInt(end);
            guiGraphics.drawString(font,endTime.toString(),width/2+box_l/2,height/2-25,textColor);
        }
    }
}
