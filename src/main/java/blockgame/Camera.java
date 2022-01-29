package blockgame;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import blockgame.game.Location;
import blockgame.gl.Shader;

public class Camera implements RenderableCallback {
	private float x;
	private float y;
	private float z;
	
	private float fov = 60;
	
	private float yaw;
	private float pitch;

	private Vector3f forwardVector;
	private Vector3f rightVector;
	
	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;
	
	public Camera() {
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();

		this.forwardVector = new Vector3f();
		this.rightVector = new Vector3f();
	}
	
	public Vector3f getPosition() {
		return new Vector3f( x, y, z );
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public void setPosition( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setPosition( Location location ) {
		setPosition( location.getX(), location.getY(), location.getZ() );
	}
	
	public void setPitch(int pitch) {
		this.pitch = pitch;
	}
	
	public void setYaw(int yaw) {
		this.yaw = yaw;
	}

	@Override
	public void render() {
		Shader shader = Application.baseShader;
		
		// Rotate camera
		rotateCamera();

		// Get camera look vector
		float lookX = (float) (Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)));
		float lookY = (float) (Math.sin(Math.toRadians(pitch)));
		float lookZ = (float) (Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)));
		forwardVector.set( lookX, lookY, lookZ );
		rightVector.set( (float)Math.cos(Math.toRadians(yaw+90)), 0, (float)Math.sin(Math.toRadians(yaw+90)) );

		// Move camera
		moveCamera();
		
		// Handle final camera orientation
		viewMatrix.identity().lookAt(x, y, z, x + lookX, y + lookY, z + lookZ, 0, 1, 0);
		projectionMatrix.identity().perspective((float)Math.toRadians(fov), Application.windowWidth/(float)Application.windowHeight, 0.1f, 3200);
		
		shader.setViewMatrix(viewMatrix);
		shader.setProjectionMatrix(projectionMatrix);
		
		if ( GLFW.glfwGetKey(Application.window, GLFW.GLFW_KEY_Q ) == GLFW.GLFW_PRESS ) {
			yaw += 180;
		}
	}
	
	private void rotateCamera() {
		if ( !Application.grabbedMouse )
			return;
		
		yaw += Application.mouseDelta.x * 0.1;
		pitch -= Application.mouseDelta.y * 0.1;
		pitch = (float)Math.max(-89.9, Math.min(89.9, pitch));
	}
	
	private void moveCamera() {
		boolean moveW = GLFW.glfwGetKey(Application.window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
		boolean moveA = GLFW.glfwGetKey(Application.window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
		boolean moveS = GLFW.glfwGetKey(Application.window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
		boolean moveD = GLFW.glfwGetKey(Application.window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
		
		Vector3f finalVector = new Vector3f(0, 0, 0);
		if ( moveW )
			finalVector.add(forwardVector);
		if ( moveD )
			finalVector.add(rightVector);
		if ( moveA )
			finalVector.sub(rightVector);
		if ( moveS )
			finalVector.sub(forwardVector);
		
		if ( finalVector.lengthSquared() > 0 ) {
			finalVector = finalVector.normalize();
			
			float speed = 30;
			
			x = x + finalVector.x * RenderThread.delta * speed;
			y = y + finalVector.y * RenderThread.delta * speed;
			z = z + finalVector.z * RenderThread.delta * speed;
		}
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
}
