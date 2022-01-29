package blockgame.game.world.client;

import blockgame.RenderableCallback;
import blockgame.game.world.ChunkManager;
import blockgame.game.world.World;

public class ClientWorld extends World implements RenderableCallback {

	@Override
	protected ChunkManager createChunkManager() {
		return new ClientChunkManager();
	}
	
	@Override
	public void render() {
		for (int i = 0; i < chunks.size(); i++) {
			if ( i >= chunks.size() )
				continue;
			
			ClientChunk c = (ClientChunk) chunks.get(i);
			if ( c == null )
				continue;
			c.render();
		}
	}
}
