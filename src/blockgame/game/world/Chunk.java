package blockgame.game.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import blockgame.Application;
import blockgame.Resources;
import blockgame.game.Block;
import blockgame.game.BlockData;
import blockgame.game.Location;
import blockgame.game.TextureInfo;
import blockgame.game.TextureType;
import blockgame.gl.BufferedMesh;
import blockgame.gl.Vertex;

public class Chunk {
	public static final int WIDTH = 16;
	public static final int DEPTH = 16;
	public static final int HEIGHT = 128;
	
	protected boolean updated = false;
	
	private BufferedMesh mesh;
	private boolean loaded;
	protected BufferedMesh queuedMesh;
	
	private byte[] blocks = new byte[WIDTH * DEPTH * HEIGHT];
	
	private int x;
	private int y;
	private World world;
	
	private double tOff = -HEIGHT;
	
	public Chunk(World world, int x, int y) {
		this.x = x;
		this.y = y;
		this.world = world;
		
		// Initial loop. Set all to air
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				for (int k = 0; k < DEPTH; k++) {
					setBlock(BlockData.AIR, i, j, k);
				}
			}
		}
	}
	
	public void setBlock( BlockData block, int x, int y, int z ) {
		setBlock( (byte) block.getId(), x, y, z );
	}
	
	public void setBlock( byte blockId, int x, int y, int z ) {
		if ( x < 0 )
			return;
		if ( y < 0 )
			return;
		if ( z < 0 )
			return;
		if ( x >= WIDTH )
			return;
		if ( y >= HEIGHT )
			return;
		if ( z >= DEPTH )
			return;
		
		this.blocks[(x << 11 | z << 7 | y)] = blockId;
		updated = true;
	}
	
	public Block getBlock( int x, int y, int z ) {
		return new Block(world, getBlockId(x, y, z), x, y, z);
	}
	
	public byte getBlockId( int x, int y, int z ) {
		if ( x < 0 )
			return (byte) BlockData.AIR.getId();
		if ( x >= WIDTH )
			return (byte) BlockData.AIR.getId();
		
		if ( y < 0 )
			return (byte) BlockData.AIR.getId();
		if ( y >= HEIGHT )
			return (byte) BlockData.AIR.getId();
		
		if ( z < 0 )
			return (byte) BlockData.AIR.getId();
		if ( z >= DEPTH )
			return (byte) BlockData.AIR.getId();
		
		return this.blocks[(x << 11 | z << 7 | y)];
	}
	
	protected void generateMesh() {
		if ( !updated )
			return;
		
		loaded = true;
		updated = false;
		
		Vertex[] tempVerts = new Vertex[(WIDTH * DEPTH * HEIGHT) * 36];
		int vertsAdded = 0;
		int facesAdded = 0;

		Vector3f t1 = new Vector3f();
		Vector3f t2 = new Vector3f();
		Vector3f t3 = new Vector3f();
		Vector3f t4 = new Vector3f();
		Vector3f tn = new Vector3f();
		
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				for (int k = 0; k < DEPTH; k++) {
					byte b = getBlockId( i, j, k );
					
					BlockData blockType = BlockData.getBlockData(b);
					
					TextureInfo[] tinfo = blockType.getTextureInformation();
					
					// Do not draw air
					if ( blockType.equals(BlockData.AIR) )
						continue;
					
					// Get texture information for each face
					TextureInfo all = BlockData.getTextureInfoByType( tinfo, TextureType.ALL );
					TextureInfo top = BlockData.getTextureInfoByType( tinfo, TextureType.TOP );
					TextureInfo bottom = BlockData.getTextureInfoByType( tinfo, TextureType.BOTTOM );
					TextureInfo left = BlockData.getTextureInfoByType( tinfo, TextureType.LEFT );
					TextureInfo right = BlockData.getTextureInfoByType( tinfo, TextureType.RIGHT );
					TextureInfo front = BlockData.getTextureInfoByType( tinfo, TextureType.FRONT );
					TextureInfo back = BlockData.getTextureInfoByType( tinfo, TextureType.BACK );

					// default to ALL, if explicit face is not defined
					top = top == null ? all : top;
					bottom = bottom == null ? all : bottom;
					left = left == null ? all : left;
					right = right == null ? all : right;
					front = front == null ? all : front;
					back = back == null ? all : back;

					// Compute world coordinates
					int worldx = x * WIDTH + i;
					int worldy = j;
					int worldz = y * DEPTH + k;
					
					// Compute needing face
					boolean needTop = world.getBlockId( worldx, worldy + 1, worldz ) == BlockData.AIR.getId();
					boolean needBottom = world.getBlockId( worldx, worldy - 1, worldz ) == BlockData.AIR.getId();
					boolean needLeft = world.getBlockId( worldx - 1, worldy, worldz ) == BlockData.AIR.getId();
					boolean needRight = world.getBlockId( worldx + 1, worldy, worldz ) == BlockData.AIR.getId();
					boolean needFront = world.getBlockId( worldx, worldy, worldz + 1 ) == BlockData.AIR.getId();
					boolean needBack = world.getBlockId( worldx, worldy, worldz - 1 ) == BlockData.AIR.getId();

					// Create new mesh
					if ( needBottom )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(0, 0, 0).add(i, j, k), t2.zero().add(1, 0, 0).add(i, j, k), t3.zero().add(1, 0, 1).add(i, j, k), t4.zero().add(0, 0, 1).add(i, j, k), tn.zero().add(0, -1, 0), bottom); // Bottom
					
					if ( needTop )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(1, 1, 0).add(i, j, k), t2.zero().add(0, 1, 0).add(i, j, k), t3.zero().add(0, 1, 1).add(i, j, k), t4.zero().add(1, 1, 1).add(i, j, k), tn.zero().add(0, 1, 0), top); // Top
					
					if ( needLeft )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(0, 1, 1).add(i, j, k), t2.zero().add(0, 1, 0).add(i, j, k), t3.zero().add(0, 0, 0).add(i, j, k), t4.zero().add(0, 0, 1).add(i, j, k), tn.zero().add(-1, 0, 0), left); // Left
					
					if ( needRight )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(1, 1, 0).add(i, j, k), t2.zero().add(1, 1, 1).add(i, j, k), t3.zero().add(1, 0, 1).add(i, j, k), t4.zero().add(1, 0, 0).add(i, j, k), tn.zero().add(1, 0, 0), right); // Right
					
					if ( needFront )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(1, 1, 1).add(i, j, k), t2.zero().add(0, 1, 1).add(i, j, k), t3.zero().add(0, 0, 1).add(i, j, k), t4.zero().add(1, 0, 1).add(i, j, k), tn.zero().add(0, 0, 1), front); // Front
					
					if ( needBack )
						vertsAdded += createFace(facesAdded++, tempVerts, t1.zero().add(0, 1, 0).add(i, j, k), t2.zero().add(1, 1, 0).add(i, j, k), t3.zero().add(1, 0, 0).add(i, j, k), t4.zero().add(0, 0, 0).add(i, j, k), tn.zero().add(0, 0, -1), back); // Back
				}
			}
		}
		
		// Create new mesh
		BufferedMesh tempMesh = new BufferedMesh(vertsAdded);
		Vertex[] finalVert = tempMesh.getVertices();
		System.arraycopy(tempVerts, 0, finalVert, 0, vertsAdded);
		tempVerts = null;
		
		// Clean old mesh
		this.queuedMesh = tempMesh;
	}
	
	protected void unload() {
		this.updated = true;
		this.loaded = false;
	}
	
	private int createFace(int index, Vertex[] vertArray, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector3f normal, TextureInfo tinfo) {		
		float t = 16;
		
		vertArray[(index * 6) + 0] = new Vertex( v1.x, v1.y, v1.z, normal.x, normal.y, normal.z, (tinfo.getS()+0)/t, (tinfo.getT()+0)/t );
		vertArray[(index * 6) + 1] = new Vertex( v2.x, v2.y, v2.z, normal.x, normal.y, normal.z, (tinfo.getS()+1)/t, (tinfo.getT()+0)/t );
		vertArray[(index * 6) + 2] = new Vertex( v3.x, v3.y, v3.z, normal.x, normal.y, normal.z, (tinfo.getS()+1)/t, (tinfo.getT()+1)/t );

		vertArray[(index * 6) + 3] = new Vertex( v3.x, v3.y, v3.z, normal.x, normal.y, normal.z, (tinfo.getS()+1)/t, (tinfo.getT()+1)/t );
		vertArray[(index * 6) + 4] = new Vertex( v4.x, v4.y, v4.z, normal.x, normal.y, normal.z, (tinfo.getS()+0)/t, (tinfo.getT()+1)/t );
		vertArray[(index * 6) + 5] = new Vertex( v1.x, v1.y, v1.z, normal.x, normal.y, normal.z, (tinfo.getS()+0)/t, (tinfo.getT()+0)/t );
		
		return 6;
	}
	
	public boolean loaded() {
		return loaded;
	}
	
	public void render() {
		if ( this.queuedMesh != null ) {
			BufferedMesh old = this.mesh;
			this.mesh = this.queuedMesh;
			this.queuedMesh = null;
			
			if ( old != null )
				old.cleanup();
		}
		
		// Must have mesh
		if ( this.mesh == null )
			return;
		
		// Chunk animation
		if ( !loaded ) {
			this.tOff = tOff + (-HEIGHT-tOff)*0.001;
			if ( tOff <= -HEIGHT * 0.6 )
				return;
		} else {
			tOff *= 0.99f;	
		}
		
		// Render
		this.mesh.render(Application.baseShader, new Matrix4f().translate(x * WIDTH, (float) tOff, y * DEPTH), Resources.terrainMaterial);
	}

	public Location getLocation() {
		return new Location( world, x, 0, y );
	}
	
	public Location getWorldLocation() {
		return new Location( world, x * WIDTH, 0, y * DEPTH );
	}

	public World getWorld() {
		return this.world;
	}

	public int getGroundLevel(int x, int z) {
		for (int i = 0; i < HEIGHT; i++) {
			byte block = getBlockId( x, i, z );
			if ( block == BlockData.AIR.getId() ) {
				return i;
			}
		}
		
		return HEIGHT-1;
	}
}
