package blockgame.game.world.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import blockgame.Application;
import blockgame.Camera;
import blockgame.MainGame;
import blockgame.game.world.Chunk;
import blockgame.game.world.ChunkManager;
import blockgame.game.world.World;

public class ClientChunkManager extends ChunkManager {
	
	private Map<Chunk, Long> chunkTimeout = new HashMap<Chunk, Long>();

	public ClientChunkManager() {
		super();
		
		// Chunk Mesh Generation Thread
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
					List<Chunk> chunks = MainGame.world.getLoadedChunks();
					
					// Copy chunk list
					List<ClientChunk> sortedChunks = new ArrayList<ClientChunk>();
					synchronized(chunks) {
						for (Chunk chunk : chunks) {
							sortedChunks.add((ClientChunk)chunk);
						}
					}
					
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
						ClientChunk c = sortedChunks.get(i);
						
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
						if ( c.isUpdated() && isInView ) {
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
	
	@Override
	protected Chunk newChunk(World world, int i, int j) {
		return new ClientChunk(world, i, j);
	}
}
