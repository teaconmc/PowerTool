package org.teacon.powertool.client.gui.observers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.block.TimeObserverBlock;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;
import org.teacon.powertool.utils.time.InWorldDailyCycleTimeSection;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GameTimeCycleObserverScreen extends Screen {
    
    private static final ResourceLocation SKY_COLOR_IMAGE = VanillaUtils.modRL("sky_color");
    private static final ResourceLocation SLIDER = VanillaUtils.modRL("slider");
    
    protected final TimeObserverBlockEntity te;
    protected ObjectInputBox<Float> startInput;
    protected ObjectInputBox<Float> endInput;
    
    public GameTimeCycleObserverScreen(TimeObserverBlockEntity te) {
        super(Component.translatable("powertool.gametime_cycle_observer.gui"));
        this.te = te;
    }
    
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
                .size(200, 20).build());
        if(te.getBlockType() != TimeObserverBlock.Type.GAME_DAILY_CYCLE){
            this.addRenderableWidget(new StringWidget(Component.translatable("powertool.gui.error_and_close"),font).alignCenter());
        }
        else {
            var box_l = (int)Math.max(100,width*0.2);
            this.startInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-60,box_l,20,Component.literal("Start Time: "),ObjectInputBox.NORMALIZED_FLOAT_VALIDATOR,ObjectInputBox.FLOAT_RESPONDER);
            this.startInput.setMaxLength(114);
            this.startInput.setValue("0.0");
            this.endInput = new ObjectInputBox<>(font,width/2-box_l/2,height/2-35,box_l,20,Component.literal("End Time: "),ObjectInputBox.NORMALIZED_FLOAT_VALIDATOR,ObjectInputBox.FLOAT_RESPONDER);
            this.endInput.setMaxLength(114);
            this.endInput.setValue("0.0");
            
            var timeSection = te.getTimeSection() instanceof InWorldDailyCycleTimeSection ? (InWorldDailyCycleTimeSection) te.getTimeSection() : null;
            if(timeSection != null){
                this.startInput.setValue(String.valueOf(timeSection.getStart()));
                this.endInput.setValue(String.valueOf(timeSection.getEnd()));
            }
            this.addRenderableWidget(this.startInput);
            this.addRenderableWidget(this.endInput);
            
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
        if( startInput == null || endInput == null) return;
        var start = startInput.get();
        var end = endInput.get();
        if( start == null || end == null) return;
        te.setTimeSection(new InWorldDailyCycleTimeSection(() -> Minecraft.getInstance().level,start,end));
        PacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        var textColor = VanillaUtils.getColor(255,255,255,255);
        guiGraphics.blitSprite(SKY_COLOR_IMAGE, (int) (width/2f-width*0.2f),height/2-110, (int) (width*0.4f),25);
        var text = Component.literal("Sky Color During Time Of Day: ");
        guiGraphics.drawString(font,text,(int) (width/2f-width*0.2f-font.width(text)),height/2-108,textColor);
        guiGraphics.hLine((int) (width/2f-width*0.2f),(int) (width/2f+width*0.2f-1),height/2-85,textColor);
        guiGraphics.drawString(font,"0.0", (int) (width/2f-width*0.2f-font.width("0.0")/2f),height/2-93,textColor);
        guiGraphics.drawString(font,"1.0", (int) (width/2f+width*0.2f-font.width("1.0")/2f),height/2-93,textColor);
        if(startInput == null || endInput == null) return;
        var start = startInput.get();
        var end = endInput.get();
        var level = Minecraft.getInstance().level;
        if(start == null || end == null || level == null) return;
        var current = level.getTimeOfDay(0);
        guiGraphics.blitSprite(SLIDER, (int) (width/2f-width*0.2f+width*0.4f*current-4),height/2-85,8,16);
        guiGraphics.drawString(font,"current",(int) (width/2f-width*0.2f+width*0.4f*current-font.width("current")/2f),height/2-70,0x0000FFF0);
        guiGraphics.blitSprite(SLIDER, (int) (width/2f-width*0.2f+width*0.4f*start-4),height/2-85,8,16);
        guiGraphics.blitSprite(SLIDER, (int) (width/2f-width*0.2f+width*0.4f*end-4),height/2-85,8,16);
        guiGraphics.drawString(font,"1",(int) (width/2f-width*0.2f+width*0.4f*start-font.width("1")/2f),height/2-79,0x0000FFF0);
        guiGraphics.drawString(font,"2",(int) (width/2f-width*0.2f+width*0.4f*end-font.width("2")/2f),height/2-79,0x0000FFF0);
    }
}
