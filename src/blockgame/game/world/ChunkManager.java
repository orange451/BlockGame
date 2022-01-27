package blockgame.game.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import blockgame.Application;
import blockgame.Camera;
import blockgame.MainGame;
import blockgame.game.Location;

public class ChunkManager {
	
	public static final Long RENDER_TIMEOUT = (long) 10000;
	public static final Long UPDATE_TIMEOUT = (long) 250;
	
	public ChunkManager() {
		// Chunk Generation
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				while(true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						//
					}
					
					// Get camera
					Camera camera = Application.camera;
					final Vector3f cameraLocation = camera.getPosition();
					Location loc = new Location(MainGame.world, cameraLocation.x, 0, cameraLocation.z);
					
					// Get chunk location for camera
					int chunkX = loc.getChunkX();
					int chunkZ = loc.getChunkZ();
					int t = MainGame.VIEW_DISTANCE;
					
					// Find all nearby chunks
					List<Chunk> chunksToGenerate = new ArrayList<Chunk>();
					for (int i = chunkX-t; i <= chunkX+t; i++) {
						for (int j = chunkZ-t; j <= chunkZ+t; j++) {
							Chunk currentChunk = MainGame.world.getChunk(i, j);
							
							// Load the chunk into memory if it does not exist
							if ( currentChunk == null ) {
								chunksToGenerate.add(newChunk(loc.getWorld(), i, j));
							}
						}
					}
					
					// Sort based on camera location
					Collections.sort(chunksToGenerate, new Comparator<Chunk>() {
						@Override
						public int compare(Chunk c1, Chunk c2) {
							return (int) (c1.getWorldLocation().toVector3f().distanceSquared(cameraLocation) - c2.getWorldLocation().toVector3f().distanceSquared(cameraLocation));
						}
					});
					
					// Compute camera matrices
					Matrix4f viewMatrix = camera.getViewMatrix();
					Matrix4f projectionMatrix = camera.getProjectionMatrix();
					Matrix4f viewProjectionMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());
					
					// Generate new chunks
					for (int i = 0; i < chunksToGenerate.size(); i++) {
						Chunk chunk = chunksToGenerate.get(i);
						
						if ( isChunkInFrustum( viewProjectionMatrix, chunk) ) {
							loc.getWorld().loadChunk(chunk, true);
						}
					}
				}
			}
			
		}).start();
	}
	
	protected Chunk newChunk(World world, int i, int j) {
		return new Chunk(world, i, j);
	}
	
	protected boolean isChunkNear(Vector3f eyePosition, Chunk chunk) {
		Chunk eyeChunk = MainGame.world.getChunkAt(eyePosition.x, eyePosition.z);
		final float minDist = 2.5f;
		return eyeChunk.getLocation().distanceSquared(chunk.getLocation()) < minDist*minDist;
	}
	
	protected boolean isChunkInFrustum(Matrix4f viewProjectionMatrix, Chunk chunk) {
		Location worldLocation = chunk.getWorldLocation();
		Vector4f worldPosition1 = new Vector4f( worldLocation.getX(), worldLocation.getY() + Chunk.HEIGHT/2, worldLocation.getZ(), 1.0f );
		Vector4f worldPosition2 = new Vector4f( worldLocation.getX() + Chunk.WIDTH, worldLocation.getY() + Chunk.HEIGHT/2, worldLocation.getZ(), 1.0f );
		Vector4f worldPosition3 = new Vector4f( worldLocation.getX() + Chunk.WIDTH, worldLocation.getY() + Chunk.HEIGHT/2, worldLocation.getZ() + Chunk.DEPTH, 1.0f );
		Vector4f worldPosition4 = new Vector4f( worldLocation.getX(), worldLocation.getY() + Chunk.HEIGHT/2, worldLocation.getZ() + Chunk.DEPTH, 1.0f );
		Vector4f worldPosition5 = new Vector4f( worldLocation.getX() + Chunk.WIDTH/2f, worldLocation.getY() + Chunk.HEIGHT/2, worldLocation.getZ() + Chunk.DEPTH/2f, 1.0f );
		
		return testPoint( viewProjectionMatrix, worldPosition1 )
				|| testPoint( viewProjectionMatrix, worldPosition2 )
				|| testPoint( viewProjectionMatrix, worldPosition3 )
				|| testPoint( viewProjectionMatrix, worldPosition4 )
				|| testPoint( viewProjectionMatrix, worldPosition5 );
	}
	
	protected boolean testPoint( Matrix4f viewProjectionMatrix, Vector4f worldPosition ) {
		Vector4f projected = viewProjectionMatrix.transform(worldPosition, new Vector4f());
		return projected.z > 0;
	}
}
