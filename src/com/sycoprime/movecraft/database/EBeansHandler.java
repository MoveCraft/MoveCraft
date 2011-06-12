package com.sycoprime.movecraft.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.validation.NotNull;

@Entity()
@CacheStrategy
@Table(name = "minecartowners")
public class EBeansHandler {

	/*
	import com.afforess.minecartmaniacore.utils.StringUtils;
	import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
	*/
	public class MinecartOwner {
		@Id
		private int id;
		@NotNull
		private String owner;
		@NotNull
		private String world;

		public MinecartOwner() {
			this.owner = "none";
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public MinecartOwner(String owner) {
			this.owner = owner;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public void setWorld(String world) {
			this.world = world;
		}

		public String getWorld() {
			return world;
		}
		
		public World getBukkitWorld() {
			return Bukkit.getServer().getWorld(world);
		}

		/*
		public Object getRealOwner() {
			if (owner.equals("none")) {
				return null;
			}
			if (owner.contains("[") && owner.contains("]")) {
				try {
					int x, y, z;
					String[] split = owner.split(":");
					x = Integer.valueOf(StringUtils.getNumber(split[0]));
					y = Integer.valueOf(StringUtils.getNumber(split[1]));
					z = Integer.valueOf(StringUtils.getNumber(split[2]));
					if (MinecartManiaWorld.getBlockAt(getBukkitWorld(), x, y, z).getState() instanceof Chest) {
						return MinecartManiaWorld.getMinecartManiaChest((Chest)MinecartManiaWorld.getBlockAt(getBukkitWorld(), x, y, z).getState());
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			return Bukkit.getServer().getPlayer(owner);
		}
			*/


		

	}

}
