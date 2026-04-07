package org.teacon.powertool.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.client.gui.widget.BlockEntityList;
import org.teacon.powertool.datagen.PowerToolBlockTagsProvider;
import org.teacon.powertool.item.ExamineHoloGlass;
import org.teacon.powertool.item.PowerToolDataComponents;
import org.teacon.powertool.network.server.UpdateItemStackData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class ExamineHoloGlassScreen extends Screen {
    
    protected final EquipmentSlot slot;
    public Set<TagKey<Block>> tagsData;
    public Set<Identifier> blocksData;
    
    protected Checkbox commandBlockTagCheckBox;
    protected Checkbox repeatingCommandBlockTagCheckBox;
    protected Checkbox bezierCurveBlockCheckBox;
    protected BlockEntityList blockEntityList;
    
    public ExamineHoloGlassScreen(EquipmentSlot slot, @Nullable ExamineHoloGlass.BlockTagsComponent tagsData, @Nullable ExamineHoloGlass.BlockComponents blocksData) {
        super(Component.translatable("powertool.examine_holo_glass.screen"));
        this.slot = slot;
        this.tagsData = new HashSet<>();
        this.blocksData = new HashSet<>();
        if (tagsData != null) this.tagsData.addAll(tagsData.tags());
        if (blocksData != null) this.blocksData.addAll(blocksData.blocks());
    }
    
    @Override
    protected void init() {
        var font = Minecraft.getInstance().font;
        var wc = this.width / 2;
        commandBlockTagCheckBox = Checkbox.builder(Component.translatable("powertool.examine_holo_glass.screen.tag.command_block"), font)
                .pos(wc - 160, 40)
                .maxWidth(150)
                .selected(tagsData.contains(PowerToolBlockTagsProvider.COMMAND_BLOCK_TAG))
                .onValueChange(withTag(PowerToolBlockTagsProvider.COMMAND_BLOCK_TAG))
                .build();
        repeatingCommandBlockTagCheckBox = Checkbox.builder(Component.translatable("powertool.examine_holo_glass.screen.tag.repeating_command_block"), font)
                .pos(wc - 160, 65)
                .maxWidth(150)
                .selected(tagsData.contains(PowerToolBlockTagsProvider.REPEATING_COMMAND_BLOCK_TAG))
                .onValueChange(withTag(PowerToolBlockTagsProvider.REPEATING_COMMAND_BLOCK_TAG))
                .build();
        bezierCurveBlockCheckBox = Checkbox.builder(Component.translatable("powertool.examine_holo_glass.screen.tag.bezier_curve_block"), font)
                .pos(wc - 160, 90)
                .maxWidth(150)
                .selected(blocksData.contains(PowerToolBlocks.BEZIER_CURVE_BLOCK.getId()))
                .onValueChange(withBlock(PowerToolBlocks.BEZIER_CURVE_BLOCK.getId()))
                .build();
        blockEntityList = new BlockEntityList(this, (int) (width * 0.4), (int) (height * 0.8), (int) (height * 0.1), 25);
        this.addRenderableWidget(commandBlockTagCheckBox);
        this.addRenderableWidget(repeatingCommandBlockTagCheckBox);
        this.addRenderableWidget(bezierCurveBlockCheckBox);
        this.addRenderableWidget(blockEntityList);
    }
    
    protected Checkbox.OnValueChange withTag(TagKey<Block> tag) {
        return (self, value) -> {
            if (value) tagsData.add(tag);
            else tagsData.remove(tag);
            //if(blockEntityList != null) blockEntityList.update();
        };
    }
    
    protected Checkbox.OnValueChange withBlock(Identifier blockID) {
        return (self, value) -> {
            if (value) blocksData.add(blockID);
            else blocksData.remove(blockID);
            //if(blockEntityList != null) blockEntityList.update();
        };
    }
    
    @Override
    public void removed() {
        if (blockEntityList != null) {
            blocksData.addAll(blockEntityList.getResult());
        }
        var patch = DataComponentPatch.builder()
                .set(PowerToolDataComponents.BLOCK_TAGS_DATA.get(), new ExamineHoloGlass.BlockTagsComponent(new ArrayList<>(tagsData)))
                .set(PowerToolDataComponents.BLOCKS_DATA.get(), new ExamineHoloGlass.BlockComponents(new ArrayList<>(blocksData)))
                .build();
        ClientPacketDistributor.sendToServer(new UpdateItemStackData(slot, patch));
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        var str = Component.translatable("powertool.gui.examine_holo_glass.warn");
        graphics.text(font, str, (int) (width * 0.55), (int) (height * 0.9 + 2), -1);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
