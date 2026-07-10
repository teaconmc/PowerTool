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
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import com.xkball.xklibmc.ui.widget.mc.ObjectInputBox;
import com.xkball.xklibmc.x3d.backend.b3d.gui.ComponentConverter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@NonNullByDefault
public class BezierCurveBlockScreen extends XKLibBaseScreen {
    
    public final BezierCurveBlockEntity te;
    protected @Nullable ListInputWidget<Vector3f, ControlPointInputWidget> controlPointList;
    protected @Nullable ObjectInputWidget<Integer> stepInput;
    protected @Nullable ObjectInputWidget<Integer> sideCountInput;
    protected @Nullable ObjectInputWidget<Float> radiusInput;
    protected @Nullable ObjectInputWidget<Integer> uScaleInput;
    protected @Nullable ObjectInputWidget<Integer> vScaleInput;
    protected @Nullable ObjectInputWidget<Integer> colorInput;
    protected @Nullable ObjectInputWidget<Identifier> textureInput;
    protected @Nullable Checkbox useWorldCoordinate;
    
    
    public BezierCurveBlockScreen(BezierCurveBlockEntity te) {
        super(Component.literal("BezierCurveBlock"));
        this.te = te;
        this.addScreenLayer(this.createLayout());
    }
    
    protected Widget createLayout() {
        var font = Minecraft.getInstance().font;
        this.stepInput = inputWidget(ObjectInputBox.INT_VALIDATOR.and(str -> {
            var i = Integer.parseInt(str);
            return i >= 2 && i < 2000;
        }), ObjectInputBox.INT_RESPONDER, String.valueOf(Math.max(te.steps, 2)));
        this.sideCountInput = inputWidget(ObjectInputBox.INT_VALIDATOR.and(str -> Integer.parseInt(str) >= 3),
                ObjectInputBox.INT_RESPONDER, String.valueOf(Math.max(te.sideCount, 3)));
        this.radiusInput = inputWidget(ObjectInputBox.FLOAT_VALIDATOR,
                ObjectInputBox.FLOAT_RESPONDER, String.valueOf(te.radius));
        this.textureInput = inputWidget(ObjectInputBox.TEXTURE_VALIDATOR,
                ObjectInputBox.TEXTURE_RESPONDER, te.texture.toString());
        this.uScaleInput = inputWidget(ObjectInputBox.INT_VALIDATOR,
                ObjectInputBox.INT_RESPONDER, String.valueOf(te.uScale));
        this.vScaleInput = inputWidget(ObjectInputBox.INT_VALIDATOR,
                ObjectInputBox.INT_RESPONDER, String.valueOf(te.vScale));
        this.colorInput = inputWidget(ObjectInputBox.RGB_COLOR_VALIDATOR,
                ObjectInputBox.RGB_COLOR_RESPONDER, VanillaUtils.hexColorFromInt(te.color));
        this.useWorldCoordinate = Checkbox.builder(Component.translatable("powertool.gui.bezier_curve.use_world_coordinate"), font)
                .tooltip(Tooltip.create(Component.translatable("powertool.gui.bezier_curve.use_world_coordinate.tooltip")))
                .selected(te.worldCoordinate)
                .build();
        var doneButton = WidgetWrapper.button(CommonComponents.GUI_DONE, _ -> this.onDone());
        var leftPanel = new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        overflow-y: scroll;
                        scrollbar-width: 8;
                        size: 40% 100%;
                        margin-left: 5%;
                        margin-right: 5%;
                        justify-content: center;
                        flex-shrink: 0;
                        """)
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.step"), this.stepInput).inlineStyle("margin-top: 20rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.sides"), this.sideCountInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.radius"), this.radiusInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.texture"), this.textureInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.uScale"), this.uScaleInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.vScale"), this.vScaleInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(labeledInput(IComponent.translatable("powertool.gui.bezier_curve.color"), this.colorInput).inlineStyle("margin-top: 5rpx;"))
                .addChild(new WidgetWrapper(this.useWorldCoordinate).inlineStyle("size: 65% 20rpx; flex-shrink: 0; margin-top: 5rpx; margin-left: 35%;"))
                .addChild(lengthLabel().inlineStyle("""
                        size: 100% 20rpx;
                        flex-shrink: 0;
                        margin-top: 5rpx;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(doneButton.inlineStyle("""
                        size: 75% 20rpx;
                        flex-shrink: 0;
                        margin-top: 5rpx;
                        align-self: center;
                        """));
        this.controlPointList = new ListInputWidget<>(ControlPointInputWidget::new, (text, callback) -> WidgetWrapper.button(ComponentConverter.toComponent(text), _ -> callback.run()));
        this.controlPointList.setValue(te.controlPoints);
        var rightPanel = new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        size: 40% 80%;
                        margin-left: 5%;
                        align-self: center;
                        flex-shrink: 0;
                        """)
                .addChild(new Label(IComponent.translatable("powertool.gui.bezier_curve.control_points")).inlineStyle("""
                        size: 100% 20rpx;
                        flex-shrink: 0;
                        margin-top: 10rpx;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(this.controlPointList.inlineStyle("size: 100% 100%-35rpx; flex-shrink: 0;"));
        return new ContainerWidget()
                .inlineStyle("""
                        flex-direction: row;
                        size: 100% 100%;
                        """)
                .addChild(leftPanel)
                .addChild(rightPanel);
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void removed() {
        if (this.controlPointList == null || this.stepInput == null || this.sideCountInput == null || this.radiusInput == null || this.textureInput == null || this.uScaleInput == null || this.vScaleInput == null || this.colorInput == null || this.useWorldCoordinate == null) return;
        te.steps = Objects.requireNonNullElse(stepInput.getValue(), 2);
        te.sideCount = Objects.requireNonNullElse(sideCountInput.getValue(), 3);
        te.radius = Objects.requireNonNullElse(radiusInput.getValue(), 0f);
        te.texture = Objects.requireNonNullElse(textureInput.getValue(), VanillaUtils.MISSING_TEXTURE);
        te.uScale = Objects.requireNonNullElse(uScaleInput.getValue(), 1);
        te.vScale = Objects.requireNonNullElse(vScaleInput.getValue(), 1);
        te.color = Objects.requireNonNullElse(colorInput.getValue(), -1);
        te.worldCoordinate = useWorldCoordinate.selected();
        te.setControlPoints(controlPointList.getValue());
        ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    private Label lengthLabel() {
        if (te.bezierCurve == null) {
            return new Label(IComponent.literal(""));
        }
        return new Label(IComponent.literal("length: " + te.bezierCurve.getLength()));
    }
    
    private static <T> ObjectInputWidget<T> inputWidget(Predicate<String> validator, Function<String, T> parser, String value) {
        var input = new ObjectInputWidget<>(validator, parser);
        input.setAsString(value);
        return input;
    }
    
    private static ContainerWidget labeledInput(IComponent label, ObjectInputWidget<?> input) {
        return new ContainerWidget()
                .inlineStyle("""
                        size: 100% 20rpx;
                        flex-shrink: 0;
                        align-items: center;
                        """)
                .addChild(new Label(label).inlineStyle("""
                        size: 35% 100%;
                        flex-shrink: 0;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(input.inlineStyle("size: 65% 100%; flex-shrink: 0;"));
    }
    
    public static class ControlPointInputWidget extends ContainerWidget implements IInputWidget<Vector3f> {
        
        private final ObjectInputWidget<Float> x;
        private final ObjectInputWidget<Float> y;
        private final ObjectInputWidget<Float> z;
        
        public ControlPointInputWidget(Widget removeButton) {
            this.x = createInput();
            this.y = createInput();
            this.z = createInput();
            this.inlineStyle("""
                    flex-direction: row;
                    align-items: center;
                    justify-content: space-between;
                    size: 100% 100%;
                    """)
                    .addChild(pointInput(IComponent.literal("X:"), this.x))
                    .addChild(pointInput(IComponent.literal("Y:"), this.y))
                    .addChild(pointInput(IComponent.literal("Z:"), this.z))
                    .addChild(removeButton);
        }
        
        private static ObjectInputWidget<Float> createInput() {
            var input = new ObjectInputWidget<>(ObjectInputBox.FLOAT_VALIDATOR,
                    ObjectInputBox.FLOAT_RESPONDER);
            input.setAsString("0");
            return input;
        }
        
        private static ContainerWidget pointInput(IComponent label, ObjectInputWidget<Float> input) {
            return new ContainerWidget()
                    .inlineStyle("""
                            flex-direction: row;
                            align-items: center;
                            size: 29% 20rpx;
                            flex-shrink: 0;
                            """)
                    .addChild(new Label(label).inlineStyle("""
                            size: 12rpx 100%;
                            flex-shrink: 0;
                            text-color: -1;
                            text-height: 10rpx;
                            text-align: left;
                            """))
                    .addChild(input.inlineStyle("size: 100%-12rpx 100%; flex-shrink: 0;"));
        }
        
        @Override
        public Vector3f getValue() {
            var x = Objects.requireNonNullElse(this.x.getValue(), 0f);
            var y = Objects.requireNonNullElse(this.y.getValue(), 0f);
            var z = Objects.requireNonNullElse(this.z.getValue(), 0f);
            return new Vector3f(x, y, z);
        }
        
        @Override
        public void setValue(@Nullable Vector3f value) {
            var point = Objects.requireNonNullElseGet(value, Vector3f::new);
            this.x.setAsString(String.valueOf(point.x()));
            this.y.setAsString(String.valueOf(point.y()));
            this.z.setAsString(String.valueOf(point.z()));
        }
        
        @Override
        public ControlPointInputWidget bind(ILayoutVariable<Vector3f> variable) {
            this.setValue(variable.get());
            return this;
        }
    }
}
