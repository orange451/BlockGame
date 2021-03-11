package blockgame.game;

public enum BlockData {
	GRASS(1, new TextureInfo(TextureType.ALL, 3,0), new TextureInfo(TextureType.TOP, 0,0), new TextureInfo(TextureType.BOTTOM, 2,0)),
	STONE(2, new TextureInfo(TextureType.ALL, 1,0)),
	DIRT(3, new TextureInfo(TextureType.ALL, 2,0)),
	LOG(4, new TextureInfo(TextureType.ALL, 4,1), new TextureInfo(TextureType.TOP, 5,1), new TextureInfo(TextureType.BOTTOM, 5,1)),
	LEAF(5, new TextureInfo(TextureType.ALL, 4,3)),
	SAND(6, new TextureInfo(TextureType.ALL, 2,1)),
	BEDROCK(7, new TextureInfo(TextureType.ALL, 1,1)),
	WATER(8, new TextureInfo(TextureType.ALL, 0,9)),
	AIR(0);
	
	private byte id;
	private TextureInfo[] textureInformation;
	
	BlockData( int id, TextureInfo...infos ) {
		this.textureInformation = infos;
		this.id = (byte) (id & 0xFF);
	}
	
	public TextureInfo[] getTextureInformation() {
		return textureInformation;
	}

	public static TextureInfo getTextureInfoByType( TextureInfo[] tinfo, TextureType type ) {
		for (int i = 0; i < tinfo.length; i++) {
			TextureInfo t = tinfo[i];
			if ( t.getType().equals(type) ) {
				return t;
			}
		}
		return null;
	}
	
	public byte getId() {
		return id;
	}

	public static BlockData getBlockData(byte blockId) {
		for(BlockData block : BlockData.values()) {
			if ( block.getId() == blockId )
				return block;
		}
		return null;
	}
	
	public static boolean isSolid(BlockData block) {
		if ( block.equals(BlockData.AIR) )
			return false;
		
		return true;
	}
}
