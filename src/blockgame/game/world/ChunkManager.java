package blockgame.game.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import blockgame.Application;
import blockgame.Camera;
import blockgame.MainGame;
import blockgame.game.Location;

public class ChunkManager {
	
	private HashMap<Chunk, Long> chunkTimeout = new HashMap<Chunk, Long>();
	
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
					ArrayList<Chunk> chunksToGenerate = new ArrayList<Chunk>();
					for (int i = chunkX-t; i <= chunkX+t; i++) {
						for (int j = chunkZ-t; j <= chunkZ+t; j++) {
							Chunk currentChunk = MainGame.world.getChunk(i, j);
							
							// Load the chunk into memory if it does not exist
							if ( currentChunk == null ) {
								chunksToGenerate.add(new Chunk(loc.getWorld(), i, j));
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
		
		// Chunk Mesh Generation
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						//
					}
					
					// Get camera
					Camera camera = Application.camera;
					final Vector3f cameraLocation = camera.getPosition();
					
					// Get list of chunks
					ArrayList<Chunk> chunks = MainGame.world.getLoadedChunks();
					
					// Copy chunk list
					ArrayList<Chunk> sortedChunks = new ArrayList<Chunk>();
					sortedChunks.addAll(chunks);
					
					// Sort based on camera location
					Collections.sort(sortedChunks, new Comparator<Chunk>() {
						@Override
						public int compare(Chunk c1, Chunk c2) {
							return (int) (c1.getWorldLocation().toVector3f().distanceSquared(cameraLocation) - c2.getWorldLocation().toVector3f().distanceSquared(cameraLocation));
						}
					});
					
					Matrix4f viewMatrix = camera.getViewMatrix();
					Matrix4f projectionMatrix = camera.getProjectionMatrix();
					Matrix4f viewProjectionMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());
					
					long start = System.currentTimeMillis();
					
					// Update
					for (int i = 0; i < sortedChunks.size(); i++) {
						Chunk c = sortedChunks.get(i);
						
						// Project chunk position onto screen (3d --> 2d)
						boolean isInView = isChunkInFrustum(viewProjectionMatrix, c) || isChunkNear(camera.getPosition(), c);
						
						// If chunk is too far away, not visible
						if ( c.getWorldLocation().toVector3f().distanceSquared(camera.getPosition().sub(0, camera.getPosition().y, 0)) > Math.pow(MainGame.VIEW_DISTANCE * Chunk.WIDTH, 2) )
							isInView = false;
						
						// Start culling checks
						if ( c.loaded() ) {
							
							// Unload chunks if they've been invisible for too long
							if ( chunkTimeout.containsKey(c) /*&& System.currentTimeMillis() > chunkTimeout.get(c)*/ ) {
								c.unload();
								chunkTimeout.remove(c);
								continue;
							}

							// Mark chunk as not in view
							if ( !isInView ) {
								
								// Add chunk to list of chunks waiting to time-out
								if ( !chunkTimeout.containsKey( c ) ) {
									chunkTimeout.put(c, System.currentTimeMillis() + RENDER_TIMEOUT);
								}
							} else {
								// This chunk is visible.
								chunkTimeout.remove(c);
							}
						} else {
							chunkTimeout.remove(c);
						}
						
						// Update chunk
						if ( c.updated && isInView ) {
							c.generateMesh();
						}
						
						// If we're taking a long time to finish updating, break so that we can re-order chunks.
						if ( System.currentTimeMillis() - start > UPDATE_TIMEOUT )
							break;
					}
				}
			}
		}).start();
	}
	
	private boolean isChunkNear(Vector3f eyePosition, Chunk chunk) {
		Chunk eyeChunk = MainGame.world.getChunkAt(eyePosition.x, eyePosition.z);
		final float minDist = 2.5f;
		return eyeChunk.getLocation().distanceSquared(chunk.getLocation()) < minDist*minDist;
	}
	
	private boolean isChunkInFrustum(Matrix4f viewProjectionMatrix, Chunk chunk) {
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
	
	private boolean testPoint( Matrix4f viewProjectionMatrix, Vector4f worldPosition ) {
		Vector4f projected = viewProjectionMatrix.transform(worldPosition, new Vector4f());
		return projected.z > 0;
	}
}
