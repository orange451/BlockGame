package blockgame.game;

public class TextureInfo {
	private int s;
	private int t;
	private TextureType type;
	
	public TextureInfo( TextureType type, int s, int t ) {
		this.s = s;
		this.t= t;
		this.type = type;
	}
	
	public int getS() {
		return s;
	}
	
	public int getT() {
		return t;
	}
	
	public TextureType getType() {
		return type;
	}
}
