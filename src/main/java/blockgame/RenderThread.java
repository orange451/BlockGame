package blockgame;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import blockgame.etc.Sync;

public class RenderThread {
	public static float delta;
	public static float fps = 1;
	public static int desiredFPS = 1000;
	
	private int fpscounter;
	
	private List<RenderableCallback> callbacks;
	
	private List<Initializable> toInitialize;
	
	public RenderThread() {
		this.callbacks = new ArrayList<RenderableCallback>();
		this.toInitialize = new ArrayList<Initializable>();
	}
	
	public void addCallback(RenderableCallback callback) {
		synchronized(callbacks) {
			callbacks.add(callback);
		}
		
		if ( callback instanceof Initializable ) {
			synchronized(toInitialize) {
				toInitialize.add((Initializable) callback);
			}
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
		
		// Initialize
		synchronized(toInitialize) {
			for (int i = 0; i < toInitialize.size(); i++) {
				toInitialize.get(i).initialize();
			}
			toInitialize.clear();
		}
		
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
