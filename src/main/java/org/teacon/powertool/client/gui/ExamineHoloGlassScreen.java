package org.teacon.powertool.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.datagen.PowerToolBlockTagsProvider;
import org.teacon.powertool.item.ExamineHoloGlass;
import org.teacon.powertool.item.PowerToolItems;
import org.teacon.powertool.network.server.UpdateItemStackData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class ExamineHoloGlassScreen extends Screen {
    
    protected final EquipmentSlot slot;
    protected Set<TagKey<Block>> tagsData;
    protected Set<ResourceLocation> blocksData;
    
    protected Checkbox commandBlockTagCheckBox;
    protected Checkbox repeatingCommandBlockTagCheckBox;
    
    public ExamineHoloGlassScreen(EquipmentSlot slot,@Nullable ExamineHoloGlass.BlockTagsComponent tagsData,@Nullable ExamineHoloGlass.BlockComponents blocksData) {
        super(Component.translatable("powertool.examine_holo_glass.screen"));
        this.slot = slot;
        this.tagsData = new HashSet<>();
        this.blocksData = new HashSet<>();
        if(tagsData != null) this.tagsData.addAll(tagsData.tags());
        if(blocksData != null) this.blocksData.addAll(blocksData.blocks());
    }
    
    @Override
    protected void init() {
        var font = Minecraft.getInstance().font;
        var wc = this.width/2;
        commandBlockTagCheckBox = Checkbox.builder(Component.translatable("powertool.examine_holo_glass.screen.tag.command_block"),font)
                .pos(wc-160,40)
                .maxWidth(150)
                .selected(tagsData.contains(PowerToolBlockTagsProvider.COMMAND_BLOCK_TAG))
                .onValueChange(withTag(PowerToolBlockTagsProvider.COMMAND_BLOCK_TAG))
                .build();
        repeatingCommandBlockTagCheckBox = Checkbox.builder(Component.translatable("powertool.examine_holo_glass.screen.tag.repeating_command_block"),font)
                .pos(wc-160,65)
                .maxWidth(150)
                .selected(tagsData.contains(PowerToolBlockTagsProvider.REPEATING_COMMAND_BLOCK_TAG))
                .onValueChange(withTag(PowerToolBlockTagsProvider.REPEATING_COMMAND_BLOCK_TAG))
                .build();
        
        this.addRenderableWidget(commandBlockTagCheckBox);
        this.addRenderableWidget(repeatingCommandBlockTagCheckBox);
    }
    
    protected Checkbox.OnValueChange withTag(TagKey<Block> tag) {
        return (self,value) -> {
            if(value) tagsData.add(tag);
            else tagsData.remove(tag);
        };
    }
    
    @Override
    public void removed() {
        var patch = DataComponentPatch.builder()
                .set(PowerToolItems.BLOCK_TAGS_DATA.get(),new ExamineHoloGlass.BlockTagsComponent(new ArrayList<>(tagsData)))
                .set(PowerToolItems.BLOCKS_DATA.get(),new ExamineHoloGlass.BlockComponents(new ArrayList<>(blocksData)))
                .build();
        PacketDistributor.sendToServer(new UpdateItemStackData(slot,patch));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Lighting.setupFor3DItems();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
