package blockgame.game.world;

import blockgame.etc.OpenSimplexNoise;
import blockgame.game.BlockData;

public class WorldGeneration {

	protected static OpenSimplexNoise terrainNoise = new OpenSimplexNoise(World.SEED);
	protected static OpenSimplexNoise mountainNoise = new OpenSimplexNoise(World.SEED*4);
	protected static OpenSimplexNoise moistureNoise = new OpenSimplexNoise(World.SEED*2);
	protected static OpenSimplexNoise treeNoise = new OpenSimplexNoise(World.SEED*3);

	public static void generate(Chunk chunk) {
		
		generateTerrain(chunk);
		generateCaves(chunk);
		generateTrees(chunk);
		bedrock(chunk);
		generateWater(chunk);
	}
	
	private static void bedrock(Chunk chunk) {
		for(int i=0;i<Chunk.WIDTH;i++){
			for(int j=0;j<Chunk.DEPTH;j++){
				chunk.setBlock(BlockData.BEDROCK, i, 0, j);
			}
		}
	}
	
	/**
	 * Returns moisture value between 0 and 1.
	 * @param worldX
	 * @param worldY
	 */
	private static double getMoisture( int worldX, int worldY ) {
		double SCALE = 512;
		double xx = worldX / SCALE;
		double yy = worldY / SCALE;
		return (moistureNoise.eval(xx, yy)+terrainNoise.eval(xx*64, yy*64)*0.1) * 0.5 + 0.5;
	}
	
	private static double slope(int x, int y) {
		double h1 = naturalHeight(x-1, y);
		double h2 = naturalHeight(x+1, y);
		double h3 = naturalHeight(x, y-1);
		double h4 = naturalHeight(x, y+1);
		double h5 = naturalHeight(x, y);

		double average = (h1+h2+h3+h4)/4d;
		double min = Math.min(Math.min(h1, h2), Math.min(h3, Math.min(h4, h5)));
		double max = Math.max(Math.max(h1, h2), Math.max(h3, Math.max(h4, h5)));
		return (max-average)/(min-average);
	}
	
	private static double naturalHeight(int x, int y) {
		final double SCALE = 48;
		final double xx = x / SCALE;
		final double yy = y / SCALE;
		
		// Use noise to generate elevation
		double e =		 1 	* terrainNoise.eval(1 * xx, 1 * yy)
					+  0.5	* terrainNoise.eval(2 * xx, 2 * yy)
					+ 0.25	* terrainNoise.eval(4 * xx, 4 * yy);
		e += 0.125;
		
		// Add some flatness
		double flatNess = terrainNoise.eval(xx / 2d, yy / 2d);
		flatNess = flatNess * 0.5 + 0.5;
		flatNess = Math.pow(flatNess+0.25, mountainNoise.eval(xx / 2d, yy / 2d) + 4);
		e *= flatNess;
		
		// Get moisture
		double M = getMoisture( x, y );
		
		// The less moist it is, the more flat
		e *= Math.pow(M, 2);
		
		// Increase scale
		e *= 32;
		
		// Add some slow height changes
		e += Math.pow(terrainNoise.eval((1/8f) * -xx, (1/8f) * -yy),3)*24;
		
		// Current ground level
		double H = World.SEA_LEVEL + (int)Math.ceil(e);
		return H;
	}
	
	/**
	 * Adds the final water level
	 * @param chunk
	 */
	private static void generateWater(Chunk chunk) {
		int x = chunk.getWorldLocation().getBlockX();
		int y = chunk.getWorldLocation().getBlockZ();

		for(int i=0;i<Chunk.WIDTH;i++){
			for(int j=0;j<Chunk.DEPTH;j++){
				int H = (int) naturalHeight(i+x, j+y);

				for (int k = H-1; k <= World.SEA_LEVEL; k++) {
					if ( chunk.getBlock(i, k, j).getData() != BlockData.AIR )
						continue;
					
					chunk.setBlock(BlockData.WATER, i, k, j);
				}
			}
		}
	}
	
	/**
	 * Generates main terrain
	 * @param chunk
	 */
	private static void generateTerrain(Chunk chunk) {
		int x = chunk.getWorldLocation().getBlockX();
		int y = chunk.getWorldLocation().getBlockZ();

		for(int i=0;i<Chunk.WIDTH;i++){
			for(int j=0;j<Chunk.DEPTH;j++){
				int worldX = i+x;
				int worldZ = j+y;
				
				double H = naturalHeight(worldX, worldZ);
				double M = getMoisture(worldX, worldZ);
				double slope = slope(worldX, worldZ);
				double seaOffset = H - World.SEA_LEVEL;
				
				// Add ground
				for (int a = 0; a <= H; a++) {
					
					if ( slope < 1 && seaOffset-slope <= 1 ) {
						chunk.setBlock(BlockData.SAND, i, a, j);
						continue;
					}
					
					if ( a < H-1 ) {
						chunk.setBlock(BlockData.STONE, i, a, j);
					} else {
						if ( M > 0.3 ) {
							chunk.setBlock(BlockData.DIRT, i, a, j);
						} else {
							if ( a < World.SEA_LEVEL + 12 ) {
								chunk.setBlock(BlockData.SAND, i, a, j);
							} else {
								chunk.setBlock(BlockData.STONE, i, a, j);
							}
						}
					}
				}
				
				// Grass
				if ( chunk.getBlockId(i, (int)H, j) == BlockData.DIRT.getId() && H >= World.SEA_LEVEL ) {
					chunk.setBlock(BlockData.GRASS, i, (int)H, j);
				}
			}
		}
	}
	
	/**
	 * Generates caves within a chunk
	 * @param chunk
	 */
	private static void generateCaves(Chunk chunk) {
		int x = chunk.getWorldLocation().getBlockX();
		int y = chunk.getWorldLocation().getBlockZ();
		double SCALE = 16;

		for(int i=0;i<Chunk.WIDTH;i++) {
			for(int j=0;j<Chunk.DEPTH;j++) {
				for (int k=0; k<Chunk.HEIGHT; k++) {
					double xx = (i + x) / SCALE;
					double yy = (k + 0) / SCALE;
					double zz = (j + y) / SCALE;
					
					double e = terrainNoise.eval(xx, yy, zz);
					double caveFactor = 0.4 + (k / (double)Chunk.HEIGHT) * 0.5;
					
					if ( e > caveFactor ) {
						chunk.setBlock(BlockData.AIR, i, k, j);
					}
				}
			}
		}
	}
	
	/**
	 * Generates trees within a chunk
	 * @param chunk
	 */
	private static void generateTrees(Chunk chunk) {
		int x = chunk.getWorldLocation().getBlockX();
		int y = chunk.getWorldLocation().getBlockZ();
		final double SCALE = 4;
		final int PADDING = 3;
		
		for(int i=-PADDING;i<Chunk.WIDTH+PADDING*2;i++){
			for(int j=-PADDING;j<Chunk.DEPTH+PADDING*2;j++){
				double xx = (i + x) / SCALE;
				double yy = (j + y) / SCALE;
				
				double Moisture = Math.pow(
						getMoisture(x+i, y+j)
						+ 0.1  * terrainNoise.eval(4 * xx, 4 * yy)
						+ 0.1 * terrainNoise.eval(8 * xx, 8 * yy)
						+ 0.1 * treeNoise.eval(12 * xx, 12 * yy)
				,0.5);
				
				// Compute this random e value...
				double e =	   1.0 	* treeNoise.eval(1 * xx, 1 * yy)
							+  0.25	* terrainNoise.eval(2 * xx, 2 * yy)
							+ 0.125	* treeNoise.eval(4 * xx, 4 * yy)
							+ 0.100	* terrainNoise.eval(8 * xx, 8 * yy)
							+ 0.025	* treeNoise.eval(16 * xx, 16 * yy)
							;
				e *= Moisture;
				e += 0.1;
				
				double e2 = treeNoise.eval(16 * xx, 16 * yy)+terrainNoise.eval(32 * xx, 32 * yy);
				
				if ( e > 0.6 && e2 > 0.6) {
					int H = (int) naturalHeight( x + i, y + j );
					if ( H < World.SEA_LEVEL )
						continue;
					
					Block block = chunk.getBlock(i, H, j);
					if ( !block.getData().equals(BlockData.GRASS) )
						continue;
					
					spawnTree(chunk.getWorld(), x+i, H+1 ,y+j);
				}
			}
		}
	}

	/**
	 * Spawns a tree at a specific location.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void spawnTree( World world, int x, int y, int z ) {
		for (int a = 0; a < 5; a++) {
			world.setBlock(BlockData.LOG, x, y + a, z);
		}
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int k = 0; k < 2; k++) {
					if ( i == 0 && j == 0 )
						continue;
					world.setBlock(BlockData.LEAF, x + i, y + 3 + k, z + j);
				}
			}
		}

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = 0; k < 2; k++) {
					world.setBlock(BlockData.LEAF, x + i, y + 5 + k, z + j);
				}
			}
		}
	}
}
