package scutum;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class ForcefieldBlock extends BlockWithEntity {
    public ForcefieldBlock(AbstractBlock.Settings settings) { super(settings); }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ForcefieldBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ForcefieldBlockEntity ffbe) ffbe.notifyOwnerOfExplosion(explosion);
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof ForcefieldBlockEntity ffbe) ffbe.notifyOwnerOfBreak();
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        } else {
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}