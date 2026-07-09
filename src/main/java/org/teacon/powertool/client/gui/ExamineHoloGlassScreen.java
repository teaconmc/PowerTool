package org.teacon.powertool.client.gui;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.item.ExamineHoloGlass;
import org.teacon.powertool.item.PowerToolDataComponents;
import org.teacon.powertool.network.server.UpdateItemStackData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@NonNullByDefault
public class ExamineHoloGlassScreen extends XKLibBaseScreen {
    
    protected final EquipmentSlot slot;
    protected final Set<TagKey<Block>> tagsData;
    protected final Set<Identifier> selectedTypes = new HashSet<>();
    protected final List<BlockEntityTypeData> allTypes;
    protected @Nullable ContainerWidget selectedList;
    protected @Nullable ContainerWidget availableList;
    protected String searchText = "";
    
    public ExamineHoloGlassScreen(EquipmentSlot slot, ExamineHoloGlass.@Nullable BlockTagsComponent tagsData, ExamineHoloGlass.@Nullable BlockComponents blocksData) {
        super(Component.translatable("powertool.examine_holo_glass.screen"));
        this.slot = slot;
        this.tagsData = new HashSet<>();
        if (tagsData != null) {
            this.tagsData.addAll(tagsData.tags());
        }
        var blockIds = new HashSet<Identifier>();
        if (blocksData != null) {
            blockIds.addAll(blocksData.blocks());
        }
        this.allTypes = BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet().stream()
                .map(entry -> new BlockEntityTypeData(entry.getKey().identifier(), entry.getValue()))
                .sorted(Comparator.comparing(data -> data.id().toString()))
                .toList();
        for (var data : this.allTypes) {
            if (data.type().getValidBlocks().stream().map(BuiltInRegistries.BLOCK::getKey).anyMatch(blockIds::contains)) {
                this.selectedTypes.add(data.id());
            }
        }
        this.addScreenLayer(this.createLayout());
    }
    
    protected Widget createLayout() {
        this.selectedList = listContainer();
        this.availableList = listContainer();
        var searchBox = WidgetWrapper.editBox(Component.translatable("powertool.examine_holo_glass.screen.search").getString(), 114514, value -> {
            this.searchText = value.toLowerCase(Locale.ROOT);
            this.refreshAvailableList();
        });
        var leftPanel = panel(IComponent.translatable("powertool.examine_holo_glass.screen.selected"), this.selectedList, null);
        var rightPanel = panel(IComponent.translatable("powertool.examine_holo_glass.screen.available"), this.availableList, searchBox);
        var root = new ContainerWidget()
                .inlineStyle("""
                        flex-direction: row;
                        size: 100% 100%;
                        align-items: center;
                        justify-content: center;
                        """)
                .addChild(leftPanel)
                .addChild(rightPanel.inlineStyle("margin-left: 4%;"));
        this.refreshLists();
        return root;
    }
    
    private ContainerWidget panel(IComponent title, ContainerWidget list, @Nullable Widget searchBox) {
        var panel = new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        size: 38% 80%;
                        flex-shrink: 0;
                        """)
                .addChild(new Label(title).inlineStyle("""
                        size: 100% 20rpx;
                        flex-shrink: 0;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(list.inlineStyle("""
                        size: 100% 100%-50rpx;
                        flex-shrink: 0;
                        margin-top: 5rpx;
                        border: 1rpx;
                        border-color: 0x99FFFFFF;
                        """));
        if (searchBox == null) {
            panel.addChild(new Widget().inlineStyle("size: 100% 25rpx; flex-shrink: 0; margin-top: 5rpx;"));
        } else {
            panel.addChild(searchBox.inlineStyle("size: 100% 20rpx; flex-shrink: 0; margin-top: 5rpx;"));
        }
        return panel;
    }
    
    private ContainerWidget listContainer() {
        return new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        overflow-y: scroll;
                        scrollbar-width: 8;
                        """);
    }
    
    private void refreshLists() {
        this.refreshSelectedList();
        this.refreshAvailableList();
    }
    
    private void refreshSelectedList() {
        if (this.selectedList == null) {
            return;
        }
        this.selectedList.clearChildren();
        for (var data : this.selectedBlockEntityTypes()) {
            this.selectedList.addChild(row(data.id(), Component.literal("-"), () -> {
                this.selectedTypes.remove(data.id());
                this.refreshLists();
            }));
        }
    }
    
    private void refreshAvailableList() {
        if (this.availableList == null) {
            return;
        }
        this.availableList.clearChildren();
        for (var data : this.availableBlockEntityTypes()) {
            this.availableList.addChild(row(data.id(), Component.literal("+"), () -> {
                this.selectedTypes.add(data.id());
                this.refreshLists();
            }));
        }
    }
    
    private List<BlockEntityTypeData> selectedBlockEntityTypes() {
        return this.allTypes.stream()
                .filter(data -> this.selectedTypes.contains(data.id()))
                .toList();
    }
    
    private List<BlockEntityTypeData> availableBlockEntityTypes() {
        return this.allTypes.stream()
                .filter(data -> !this.selectedTypes.contains(data.id()))
                .filter(data -> this.searchText.isBlank() || data.id().toString().toLowerCase(Locale.ROOT).contains(this.searchText.toLowerCase(Locale.ROOT)))
                .toList();
    }
    
    private Widget row(Identifier id, Component buttonText, Runnable action) {
        return new ContainerWidget()
                .inlineStyle("""
                        size: 100% 22rpx;
                        flex-shrink: 0;
                        flex-direction: row;
                        align-items: center;
                        """)
                .addChild(new Label(IComponent.literal(id.toString())).inlineStyle("""
                        size: 100%-24rpx 100%;
                        flex-shrink: 0;
                        text-color: -1;
                        text-height: 10rpx;
                        text-align: left;
                        """))
                .addChild(WidgetWrapper.button(buttonText, _ -> action.run()).inlineStyle("size: 20rpx 20rpx; flex-shrink: 0;"));
    }
    
    @Override
    public void removed() {
        var selectedBlocks = this.selectedBlockEntityTypes().stream()
                .flatMap(data -> data.type().getValidBlocks().stream())
                .map(BuiltInRegistries.BLOCK::getKey)
                .distinct()
                .toList();
        var patch = DataComponentPatch.builder()
                .set(PowerToolDataComponents.BLOCK_TAGS_DATA.get(), new ExamineHoloGlass.BlockTagsComponent(new ArrayList<>(tagsData)))
                .set(PowerToolDataComponents.BLOCKS_DATA.get(), new ExamineHoloGlass.BlockComponents(new ArrayList<>(selectedBlocks)))
                .build();
        ClientPacketDistributor.sendToServer(new UpdateItemStackData(slot, patch));
    }
    
    private record BlockEntityTypeData(Identifier id, BlockEntityType<?> type) {
    }
}
