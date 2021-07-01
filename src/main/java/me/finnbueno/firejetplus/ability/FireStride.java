package me.finnbueno.firejetplus.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import me.finnbueno.firejetplus.config.ConfigValue;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import me.finnbueno.firejetplus.util.FireUtil;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

/**
 * @author Finn Bon
 */
public class FireStride extends OverriddenFireAbility implements AddonAbility {

	private Permission perm;

	private static final long LAUNCH_DURATION = 100;
	private static final long MOVE_DURATION = 1000;
	@ConfigValue()
	private double speed = 1.2;
	@ConfigValue()
	private long cooldown = 1500;

	private Vector direction;

	public FireStride(Player player) {
		super(player);
		ConfigValueHandler.get().setFields(this);

		FireStride fs = getAbility(player, getClass());
		if (fs != null) {
			fs.remove();
			return;
		}

		double speed = this.speed;
		if (!isTouchingGround()) {
			speed *= .85;
		}
		this.direction = player.getLocation().getDirection().multiply(speed);
		bPlayer.addCooldown(this);
		playFirebendingSound(getLocation());
		start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > getStartTime() + MOVE_DURATION) {
			remove();
			return;
		}
		if (getStartTime() + LAUNCH_DURATION > System.currentTimeMillis()) {
			player.setVelocity(this.direction);
		} else if (isTouchingGround()) {
			remove();
			return;
		}

		player.setFallDistance(0);
		display();
	}

	public boolean attemptFireSki() {
		if (new FireSki(player).isStarted()) {
			remove();
			return true;
		}
		return false;
	}

	private boolean isTouchingGround() {
		return GeneralMethods.isSolid(player.getLocation().subtract(0, 0.075, 0).getBlock());
	}

	private void display() {
		Particle particle = bPlayer.hasSubElement(Element.SubElement.BLUE_FIRE) ? Particle.SOUL_FIRE_FLAME : Particle.FLAME;
		int amount = 10;
		double offset = .8;
		for (int i = 0; i < amount; i++) {
			Location loc = this.player.getLocation().add((Math.random() - .5) * offset, 1 + (Math.random() - .5) * offset, (Math.random() - .5) * offset);
			Vector particleDirection = this.direction.clone().normalize().multiply(-.7);
			particleDirection = FireUtil.randomizeVector(particleDirection);
			this.player.getWorld()
				.spawnParticle(particle, loc, 0, particleDirection.getX(), particleDirection.getY(), particleDirection.getZ());
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "FireDash";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {
		perm = new Permission("bending.ability." + getName());
		perm.setDefault(PermissionDefault.TRUE);
		ConfigValueHandler.get().registerDefaultValues(this, "FireJet");
	}

	@Override
	public void stop() {
		Bukkit.getServer().getPluginManager().removePermission(perm);
	}

	@Override
	public String getAuthor() {
		return FireJet.AUTHOR;
	}

	@Override
	public String getVersion() {
		return FireJet.VERSION;
	}
}
