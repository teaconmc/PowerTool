package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.xklibmc.ui.widget.ColorInputWidget;
import com.xkball.xklibmc.ui.widget.NumberInputWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BBCodeHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.block.holo_sign.SignType;
import org.teacon.powertool.network.server.UpdateBlockEntityData;

import java.util.function.Consumer;
import java.util.function.Supplier;

@NonNullByDefault
public class BaseHolographicSignEditingScreen<T extends BaseHolographicSignBlockEntity> extends XKLibBaseScreen {
    
    private static final String layoutStr = "size: 60% 20rpx; margin-top: 2rpx; margin-bottom: 2rpx; flex-shrink: 0;";
    
    protected final T sign;
    
    protected float scale;
    protected int color;
    protected BaseHolographicSignBlockEntity.Align textAlign;
    protected boolean locked;
    protected int xRotation;
    protected int yRotation;
    protected boolean bidirectional;
    protected boolean renderBackground;
    protected boolean dropShadow;
    protected boolean lit;
    protected float xOffset;
    protected float yOffset;
    protected float zOffset;
    protected int xRotate;
    protected int yRotate;
    
    public BaseHolographicSignEditingScreen(Component title, T theSign) {
        super(title);
        this.sign = theSign;
        this.readData(theSign);
        this.addScreenLayer(XKLibBaseScreen.biPanelFrame(IComponent.literal(title.getString()), createLeftPanel(), createRightPanel()));
        this.onTextAlignChange();
    }
    
    public static Screen creatHoloSignScreen(BlockEntity sign, SignType type) {
        return switch (type) {
            case COMMON ->
                    sign instanceof CommonHolographicSignBlockEntity be ? new CommonHolographicSignEditingScreen(be) : null;
            case URL ->
                    sign instanceof LinkHolographicSignBlockEntity be ? new LinkHolographicSignEditingScreen(be) : null;
            case RAW_JSON ->
                    sign instanceof RawJsonHolographicSignBlockEntity be ? new RawJsonHolographicSignEditingScreen(be) : null;
            case BBC ->
                    sign instanceof BBCodeHolographicSignBlockEntity be ? new BBCodeHolographicSignEditingScreen(be) : null;
        };
    }
    
    protected void readData(T theSign) {
        this.color = theSign.colorInARGB;
        this.scale = theSign.scale;
        this.textAlign = theSign.align;
        this.locked = theSign.lock;
        this.yRotation = theSign.yRotate;
        this.xRotation = theSign.xRotate;
        this.bidirectional = theSign.bidirectional;
        this.renderBackground = theSign.renderBackground;
        this.dropShadow = theSign.dropShadow;
        this.lit = theSign.lit;
        this.xOffset = theSign.xOffset;
        this.yOffset = theSign.yOffset;
        this.zOffset = theSign.zOffset;
        this.xRotate = theSign.xRotate;
        this.yRotate = theSign.yRotate;
    }
    
    protected Widget createRightPanel() {
        return new Widget();
    }
    
    protected Widget createLeftPanel() {
        return new ContainerWidget().inlineStyle("""
                        flex-direction: column;
                        overflow-y: scroll;
                        scrollbar-width: 8;
                        align-items: center;
                        """)
                .asRootStyle("""
                        Label{
                            text-height: 10rpx;
                        }
                        """)
                .addChild(createHorizontalSplit())
                .addChild(createFloatInput(0, 100, 0.125f, () -> IComponent.translatable("powertool.gui.holographic_sign.scale"), () -> this.scale, (s) -> this.scale = s))
                .addChild(createIntInput(-180, 180, 45, () -> IComponent.translatable("powertool.gui.holo_sign.x_rotation"), () -> this.xRotation, (s) -> this.xRotation = s))
                .addChild(createIntInput(-180, 180, 45, () -> IComponent.translatable("powertool.gui.holo_sign.y_rotation"), () -> this.yRotation, (s) -> this.yRotation = s))
                .addChild(createFloatInput(-2, 2, 0.125f, () -> IComponent.translatable("powertool.gui.holo_sign.x_offset"), () -> this.xOffset, xf -> this.xOffset = xf))
                .addChild(createFloatInput(-2, 2, 0.125f, () -> IComponent.translatable("powertool.gui.holo_sign.y_offset"), () -> this.yOffset, yf -> this.yOffset = yf))
                .addChild(createFloatInput(-2, 2, 0.125f, () -> IComponent.translatable("powertool.gui.holo_sign.z_offset"), () -> this.zOffset, zf -> this.zOffset = zf))
                .addChild(createHorizontalSplit())
                .addChild(createSelectionButton(() -> this.textAlign.displayName, () -> {
                            this.textAlign = switch (this.textAlign) {
                                case LEFT -> BaseHolographicSignBlockEntity.Align.CENTER;
                                case CENTER -> BaseHolographicSignBlockEntity.Align.RIGHT;
                                case RIGHT -> BaseHolographicSignBlockEntity.Align.LEFT;
                            };
                            this.onTextAlignChange();
                        }
                ))
                .addChild(createSelectionButton(() -> toggleMessage("powertool.gui.holo_sign.shadow", dropShadow), () -> this.dropShadow = !dropShadow))
                .addChild(createSelectionButton(() -> toggleMessage("powertool.gui.holo_sign.background", renderBackground), () -> this.renderBackground = !renderBackground))
                .addChild(createSelectionButton(() -> Component.translatable("powertool.gui.holographic_sign.lock." + this.locked), () -> this.locked = !this.locked))
                .addChild(createSelectionButton(() -> Component.translatable("powertool.gui.holographic_sign.bidirectional." + bidirectional), () -> this.bidirectional = !this.bidirectional))
                .addChild(createSelectionButton(() -> toggleMessage("powertool.gui.holo_sign.lit", this.lit), () -> this.lit = !this.lit))
                .addChild(createHorizontalSplit())
                .addChild(createColorInput())
                .addChild(createHorizontalSplit())
                .addChild(WidgetWrapper.button(CommonComponents.GUI_DONE, (_) -> this.onDone()).inlineStyle(layoutStr))
                .addChild(new Widget().inlineStyle("size: 60% 10rpx; flex-shrink: 0;"));
    }
    
    public void onTextAlignChange() {
    
    }
    
    protected Widget createColorInput() {
//        return new ContainerWidget().inlineStyle("""
//                        flex-direction: column;
//                        flex-shrink: 0;
//                        width: 100%;
//                        align-items: center;
//                        """)
//                .addChild(createALine(
//                        new Label(IComponent.translatable("powertool.gui.holographic_sign.color")),
//                        new Widget() {
//                            @Override
//                            public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
//                                super.doRender(graphics, mouseX, mouseY, a);
//                                var color = ARGB.color(colorA, colorR, colorG, colorB);
//                                graphics.fillRounded(this.x, this.y, this.getMaxX(), this.getMaxY(), color, 10);
//                            }
//                        }))
//                .addChild(createIntInput(0, 255, 1, () -> IComponent.literal("R: "), () -> this.colorR, i -> this.colorR = i))
//                .addChild(createIntInput(0, 255, 1, () -> IComponent.literal("G: "), () -> this.colorG, i -> this.colorG = i))
//                .addChild(createIntInput(0, 255, 1, () -> IComponent.literal("B: "), () -> this.colorB, i -> this.colorB = i))
//                .addChild(createIntInput(0, 255, 1, () -> IComponent.literal("A: "), () -> this.colorA, i -> this.colorA = i));
        ColorInputWidget result = (ColorInputWidget) new ColorInputWidget()
                .setCallback(c -> this.color = c.getValue())
                .inlineStyle("""
                        flex-direction: column;
                        flex-shrink: 0;
                        size: 100% 100rpx;
                        """);
        result.setValue(this.color);
        return result;
    }
    
    protected Widget createSelectionButton(Supplier<Component> text, Runnable callback) {
        return WidgetWrapper.button(text.get(), b -> {
            callback.run();
            b.setMessage(text.get());
        }).inlineStyle(layoutStr);
    }
    
    protected Widget createFloatInput(float min, float max, float step, Supplier<IComponent> text, Supplier<Float> getter, Consumer<Float> setter) {
        return createInput(NumberInputWidget.ofFloat(min, max, step), text, getter, setter);
    }
    
    protected Widget createIntInput(int min, int max, int step, Supplier<IComponent> text, Supplier<Integer> getter, Consumer<Integer> setter) {
        return createInput(NumberInputWidget.ofInt(min, max, step), text, getter, setter);
    }
    
    protected <N extends Number> Widget createInput(NumberInputWidget<N> input, Supplier<IComponent> text, Supplier<N> getter, Consumer<N> setter) {
        var label = new Label(text.get());
        input.setValue(getter.get());
        input.setCallback(n -> {
            setter.accept(n.getValue());
            label.setText(text.get());
        });
        return createALine(label, input);
    }
    
    protected Widget createALine(Widget left, Widget right) {
        return new ContainerWidget()
                .inlineStyle("""
                        size: 80% 20rpx;
                        margin-top: 2rpx;
                        margin-bottom: 2rpx;
                        flex-shrink: 0;
                        """)
                .addChild(left.inlineStyle("""
                        width: 45%;
                        text-align: left;
                        text-height: 10rpx;
                        text-color: -1;
                        """))
                .addChild(right.inlineStyle("width: 50%; margin-left: 2rpx;"));
    }
    
    protected Widget createHorizontalSplit() {
        return new Widget().inlineStyle("""
                size: 100% 2rpx;
                margin-top: 2rpx;
                margin-bottom: 2rpx;
                background-color: rgb(50,56,68);
                flex-shrink: 0;
                """);
    }
    
    private void rotateY(int degree) {
        var r = this.yRotation + degree;
        if (r < 0) {
            rotateY(360 + degree);
        } else {
            this.yRotation = r % 360;
        }
    }
    
    private void rotateX(int degree) {
        var r = this.xRotation + degree;
        if (r < 0) {
            rotateX(360 + degree);
        } else {
            this.xRotation = r % 360;
        }
    }
    
    protected void writeBackToBE() {
        this.sign.colorInARGB = this.color;
        this.sign.scale = this.scale;
        this.sign.align = this.textAlign;
        this.sign.lock = this.locked;
        this.sign.yRotate = this.yRotation;
        this.sign.xRotate = this.xRotation;
        this.sign.xOffset = this.xOffset;
        this.sign.yOffset = this.yOffset;
        this.sign.zOffset = this.zOffset;
        this.sign.bidirectional = this.bidirectional;
        this.sign.renderBackground = this.renderBackground;
        this.sign.dropShadow = this.dropShadow;
        this.sign.lit = this.lit;
    }
    
    protected Component toggleMessage(String key, boolean state) {
        return Component.translatable(key + (state ? "_on" : "_off"));
    }
    
    @Override
    public void removed() {
        //this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.writeBackToBE();
        ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(sign));
    }
    
    @Override
    public void tick() {
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }
    }
    
    public void onDone() {
        this.sign.setChanged();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void onClose() {
        this.onDone();
    }
    
}
