package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommonHolographicSignBlockEntity extends BaseHolographicSignBlockEntity {
    
    public List<? extends Component> contents = Collections.emptyList();
    public List<? extends Component> renderedContents = Collections.emptyList();

    public static final NodeParser TPAPI_PARSER = TagParser.DEFAULT;
    
    public CommonHolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    public void readFrom(ValueInput input) {
        super.readFrom(input);
        this.contents = input.listOrEmpty("content", ComponentSerialization.CODEC).stream().toList();
        if (getLevel() != null && getLevel().isClientSide()) {
            renderedContents = contents.stream().map(storedComponent -> {
                String storedComponentFormatless = storedComponent.getString();
                Component parsed = TPAPI_PARSER.parseComponent(storedComponentFormatless, ParserContext.of());
                if (parsed.getString().equals(storedComponentFormatless)) {
                    return Component.literal(storedComponentFormatless);
                } else {
                    return Component.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(storedComponentFormatless));
                }
            }).toList();
        }
    }
    
    @Override
    public void writeTo(ValueOutput output) {
        super.writeTo(output);
        var list = output.list("content", ComponentSerialization.CODEC);
        for (var c : contents) {
            list.add(c);
        }
    }
    
    @Override
    public void filterMessage(ServerPlayer player) {
        var task = player.getTextFilter()
                .processMessageBundle(this.contents.stream().map(Component::getString).toList());
        task.thenAcceptAsync(filtered -> {
            if (player.isTextFilteringEnabled()) {
                this.contents = filtered.stream().map(t -> Component.literal(t.filteredOrEmpty())).toList();
            } else {
                this.contents = filtered.stream().map(t -> Component.literal(t.raw())).toList();
            }
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }
}
