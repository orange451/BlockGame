package blockgame.game.world;

import blockgame.game.BlockData;
import blockgame.game.Location;

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
	
	@Override
	public boolean equals(Object o) {
		if ( o == null )
			return false;
		
		if ( !(o instanceof Block) )
			return false;
		
		Block b = (Block)o;
		if ( b.blockId != this.blockId )
			return false;
		
		if ( b.x != x || b.y != y || b.z != z )
			return false;
		
		if ( !b.chunk.equals(this.chunk) )
			return false;
		
		if ( !b.world.equals(this.world) )
			return false;
		
		return true;
	}
}
