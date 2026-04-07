package org.teacon.powertool.client.gui;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.client.gui.widget.Vector3fList;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BezierCurveBlockScreen extends Screen {
    
    public final BezierCurveBlockEntity te;
    protected Button append;
    protected Vector3fList vector3fList;
    protected ObjectInputBox<Integer> stepInput;
    protected ObjectInputBox<Integer> sideCountInput;
    protected ObjectInputBox<Float> radiusInput;
    protected ObjectInputBox<Integer> uScaleInput;
    protected ObjectInputBox<Integer> vScaleInput;
    protected ObjectInputBox<Integer> colorInput;
    protected ObjectInputBox<Identifier> textureInput;
    protected Checkbox useClampMode;
    protected Checkbox useWorldCoordinate;
    
    
    public BezierCurveBlockScreen(BezierCurveBlockEntity te) {
        super(Component.literal("BezierCurveBlock"));
        this.te = te;
    }
    
    @Override
    protected void init() {
        super.init();
        var startY = (int) (height * 0.05);
        var startX = (int) (width * 0.2);
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(startX, (int) Math.max(startY * 2 + 20 + 25 * 9, this.height * 0.8 + startY))
                .size(startX, 20).build());
        
        this.stepInput = new ObjectInputBox<>(font, startX, startY * 2 + 20, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.step"), ObjectInputBox.INT_VALIDATOR.and(str -> {
            var i = Integer.parseInt(str);
            return i >= 2 && i < 2000;
        }), ObjectInputBox.INT_RESPONDER);
        this.stepInput.setMaxLength(14);
        this.stepInput.setValue(String.valueOf(Math.max(te.steps, 2)));
        this.addRenderableWidget(this.stepInput);
        this.sideCountInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.sides"), ObjectInputBox.INT_VALIDATOR.and(str -> Integer.parseInt(str) >= 3), ObjectInputBox.INT_RESPONDER);
        this.sideCountInput.setMaxLength(14);
        this.sideCountInput.setValue(String.valueOf(Math.max(te.sideCount, 3)));
        this.addRenderableWidget(this.sideCountInput);
        this.radiusInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25 * 2, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.radius"), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
        this.radiusInput.setMaxLength(14);
        this.radiusInput.setValue(String.valueOf(te.radius));
        this.addRenderableWidget(this.radiusInput);
        this.textureInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25 * 3, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.texture"), ObjectInputBox.TEXTURE_VALIDATOR, ObjectInputBox.TEXTURE_RESPONDER);
        this.textureInput.setMaxLength(1000);
        this.textureInput.setValue(te.texture.toString());
        this.addRenderableWidget(this.textureInput);
        this.uScaleInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25 * 4, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.uScale"), ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER);
        this.uScaleInput.setMaxLength(14);
        this.uScaleInput.setValue(String.valueOf(te.uScale));
        this.addRenderableWidget(this.uScaleInput);
        this.vScaleInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25 * 5, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.vScale"), ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER);
        this.vScaleInput.setMaxLength(14);
        this.vScaleInput.setValue(String.valueOf(te.vScale));
        this.addRenderableWidget(this.vScaleInput);
        this.colorInput = new ObjectInputBox<>(font, startX, startY * 2 + 20 + 25 * 6, (int) (width * 0.25), 20, Component.translatable("powertool.gui.bezier_curve.color"), ObjectInputBox.RGB_COLOR_VALIDATOR, ObjectInputBox.RGB_COLOR_RESPONDER);
        this.colorInput.setMaxLength(14);
        this.colorInput.setValue(VanillaUtils.hexColorFromInt(te.color));
        this.addRenderableWidget(this.colorInput);
        this.useClampMode = Checkbox.builder(Component.translatable("powertool.gui.bezier_curve.use_clamp_mode"), font)
                .tooltip(Tooltip.create(Component.translatable("powertool.gui.bezier_curve.use_clamp_mode.tooltip")))
                .pos(startX, startY * 2 + 20 + 25 * 7)
                .selected(te.clampMode)
                .build();
        this.addRenderableWidget(this.useClampMode);
        this.useWorldCoordinate = Checkbox.builder(Component.translatable("powertool.gui.bezier_curve.use_world_coordinate"), font)
                .tooltip(Tooltip.create(Component.translatable("powertool.gui.bezier_curve.use_world_coordinate.tooltip")))
                .pos(startX, startY * 2 + 20 + 25 * 8)
                .selected(te.worldCoordinate)
                .build();
        this.addRenderableWidget(this.useWorldCoordinate);
        this.append = Button.builder(Component.literal("+"), (b) -> {
            if (this.vector3fList != null) vector3fList.appendEntry();
        }).size(20, 20).pos((int) (width * 0.95 - 25), startY + 20).build();
        this.vector3fList = new Vector3fList(this, (int) (height * 0.8), startY + 20);
        this.addRenderableWidget(vector3fList);
        this.addRenderableWidget(append);
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void removed() {
        if (this.vector3fList == null) return;
        te.steps = Objects.requireNonNullElse(stepInput.get(), 2);
        te.sideCount = Objects.requireNonNullElse(sideCountInput.get(), 3);
        te.radius = Objects.requireNonNullElse(radiusInput.get(), 0f);
        te.texture = Objects.requireNonNullElse(textureInput.get(), VanillaUtils.MISSING_TEXTURE);
        te.uScale = Objects.requireNonNullElse(uScaleInput.get(), 1);
        te.vScale = Objects.requireNonNullElse(vScaleInput.get(), 1);
        te.color = Objects.requireNonNullElse(colorInput.get(), -1);
        te.clampMode = useClampMode.selected();
        te.worldCoordinate = useWorldCoordinate.selected();
        var points = vector3fList.entries().stream().map(Vector3fList.Entry::getResult).toList();
        te.setControlPoints(points);
        ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, a);
        var s1 = Component.translatable("powertool.gui.bezier_curve.control_points");
        var s2 = Component.translatable("powertool.gui.bezier_curve.control_points_warn");
        var startY = (int) (height * 0.05);
        guiGraphics.text(font, s1, (int) (width * 0.55) + 5, startY + 20 + 5, -1);
        guiGraphics.text(font, s2, (int) (width * 0.55) + 5, (int) (startY + 20 + height * 0.8) + 5, -1);
        if (te.bezierCurve != null)
            guiGraphics.text(font, "length: " + te.bezierCurve.getLength(), (int) (width * 0.2), startY * 2 + 20 + 25 * 9, -1);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
