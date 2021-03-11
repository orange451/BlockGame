package blockgame.game;

import blockgame.game.world.Chunk;
import blockgame.game.world.World;

public class Block {
	private byte blockId;
	private Chunk chunk;
	private World world;
	private int x;
	private int y;
	private int z;
	
	public Block(World world, byte blockId, int x, int y, int z) {
		this.blockId = blockId;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.chunk = world.getChunkAt(x, z);
	}
	
	public Block(World world, BlockData blockType, int x, int y, int z) {
		this( world, (byte)blockType.getId(), x, y, z );
	}

	public BlockData getData() {
		return BlockData.getBlockData(blockId);
	}
	
	public Chunk getChunk() {
		return this.chunk;
	}
	
	public Location getLocation() {
		return new Location( world, x, y, z );
	}
}
