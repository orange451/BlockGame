package blockgame.game;

import org.joml.Vector3f;

import blockgame.game.world.Chunk;
import blockgame.game.world.World;

public class Location {
	private float x;
	private float y;
	private float z;
	private float yaw;
	private float pitch;
	private World world;
	
	public Location( World world, float x, float y, float z ) {
		this(world, x, y, z, 0, 0);
	}
	
	public Location( World world, float x, float y, float z, float yaw, float pitch ) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
	public Location setYaw(float yaw) {
		this.yaw = yaw;
		return this;
	}
	
	public Location setPitch(float pitch) {
		this.pitch = pitch;
		return this;
	}

	public float getX() {
		return this.x;
	}
	public float getY() {
		return this.y;
	}
	public float getZ() {
		return this.z;
	}

	public int getBlockX() {
		return (int)this.x;
	}
	public int getBlockY() {
		return (int)this.y;
	}
	public int getBlockZ() {
		return (int)this.z;
	}
	
	public int getChunkX() {
		return (int)Math.floor(x/(float)Chunk.WIDTH);
	}
	public int getChunkZ() {
		return (int)Math.floor(z/(float)Chunk.DEPTH);
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public Block getBlock() {
		return world.getBlock(getBlockX(), getBlockY(), getBlockZ());
	}
	
	public boolean equals( Location loc ) {
		if ( loc == null )
			return false;
		
		return loc.getX() == x && loc.getY() == y && loc.getZ() == z;
	}

	public float distanceSquared(Location location) {
		return (float) (Math.pow(location.getX()-x, 2) + Math.pow(location.getY()-y, 2) + Math.pow(location.getZ()-z, 2));
	}
	
	public float distance(Location location) {
		return (float) Math.sqrt(distanceSquared(location));
	}

	public Location add(float x, float y, float z) {
		x += x;
		y += y;
		z += z;
		return this;
	}
	
	public Location add(Location location) {
		return this.add( location.getX(), location.getY(), location.getZ() );
	}
	
	public Location subtract( float x, float y, float z ) {
		return add( -x, -y, -z );
	}
	
	public Location subtract( Location location ) {
		return this.add( -location.getX(), -location.getY(), -location.getZ() );
	}
	
	public Chunk getChunk() {
		return world.getChunkAt(getBlockX(), getBlockZ());
	}

	public Vector3f toVector3f() {
		return new Vector3f( x, y, z );
	}
}
