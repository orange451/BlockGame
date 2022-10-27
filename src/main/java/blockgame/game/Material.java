package blockgame.game;

import blockgame.game.material.GrassBlock;
import blockgame.game.material.MaterialData;

public enum Material {
	
	public static final Material GRASS = new GrassBlock();
	
	public abstract boolean isBlock();
	
	public abstract boolean isSolid();
	
	public abstract boolean isAir();
	
	public abstract boolean isOccluding();
	
	public abstract boolean isTransparent();
	
	public abstract byte getId();
	
	public abstract int getMaxStackSize();
	
	public abstract MaterialData getData();
}
