package blockgame.gl;

import static org.lwjgl.opengl.GL11.GL_R;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL21;

import blockgame.etc.Color;
import blockgame.etc.Image;

public class TextureUtils {
	
	public static Texture2D loadRGBATexture(String filename) {
		return loadTexture(filename, GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST, false);
	}

	public static Texture2D loadSRGBTexture(String filename) {
		return loadTexture(filename, GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST, true);
	}

	public static Texture2D loadSRGBTextureFromImage(Image image) {
		return loadTexture(image, GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST, true);
	}

	public static Texture2D loadRGBATextureFromImage(Image image) {
		return loadTexture(image, GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST, false);
	}
	
	public static Texture2D loadTexture(String filename, int internalFormat, int externalFormat) {
		return loadTexture( loadImage(filename), GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST, internalFormat, externalFormat);
	}
	
	public static Image loadImage( String path ) {
		if ( path == null || path.length() == 0 || path.contains("NULL") )
			return new Image(Color.WHITE,1,1);
		
		Image image = new Image( path );
		return image;
	}
	
	private static Texture2D loadTexture(Image image, int near, int far, int internalFormat, int externalFormat) {
		// Create new texture
		Texture2D texture = new Texture2D(GL_TEXTURE_2D, internalFormat, GL_UNSIGNED_BYTE, externalFormat);
		
		// Generate texture
		texture.gen();
		
		// Bind
		texture.bind();
		
		// Load image to texture
		texture.load(0, image.getWidth(), image.getHeight(), image.getData());
		
		// Clamp
		GL11.glTexParameterf(texture.getTarget(), GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(texture.getTarget(), GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		// Set filters
		if (near > -1 && far > -1)
			texture.setMipMapFilter(near, far);
		
		// Generate mipmaps
		texture.generateMipmaps();
		
		return texture;
	}
	
	private static Texture2D loadTexture(Image image, int near, int far, boolean srgb) {
		// Get format
		int internalFormat = srgb?GL21.GL_SRGB8_ALPHA8:GL_RGBA;
		int externalFormat = GL_RGBA;
		if (image.getComponents() == 3) {
			internalFormat = srgb?GL21.GL_SRGB8:GL_RGB;
			externalFormat = GL_RGB;
		}
		if (image.getComponents() == 1) {
			internalFormat = GL_R;
			externalFormat = GL_R;
		}
		
		return loadTexture(image, near, far, internalFormat, externalFormat);
	}
	
	private static Texture2D loadTexture(String filename, int near, int far, boolean srgb) {
		Image image = loadImage(filename);
		if ( image == null )
			return null;
		
		return loadTexture(image, near, far, srgb);
	}
	
    public static ByteBuffer loadImageBuffer(Image image, int x, int y, int width, int height, PixelHandler handler, ByteBuffer buffer) {
        int[] pixels = image.getDataArray();

        if(buffer == null){
            buffer = BufferUtils.createByteBuffer(width * height * handler.getBytesPerPixel());
        }

        int startY = y * image.getWidth();
        int startX = x;
        for(int yy = 0; yy < height; yy++){
        	int layer = startY + (yy * image.getWidth());
            for(int xx = startX; xx < startX + width; xx++){
            	int pix = pixels[layer + xx];
                handler.handlePixel(buffer, pix);
            }
        }

        buffer.flip();
        return buffer;
    }
}
