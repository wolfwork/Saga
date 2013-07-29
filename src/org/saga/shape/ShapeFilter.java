package org.saga.shape;

import org.bukkit.block.Block;



public interface ShapeFilter {
	
	
	/**
	 * Check if the block is part of the shape.
	 * 
	 * @param block block
	 * @return true if should be included in the shape
	 */
	public boolean checkBlock(Block block);
	
	
}
