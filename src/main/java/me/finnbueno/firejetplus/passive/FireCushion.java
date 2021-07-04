package me.finnbueno.firejetplus.passive;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.config.ConfigValue;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import me.finnbueno.firejetplus.util.FireUtil;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

/**
 * @author Finn Bon
 */
public class FireCushion extends OverriddenFireAbility implements AddonAbility, PassiveAbility {

	private Permission perm;

	@ConfigValue()
	private double damageReduction = 6;
	@ConfigValue()
	private boolean enabled = true;

	/**
	 * This constructor is used to generate config values, do not use
	 */
	private FireCushion() {
		super(null);
	}

	public FireCushion(Player player, EntityDamageEvent event) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		if (event.getDamage() <= this.damageReduction) {
			event.setCancelled(true);
		} else {
			event.setDamage(event.getDamage() - this.damageReduction);
		}

		playFirebendingSound(getLocation());

		Location center = getLocation().add(0, .1, 0);
		int amount = 75;
		double offset = .2;
		Particle fire = FireUtil.getFireParticle(bPlayer);
		for (int i = 0; i < amount; i++) {
			double xOffset = (Math.random() - .5) * offset;
			double zOffset = (Math.random() - .5) * offset;
			Location loc = center.clone().add(xOffset, 0, zOffset);
			Vector outward = GeneralMethods.getDirection(center, loc);
			outward.normalize().multiply(Math.random() * .1).setY(Math.random() * .05);
			player.getWorld().spawnParticle(fire, loc, 0, outward.getX(), outward.getY(), outward.getZ());
		}
	}

	@Override
	public void progress() {

	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "FireCushion";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {
		super.load();
		FireUtil.registerLanguage(this, "By firebending from their feet, firebenders can break their own fall. When you take fall damage, the damage is " +
			"automatically reduced by a few hearts.", null);
		ConfigValueHandler.get().setFields(new FireCushion());
	}

	@Override
	public void stop() {
		ConfigValueHandler.get().unregister(this);
	}

	@Override
	public String getAuthor() {
		return FireJet.AUTHOR;
	}

	@Override
	public String getVersion() {
		return FireJet.VERSION;
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return false;
	}
}
