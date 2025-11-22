package scutum;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.entity.BlockEntityTicker;

public class ProjectorBlock extends BlockWithEntity {
    public static final BooleanProperty LIT = Properties.LIT;

    public ProjectorBlock(Settings settings) { super(settings); this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false)); }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new ProjectorBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends net.minecraft.block.entity.BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        if (world.isClient) return null;
        return (w, p, s, be) -> { if (be instanceof ProjectorBlockEntity pb) pb.serverTick((ServerWorld) w); };
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> new ProjectorScreenHandler(syncId, inv, pos), Text.literal("Projector"));
        }
        return ActionResult.success(world.isClient);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) { return this.getDefaultState().with(LIT, false); }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ProjectorBlockEntity projector && !world.isClient) projector.onBroken();
            super.onStateReplaced(state, world, pos, newState, moved);
        } else super.onStateReplaced(state, world, pos, newState, moved);
    }
}