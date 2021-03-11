package blockgame;

import blockgame.game.world.World;

public class MainGame implements InitializedRenderableCallback {
	public static World world;
	
	public static int VIEW_DISTANCE = 16;
	
	public static void main(String[] args) {
		new MainGame();
	}
	
	public MainGame() {
		Application.start(1280, 720, "Block Game!", this);
	}
	
	@Override
	public void initialize() {
		world = new World();
		Application.addRenderable(new Resources());
		Application.addRenderable(world);
	}

	@Override
	public void render() {
		System.out.println(RenderThread.fps);
	}
}
