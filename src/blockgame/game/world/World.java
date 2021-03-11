package blockgame.game.world;

import java.util.ArrayList;
import java.util.HashMap;

import blockgame.RenderableCallback;
import blockgame.game.Block;
import blockgame.game.BlockData;
import blockgame.game.Location;

public class World implements RenderableCallback {
	private ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	private HashMap<ChunkHashKey, Chunk> chunkMap = new HashMap<ChunkHashKey, Chunk>();

	public final static int SEED = (int) (Math.random() * 1e5);
	
	public final static int SEA_LEVEL = (int) 64;

	public World() {
		
		// Create chunks
		int a = 2;
		for (int i = -a; i <= a; i++) {
			for (int j = -a; j <= a; j++) {
				loadChunk(new Chunk(this, i, j));
			}
		}
		
		// Generate world
		for (int i = 0; i < chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			WorldGeneration.generate(chunk);
		}

		new ChunkManager();
	}
	
	public void loadChunk(Chunk chunk) {
		loadChunk( chunk, false );
	}
	
	public synchronized void loadChunk( Chunk chunk, boolean generate ) {
		synchronized( chunks ) {
			if ( chunks.contains(chunk) )
				return;
			
			Location loc = chunk.getLocation();
			chunks.add(chunk);
			chunkMap.put(new ChunkHashKey(loc.getBlockX(), loc.getBlockZ()), chunk);
			chunk.updated = true;
			
			if ( generate ) {
				WorldGeneration.generate(chunk);
			}
			
			// Mark neighboring chunks as needing to be updated
			for (int i = -1; i <=1; i++) {
				for (int j = -1; j <=1; j++) {
					if ( i == 0 && j == 0 )
						continue;
					
					Chunk c = this.getChunk(loc.getBlockX()+i, loc.getBlockZ()+j);
					if ( c != null )
						c.updated = true;
				}
			}
		}
	}

	static class ChunkHashKey {
		private int x;
		private int y;

		ChunkHashKey( int x, int y ) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() { 
			return 40000 * x + y;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkHashKey other = (ChunkHashKey) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}

	/**
	 * Get chunk at a world coordinate (block coordinate)
	 * @param worldX
	 * @param worldZ
	 * @return
	 */
	public Chunk getChunkAt( float worldX, float worldZ ) {
		Location tempLoc = new Location(this, worldX, 0, worldZ);
		return getChunk( tempLoc.getChunkX(), tempLoc.getChunkZ() );
	}
	
	
	/**
	 * Get chunk at a chunk coordinate.
	 * @param x
	 * @param z
	 * @return
	 */
	public Chunk getChunk( int x, int z ) {
		ChunkHashKey key = new ChunkHashKey(x, z);

		return chunkMap.get(key);
	}
	
	public void setBlock(BlockData block, int x, int y, int z) {
		Chunk c = getChunkAt( x, z );
		if ( c == null )
			return;
		
		Location chunkLoc = c.getWorldLocation();
		int localX = (int) (x - chunkLoc.getX());
		int localY = (int) (y - chunkLoc.getY());
		int localZ = (int) (z - chunkLoc.getZ());
		
		c.setBlock(block, localX, localY, localZ);
	}

	public Block getBlock( int x, int y, int z ) {
		return new Block( this, getBlockId( x, y, z ), x, y, z );
	}

	public byte getBlockId( int x, int y, int z ) {
		Chunk c = getChunkAt( x, z );
		if ( c == null )
			return (byte) BlockData.AIR.getId();

		Location chunkLoc = c.getWorldLocation();
		int localX = (int) (x - chunkLoc.getX());
		int localY = (int) (y - chunkLoc.getY());
		int localZ = (int) (z - chunkLoc.getZ());

		return c.getBlockId(localX, localY, localZ);
	}

	@Override
	public void render() {
		for (int i = 0; i < chunks.size(); i++) {
			if ( i >= chunks.size() )
				continue;
			Chunk c = chunks.get(i);
			if ( c == null )
				continue;
			c.render();
		}
	}

	public ArrayList<Chunk> getLoadedChunks() {
		return chunks;
	}
}
