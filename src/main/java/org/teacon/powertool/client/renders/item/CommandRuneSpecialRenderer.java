package org.teacon.powertool.client.renders.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.item.PowerToolDataComponents;

import java.util.function.Consumer;

@NonNullByDefault
public class CommandRuneSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    
    private static final CommandRuneSpecialRenderer INSTANCE = new CommandRuneSpecialRenderer();
    
    @Override
    public void submit(@Nullable ItemStack argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        var renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().appendItemLayers(renderState, argument, ItemDisplayContext.GUI, null, null, 0);
        poseStack.pushPose();
        poseStack.translate(0.75F, 0.75F, 0.532f);
        poseStack.scale(0.6F, 0.6F, 0.001F);
        renderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.75F, 0.75F, 0.468f);
        poseStack.scale(0.6F, 0.6F, 0.001F);
        renderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        poseStack.popPose();
    }
    
    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }
    
    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        Identifier labelId = stack.get(PowerToolDataComponents.COMMAND_RUNE_LABEL);
        if (labelId == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.getValue(labelId).getDefaultInstance();
    }
    
    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());
        
        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return INSTANCE;
        }
        
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
