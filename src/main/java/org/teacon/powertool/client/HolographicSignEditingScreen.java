package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.network.PowerToolNetwork;
import org.teacon.powertool.network.server.UpdateHolographicSignData;

import java.util.Arrays;

public class HolographicSignEditingScreen extends Screen {
    private final HolographicSignBlockEntity sign;
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private final WoodType woodType = WoodType.BIRCH;
    private SignRenderer.SignModel signModel;
    private final String[] messages;

    public HolographicSignEditingScreen(HolographicSignBlockEntity theSign, boolean pIsTextFilteringEnabled) {
        super(new TranslatableComponent("sign.edit"));
        var size = theSign.contents.size();
        this.messages = new String[Math.max(size, 4)];
        Arrays.fill(this.messages, "");
        for (int i = 0; i < size; i++) {
            this.messages[i] = theSign.contents.get(i).getString();
        }
        this.sign = theSign;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, btn -> this.onDone()));
        this.signField = new TextFieldHelper(
                () -> this.messages[this.line],
                (str) -> this.messages[this.line] = str,
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                str -> true
        );
        this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
        this.signModel.stick.visible = false;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        int last = this.messages.length - 1;
        for (; last >= 0; last--) {
            if (this.messages[last] != null && !this.messages[last].isEmpty()) {
                break;
            }
        }
        var toSend = Arrays.copyOfRange(this.messages, 0, last + 1);
        PowerToolNetwork.channel().send(PacketDistributor.SERVER.with(() -> null), new UpdateHolographicSignData(this.sign.getBlockPos(), toSend));
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }
    }

    private void onDone() {
        this.sign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        this.signField.charTyped(pCodePoint);
        return true;
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            // Move up one line
            this.line = (this.line - 1) % this.messages.length;
            this.signField.setCursorToEnd();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            // Move down one line
            this.line = (this.line + 1) % this.messages.length;
            this.signField.setCursorToEnd();
            return true;
        } else {
            // Regular typing
            return this.signField.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(PoseStack transform, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        this.renderBackground(transform);
        drawCenteredString(transform, this.font, this.title, this.width / 2, 40, 0xFFFFFF);
        transform.pushPose();
        transform.translate(this.width / 2.0, 0.0D, 50.0D);
        transform.scale(93.75F, -93.75F, 93.75F);
        transform.translate(0.0, -1.625, 0.0);

        // Render the background
        transform.pushPose();
        transform.scale(2F / 3F, -2F / 3F, -2F / 3F);
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        Material material = Sheets.getSignMaterial(this.woodType);
        VertexConsumer vertexconsumer = material.buffer(bufferSource, this.signModel::renderType);
        this.signModel.root.render(transform, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        transform.popPose();

        // Render the text and cursor
        boolean showCursor = this.frame / 6 % 2 == 0;
        transform.translate(0, 1.0 / 3.0, 7.0 / 15.0);
        transform.scale(1F / 96F, -1F / 96F, 1F / 96F);
        int cursorPos = this.signField.getCursorPos();
        int selectionPos = this.signField.getSelectionPos();
        int cursorY = this.line * 10 - this.messages.length * 5;
        Matrix4f transformMat = transform.last().pose();
        for(int line = 0; line < this.messages.length; ++line) {
            String text = this.messages[line];
            if (text != null) {
                if (this.font.isBidirectional()) {
                    text = this.font.bidirectionalShaping(text);
                }
                float xStart = (float)(-this.minecraft.font.width(text) / 2);
                this.minecraft.font.drawInBatch(text, xStart, (float)(line * 10 - this.messages.length * 5), line, false, transformMat, bufferSource, false, 0, LightTexture.FULL_BRIGHT, false);
                if (line == this.line && cursorPos >= 0 && showCursor) {
                    int j1 = this.minecraft.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                    int cursorX = j1 - this.minecraft.font.width(text) / 2;
                    if (cursorPos >= text.length()) {
                        this.minecraft.font.drawInBatch("_", cursorX, cursorY, line, false, transformMat, bufferSource, false, 0, LightTexture.FULL_BRIGHT, false);
                    }
                }
            }
        }
        bufferSource.endBatch();

        // Render selection highlights
        for(int i = 0; i < this.messages.length; ++i) {
            String text = this.messages[i];
            if (text != null && i == this.line && cursorPos >= 0) {
                int j3 = this.minecraft.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                int k3 = j3 - this.minecraft.font.width(text) / 2;
                if (showCursor && cursorPos < text.length()) {
                    fill(transform, k3, cursorY - 1, k3 + 1, cursorY + 9, 0xFFFFFFFF);
                }

                if (selectionPos != cursorPos) {
                    int l3 = Math.min(cursorPos, selectionPos);
                    int l1 = Math.max(cursorPos, selectionPos);
                    int i2 = this.minecraft.font.width(text.substring(0, l3)) - this.minecraft.font.width(text) / 2;
                    int j2 = this.minecraft.font.width(text.substring(0, l1)) - this.minecraft.font.width(text) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder builder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    builder.vertex(transformMat, (float)k2, (float)(cursorY + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(transformMat, (float)l2, (float)(cursorY + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(transformMat, (float)l2, (float)cursorY, 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.vertex(transformMat, (float)k2, (float)cursorY, 0.0F).color(0, 0, 255, 255).endVertex();
                    builder.end();
                    BufferUploader.end(builder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        transform.popPose();
        Lighting.setupFor3DItems();
        super.render(transform, mouseX, mouseY, partialTick);
    }
}
