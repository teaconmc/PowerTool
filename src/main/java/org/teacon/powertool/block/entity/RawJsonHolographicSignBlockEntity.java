package org.teacon.powertool.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.utils.ParserUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class RawJsonHolographicSignBlockEntity extends BaseHolographicSignBlockEntity {
    
    public List<String> content = new ArrayList<>();
    
    public List<Component> forFilter = new ArrayList<>();
    public List<Component> forRender = new ArrayList<>();
    
    public RawJsonHolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    public void writeTo(ValueOutput output) {
        super.writeTo(output);
        output.putInt("contentSize", content.size());
        for (int i = 0; i < content.size(); i++) {
            output.putString("content_" + i, content.get(i));
        }
        output.putInt("forRenderSize", forRender.size());
        for (int i = 0; i < forRender.size(); i++) {
            output.store("forRender_" + i, ComponentSerialization.CODEC, forRender.get(i));
        }
    }
    
    @Override
    public void readFrom(ValueInput input) {
        super.readFrom(input);
        var contentSize = input.getIntOr("contentSize", 0);
        content.clear();
        for (int i = 0; i < contentSize; i++) {
            content.add(input.getStringOr("content_" + i, ""));
        }
        
        var forRenderSize = input.getIntOr("forRenderSize", 0);
        forRender.clear();
        for (var i = 0; i < forRenderSize; i++) {
            forRender.add(input.read("forRender_" + i, ComponentSerialization.CODEC).orElse(Component.empty()));
        }
        var registries = this.level.registryAccess();
        forFilter.clear();
        try {
            for (var ct : content) {
                forFilter.add(ParserUtils.parseJson(registries, ct, ComponentSerialization.CODEC));
            }
        } catch (Exception ignore) {
        }
    }
    
    @Override
    public void filterMessage(ServerPlayer player) {
        this.forRender.clear();
        var taskList = new ArrayList<CompletableFuture<?>>();
        //不用processMessageBundle 因为没有处理后list size和顺序不变的保证
        for (var i = 0; i < forFilter.size(); i++) {
            var task = player.getTextFilter()
                    .processStreamMessage(forFilter.get(i).getString());
            int finalI = i;
            task.thenAccept(filtered -> {
                if (player.isTextFilteringEnabled()) {
                    this.forRender.add(finalI, Component.literal(filtered.filteredOrEmpty()).withStyle(forFilter.get(finalI).getStyle()));
                } else {
                    this.forRender.add(finalI, forFilter.get(finalI));
                }
                try {
                    this.forRender.add(finalI, ComponentUtils.resolve(ResolutionContext.create(player.createCommandSourceStackForNameResolution(player.level())), forRender.remove(finalI), 0));
                } catch (CommandSyntaxException ignored) {
                }
                
            });
            taskList.add(task);
            
        }
        var finalTask = CompletableFuture.allOf(taskList.toArray(new CompletableFuture<?>[0]));
        finalTask.thenAcceptAsync((_void) -> {
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }
}
