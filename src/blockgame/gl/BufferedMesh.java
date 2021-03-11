package blockgame.gl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class BufferedMesh {
	private int vaoId = -1;
	private int vboId = -1;
	private Vertex[] vertices;
	private int size;
	private boolean modified;
	
	public static boolean discardVerticesOnUpload = true;

	public BufferedMesh(int vertices) {
		resize(vertices);
	}

	/**
	 * Set a vertex at a given index for this mesh.
	 * @param index
	 * @param vertex
	 */
	public void setVertex(int index, Vertex vertex) {
		modified = true;
		vertices[index] = vertex;
	}

	/**
	 * Get the vertex at a given index.
	 * @param i
	 * @return
	 */
	public Vertex getVertex(int i) {
		return vertices[i];
	}

	/**
	 * Return the array of vertices for this mesh.
	 * @return
	 */
	public Vertex[] getVertices() {
		return this.vertices;
	}

	/**
	 * Change the amount of vertices used in this mesh.
	 * @param size
	 */
	public void resize(int size) {
		// Store old vertices.
		Vertex[] old = vertices;

		// Make new vertex list.
		vertices = new Vertex[size];
		modified = true;
		
		// Try to fill old vertices into new list.
		if ( old != null ) {
			System.arraycopy(old, 0, vertices, 0, Math.min(size, old.length));
		}
	}

	/**
	 * Return the amount of vertices in the mesh
	 * @return
	 */
	public int getSize() {
		if ( vertices == null )
			return 0;
		return vertices.length;
	}

	protected void sendToGPU() {
		// Initial vertex data
		float[] buffer = null;
		
		synchronized(vertices) {
			int len = vertices.length;
			buffer = new float[len * Vertex.elementCount];
			for (int i = 0; i < len; i++) {
				float[] elements = vertices[i].getElements();
				System.arraycopy(elements, 0, buffer, i*Vertex.elementCount, elements.length);
			}
		}

		// Generate buffers
		if ( vboId == -1 )
			vboId = glGenBuffers();
		
		if ( vaoId == -1 )
			vaoId = glGenVertexArrays();

		// Upload Vertex Buffer
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

		// Set attributes (automatically stored to currently bound VAO)
		bindVertexAttributes();

		// Unbind
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Clear buffer (Garbage collect)
		buffer = null;

		// Reset modified flag
		size = vertices.length;
		modified = false;
		
		if ( discardVerticesOnUpload ) {
			vertices = null;
		}
	}

	protected void bindVertexAttributes() {
		// Enable the attributes
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);

		// Define the attributes
		boolean normalized = false;
		glVertexAttribPointer(0, Vertex.positionElementCount, GL_FLOAT, normalized, Vertex.stride, Vertex.positionByteOffset);
		glVertexAttribPointer(1, Vertex.normalElementCount, GL_FLOAT,  normalized, Vertex.stride, Vertex.normalByteOffset);
		glVertexAttribPointer(2, Vertex.textureElementCount, GL_FLOAT,  normalized, Vertex.stride, Vertex.textureByteOffset);
		glVertexAttribPointer(3, Vertex.colorElementCount, GL_FLOAT, normalized, Vertex.stride, Vertex.colorByteOffset);
	}

	/**
	 * Bind this VAO to the OpenGL state.
	 */
	public void bind() {
		glBindVertexArray(vaoId);
	}

	/**
	 * Unbind the VAO from the OpenGL state.
	 */
	public void unbind() {
		GL30.glBindVertexArray(0);
	}

	/**
	 * Cleans up all resources used by the BufferedMesh. After cleaning, the mesh can no longer be drawn.
	 * It must be re-filled with vertex data.
	 */
	public void cleanup() {
		vertices = null;
		modified = true;
		destroyBuffers();
	}
	
	/**
	 * Delete this mesh's VBO and VAO from VRAM.
	 */
	private void destroyBuffers() {
		if ( vboId > -1 ) {
			GL15.glDeleteBuffers(vboId);
			System.out.println("Destroyed Buffer");
		}
		
		if ( vaoId > -1 )
			GL30.glDeleteVertexArrays(vaoId);
		
		vboId = -1;
		vaoId = -1;
	}

	/**
	 * Render this mesh at a given world matrix with a given material.
	 * @param genericShader
	 * @param worldMatrix
	 * @param material
	 */
	public void render(Shader shader, Matrix4f worldMatrix, Material material) {
		if ( modified ) {
			sendToGPU();
		}

		// Bind the material for drawing
		if ( material != null ) {
			material.bind(shader);
		}

		// Bind the VAO for drawing
		this.bind();

		// Set world matrix
		if ( worldMatrix != null ) {
			shader.setWorldMatrix(worldMatrix);
		}

		// Draw
		glDrawArrays(GL_TRIANGLES, 0, size);

		// Unbind VAO
		this.unbind();
	}
}
