package blockgame.game;

import java.util.HashMap;
import java.util.Map;

public enum BlockData {
	GRASS(1, new TextureInfo(TextureType.ALL, 3,0), new TextureInfo(TextureType.TOP, 0,0), new TextureInfo(TextureType.BOTTOM, 2,0)),
	STONE(2, new TextureInfo(TextureType.ALL, 1,0)),
	DIRT(3, new TextureInfo(TextureType.ALL, 2,0)),
	LOG(4, new TextureInfo(TextureType.ALL, 4,1), new TextureInfo(TextureType.TOP, 5,1), new TextureInfo(TextureType.BOTTOM, 5,1)),
	LEAF(5, params().setOcclude(false), new TextureInfo(TextureType.ALL, 4,3)),
	SAND(6, new TextureInfo(TextureType.ALL, 2,1)),
	BEDROCK(7, new TextureInfo(TextureType.ALL, 1,1)),
	WATER(8, new TextureInfo(TextureType.ALL, 0,9)),
	AIR(0, params().setSolid(false).setOcclude(false));
	
	private byte id;
	private BlockParams params;
	private TextureInfo[] textureInformation;
	
	private static Map<Byte, BlockData> blockMap;
	
	static {
		blockMap = new HashMap<>();
		for (BlockData data : values()) {
			blockMap.put(data.id, data);
		}
	}
	
	BlockData( int id, TextureInfo...infos ) {
		this(id, params(), infos);
	}

	BlockData( int id, BlockParams params, TextureInfo...infos ) {
		this.textureInformation = infos;
		this.params = params;
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
		return blockMap.get(blockId);
	}
	
	public static boolean isSolid(BlockData block) {
		return block.params.solid;
	}
	
	public static boolean isOcclude(BlockData block) {
		return block.params.occlude;
	}
	
	private static BlockParams params() {
		return new BlockParams();
	}
}

class BlockParams {
	boolean solid = true;
	boolean occlude = true;

	public BlockParams setSolid(boolean b) {
		this.solid = b;
		return this;
	}

	public BlockParams setOcclude(boolean b) {
		this.occlude = b;
		return this;
	}
}
