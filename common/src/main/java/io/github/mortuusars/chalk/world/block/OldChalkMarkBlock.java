package io.github.mortuusars.chalk.world.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.OldMarkSymbol;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("removal")
@Deprecated(since = "2.0.0", forRemoval = true)
public class OldChalkMarkBlock extends BaseEntityBlock {
    public static final MapCodec<OldChalkMarkBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
          DyeColor.CODEC.fieldOf("color").forGetter(OldChalkMarkBlock::getColor),
          propertiesCodec()
    ).apply(i, OldChalkMarkBlock::new));

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<OldMarkSymbol> SYMBOL = EnumProperty.create("symbol", OldMarkSymbol.class);
    public static final EnumProperty<SymbolOrientation> ORIENTATION = EnumProperty.create("orientation", SymbolOrientation.class);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    private final DyeColor color;

    public OldChalkMarkBlock(DyeColor dyeColor, Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
              .setValue(FACING, Direction.NORTH)
              .setValue(SYMBOL, OldMarkSymbol.CENTER)
              .setValue(ORIENTATION, SymbolOrientation.NORTH)
              .setValue(GLOWING, false));
        color = dyeColor;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(ORIENTATION).add(GLOWING).add(SYMBOL);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OldMarkBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return !level.isClientSide()
              ? createTickerHelper(blockEntityType, Chalk.BlockEntityTypes.CHALK_MARK.get(), OldChalkMarkBlock::convertMark)
              : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        convertMark(level, pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            convertMark(serverLevel, pos, state);
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        convertMark(level, pos, state);
    }

    public static void convertMark(Level level, BlockPos pos, BlockState state, OldMarkBlockEntity ignored) {
        convertMark(level, pos, state);
    }

    public static void convertMark(Level level, BlockPos pos, BlockState state) {
        try {
            if (!level.isClientSide() && MarkBlock.getExistingOrPlaceNew(level, pos) instanceof MarkBlockEntity blockEntity) {
                Mark mark = new Mark(
                      state.getValue(SYMBOL).convert(level.registryAccess()),
                      ChalkItem.getColorFromDye(((OldChalkMarkBlock) state.getBlock()).getColor()),
                      state.getValue(ORIENTATION),
                      state.getValue(GLOWING)
                );
                blockEntity.getMarks().set(state.getValue(FACING), mark);
                blockEntity.marksChanged();
                return;
            }
        } catch (Exception e) {
            Chalk.LOGGER.error("Failed to convert old chalk mark. Block will be removed.", e);
        }

        level.removeBlock(pos, false);
    }
}