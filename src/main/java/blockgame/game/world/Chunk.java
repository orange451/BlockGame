package blockgame.game.world;

import blockgame.game.BlockData;
import blockgame.game.Location;

public class Chunk {
	public static final int WIDTH = 16;
	public static final int DEPTH = 16;
	public static final int HEIGHT = 128;
	
	protected boolean updated = false;
	protected boolean loaded = true;
	
	private byte[] blocks = new byte[WIDTH * DEPTH * HEIGHT];
	
	protected int x;
	protected int z;
	protected World world;
	
	public Chunk(World world, int x, int z) {
		this.x = x;
		this.z = z;
		this.world = world;
		
		// Initial loop. Set all to air
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				for (int k = 0; k < DEPTH; k++) {
					setBlock(BlockData.AIR, i, j, k);
				}
			}
		}
	}
	
	public void setBlock( BlockData block, int x, int y, int z ) {
		setBlock( (byte) block.getId(), x, y, z );
	}
	
	public void setBlock( byte blockId, int x, int y, int z ) {
		if ( x < 0 )
			return;
		if ( y < 0 )
			return;
		if ( z < 0 )
			return;
		if ( x >= WIDTH )
			return;
		if ( y >= HEIGHT )
			return;
		if ( z >= DEPTH )
			return;
		
		this.blocks[(x << 11 | z << 7 | y)] = blockId;
		updated = true;
	}
	
	public Block getBlock( int x, int y, int z ) {
		return new Block(world, getBlockId(x, y, z), x, y, z);
	}
	
	public byte getBlockId( int x, int y, int z ) {
		if ( x < 0 )
			return (byte) BlockData.AIR.getId();
		if ( x >= WIDTH )
			return (byte) BlockData.AIR.getId();
		
		if ( y < 0 )
			return (byte) BlockData.AIR.getId();
		if ( y >= HEIGHT )
			return (byte) BlockData.AIR.getId();
		
		if ( z < 0 )
			return (byte) BlockData.AIR.getId();
		if ( z >= DEPTH )
			return (byte) BlockData.AIR.getId();
		
		return this.blocks[(x << 11 | z << 7 | y)];
	}
	
	protected void unload() {
		this.updated = true;
		this.loaded = false;
	}
	
	public boolean loaded() {
		return this.loaded;
	}

	/**
	 * @return The chunk location. This is in chunk-space.
	 */
	public Location getLocation() {
		return new Location( world, x, 0, z );
	}
	
	/**
	 * @return The world location this chunk exists at. This is in block-space.
	 */
	public Location getWorldLocation() {
		return new Location( world, x * WIDTH, 0, z * DEPTH );
	}

	/**
	 * @return The world this chunk exists within.
	 */
	public World getWorld() {
		return this.world;
	}

	/**
	 * Returns the top most block y position in this chunk that is not air.
	 * @param x
	 * @param z
	 */
	public int getTopLevel(int x, int z) {
		for (int i = 0; i < HEIGHT; i++) {
			byte block = getBlockId( x, i, z );
			if ( block == BlockData.AIR.getId() ) {
				return i;
			}
		}
		
		return HEIGHT-1;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o == null )
			return false;
		
		if ( !(o instanceof Chunk) )
			return false;
		
		Chunk c = (Chunk)o;
		if ( c.x != x || c.z != z )
			return false;
		
		if ( !c.world.equals(world) )
			return false;
		
		return true;
	}
}
