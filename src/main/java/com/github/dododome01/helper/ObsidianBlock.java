package com.github.dododome01.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Random;

public class ObsidianBlock extends Block {
    public ObsidianBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int lavaCount = 0;
        for (Direction dir : Direction.values()) {
            FluidState neighbor = world.getFluidState(pos.offset(dir));
            Fluid neighborFluid = neighbor.getFluid();
            if (neighborFluid != Fluids.LAVA && neighborFluid != Fluids.FLOWING_LAVA) return;
            if (neighbor.isStill()) lavaCount += 2;
            else lavaCount += 1;
        }
        if (random.nextInt(10*(13-lavaCount)) == 0) {
            world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        }
    }
}