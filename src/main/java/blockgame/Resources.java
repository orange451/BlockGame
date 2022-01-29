package blockgame;

import blockgame.gl.Material;
import blockgame.gl.Texture2D;
import blockgame.gl.TextureUtils;

public class Resources implements Initializable, RenderableCallback {
	public static Texture2D terrain;
	public static Material terrainMaterial;
	
	@Override
	public void initialize() {
		terrain = TextureUtils.loadRGBATexture("blockgame/terrain.png");
		
		terrainMaterial = new Material();
		terrainMaterial.setDiffuseTexture(Resources.terrain);
	}
	
	@Override
	public void render() {
		//
	} 
}
