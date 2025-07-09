import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SimulatedBuildLevel extends LevelReader {
    private final Level realLevel;

    public SimulatedBuildLevel(Level realLevel) {
        super(realLevel.getChunkSource(), realLevel.dimension(), realLevel.dimensionTypeRegistration(), realLevel.getProfiler(), realLevel.isClientSide(), false, 0);
        this.realLevel = realLevel;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = realLevel.getBlockState(pos);
        if (state.getBlock() instanceof RenderBlock) {
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof RenderBlockBE renderBlockBE && renderBlockBE.targetBlock != null) {
                return renderBlockBE.targetBlock;
            }
        }
        return state;
    }
}
