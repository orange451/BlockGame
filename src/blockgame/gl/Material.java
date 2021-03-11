package blockgame.gl;

import org.joml.Vector3f;

public class Material {
	private Texture2D diffuseTexture;

	private Vector3f color;
	
	public Material() {
		setDiffuseTexture(null);
		setColor(new Vector3f(1,1,1));
	}
	
	public Material setColor(Vector3f vector) {
		if ( this.color == null ) {
			this.color = new Vector3f(vector);
		}
		this.color.set(vector);
		return this;
	}
	
	public Material setDiffuseTexture(Texture2D texture) {
		this.diffuseTexture = texture;
		return this;
	}	

	public void bind(Shader shader) {
		if ( shader == null )
			return;
		
		// Bind diffuse/albedo
		if ( diffuseTexture != null ) {
			shader.texture_set_stage(shader.diffuseTextureLoc, diffuseTexture, 0);
		}

		// Bind color
		shader.shader_set_uniform_f(shader.shader_get_uniform("uMaterialColor"), color);
	}

}
