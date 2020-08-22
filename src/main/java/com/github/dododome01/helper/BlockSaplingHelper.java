package com.github.dododome01.helper;

import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class BlockSaplingHelper
{
    // Added code for checking water for dead shrub rule
    public static boolean hasWater(WorldAccess worldIn, BlockPos pos)
    {
        for (BlockPos blockpos$mutableblockpos : BlockPos.iterate(pos.add(-4, -4, -4), pos.add(4, 1, 4)))
        {
            if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER)
            {
                return true;
            }
        }

        return false;
    }
}