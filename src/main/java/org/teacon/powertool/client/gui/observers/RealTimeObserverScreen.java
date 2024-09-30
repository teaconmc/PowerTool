package org.teacon.powertool.client.gui.observers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.block.TimeObserverBlock;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;
import org.teacon.powertool.utils.time.TimestampTimeSection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RealTimeObserverScreen extends Screen {
    
    protected final TimeObserverBlockEntity te;
    protected ObjectInputBox<Long> startTimeInput;
    protected ObjectInputBox<Long> endTimeInput;
    
    public RealTimeObserverScreen(TimeObserverBlockEntity te) {
        super(Component.translatable("powertool.realtime_observer.gui"));
        this.te = te;
    }
    
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
                .size(200, 20).build());
        if(te.getBlockType() != TimeObserverBlock.Type.REAL_TIME){
            this.addRenderableWidget(new StringWidget(Component.translatable("powertool.gui.error_and_close"),font).alignCenter());
        }
        else {
            var box_l = (int)Math.max(150,width*0.3);
            var timeSection = te.getTimeSection() instanceof TimestampTimeSection ? (TimestampTimeSection) te.getTimeSection() : null;
            this.startTimeInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-60,box_l,20,Component.empty(),ObjectInputBox.TIMESTAMP_VALIDATOR,ObjectInputBox.LONG_RESPONDER);
            this.startTimeInput.setMaxLength(114);
            this.endTimeInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-35,box_l,20,Component.empty(),ObjectInputBox.TIMESTAMP_VALIDATOR,ObjectInputBox.LONG_RESPONDER);
            this.endTimeInput.setMaxLength(114);
            if(timeSection != null){
                this.startTimeInput.setValue(String.valueOf(timeSection.start()));
                this.endTimeInput.setValue(String.valueOf(timeSection.end()));
            }
            this.addRenderableWidget(this.startTimeInput);
            this.addRenderableWidget(this.endTimeInput);
        }
        super.init();
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
        if(startTimeInput == null || endTimeInput == null) return;
        var start = startTimeInput.get();
        var end = endTimeInput.get();
        if(start == null || end == null) return;
        te.setTimeSection(new TimestampTimeSection(start,end));
        PacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(startTimeInput == null || endTimeInput == null) return;
        var time = System.currentTimeMillis();
        var textColor = VanillaUtils.getColor(255,255,255,255);
        var text = Component.translatable("powertool.realtime_observer.gui.current_time",time);
        guiGraphics.drawString(font,text,width/2 - font.width(text)/2,height/2-80, textColor);
        var box_l = (int)Math.max(150,width*0.3);
        var startText = "Start Time:";
        var endText = "End Time:";
        guiGraphics.drawString(font,startText,width/2-box_l/2-font.width(startText)-12,height/2-58, textColor);
        guiGraphics.drawString(font,endText,width/2-box_l/2-font.width(endText)-12,height/2-33, textColor);
        var start = startTimeInput.get();
        var end = endTimeInput.get();
        if(start != null) guiGraphics.drawString(font, "UTC+0 "+LocalDateTime.ofInstant(Instant.ofEpochMilli(start),ZoneOffset.UTC), width/2+box_l/2,height/2-50, textColor);
        if(end != null) guiGraphics.drawString(font, "UTC+0 "+LocalDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneOffset.UTC), width/2+box_l/2,height/2-25, textColor);
    }
}
