package blockgame;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import blockgame.etc.Sync;

public class RenderThread {
	public static float delta;
	public static float fps = 1;
	public static int desiredFPS = 1000;
	
	private int fpscounter;
	
	private ArrayList<RenderableCallback> callbacks;
	
	public RenderThread() {
		this.callbacks = new ArrayList<RenderableCallback>();
	}
	
	public void addCallback(RenderableCallback callback) {
		synchronized(callbacks) {
			callbacks.add(callback);
		}
		
		if ( callback instanceof InitializedRenderableCallback ) {
			((InitializedRenderableCallback)callback).initialize();
		}
	}
	
	public void run() {
		long window = Application.window;
		long startTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		double nanoSecond = 1e+9;

		while ( !GLFW.glfwWindowShouldClose(window) ) {
			forceUpdate();

			// Calculate FPS
			fpscounter++;
			if (System.currentTimeMillis() - timer >= 1000) {
				fps = fpscounter;
				
				timer = System.currentTimeMillis();
				fpscounter = 0;
			}

			// Calculate delta
			delta = (float) ((System.nanoTime() - startTime)/nanoSecond);
			startTime = System.nanoTime();

			// Sync
			Sync.sync( desiredFPS );
		}

		glfwDestroyWindow(window);
		glfwTerminate();

		System.exit(0);
	}

	protected void forceUpdate() {
		// Set the clear color
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		// Render callbacks
		synchronized(callbacks) {
			for (int i = 0; i < callbacks.size(); i++) {
				callbacks.get(i).render();
			}
		}

		// Send queue to GPU
		GLFW.glfwSwapBuffers(Application.window);

		// Poll mouse/keyboard
		GLFW.glfwPollEvents();
	}
	
}
