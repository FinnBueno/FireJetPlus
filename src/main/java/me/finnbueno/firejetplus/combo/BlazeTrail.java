package me.finnbueno.firejetplus.combo;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.ClickType;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.ability.FireSki;
import me.finnbueno.firejetplus.config.ConfigValue;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import me.finnbueno.firejetplus.util.FireUtil;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Finn Bon
 */
public class BlazeTrail extends OverriddenFireAbility implements ComboAbility, AddonAbility {

	private static final double ANGLE_INCREMENT = 5;
	@ConfigValue()
	private double speed = 1;
	@ConfigValue()
	private long cooldown = 4500;
	@ConfigValue()
	private int size = 2;
	@ConfigValue()
	private double angle = 65;

	private Location loc;
	private final FireSki ski;
	private Permission perm;

	public BlazeTrail(Player player, FireSki ski) {
		super(player);
		ConfigValueHandler.get().setFields(this);
		this.ski = ski;
		ski.setSpeed(speed);
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public String getInstructions() {
		return super.getInstructions();
	}

	@Override
	public void progress() {
		if (ski.isRemoved()) {
			remove();
			return;
		}

		if (ski.getFloor() == null) {
			return;
		}

		if (ThreadLocalRandom.current().nextInt(4) == 0) {
			player.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, .5F, .5F);
		}

		Location loc = ski.getFloor().getRelative(BlockFace.UP).getLocation();
		if (this.loc == loc) {
			return;
		}
		this.loc = loc;

		Vector blazeDirection = ski.getDirection().multiply(-1);
		for (double i = -angle / 2; i < angle / 2; i += ANGLE_INCREMENT) {
			Location trailStart = this.loc.clone();
			Vector v = blazeDirection.clone();
			double rad = Math.toRadians(-i);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double x = v.getX() * cos - v.getZ() * sin;
			double z = v.getX() * sin + v.getZ() * cos;
			v.setX(x).setZ(z).normalize();
			trailStart.add(v);
			new BlazeArc(player, trailStart, v, size);
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "BlazeTrail";
	}

	@Override
	public Location getLocation() {
		return this.loc;
	}

	@Override
	public void load() {
		perm = new Permission("bending.ability." + getName());
		perm.setDefault(PermissionDefault.TRUE);
		FireUtil.registerLanguage(this, "With this combo, a firebender can use their FireSki to set the ground on fire. To use this combo, tap shift on " +
			"FireBurst twice. Then, start skiing.", FireUtil.generateComboInstructions(this));
		ConfigValueHandler.get().registerDefaultValues(this);
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

	@Override
	public Object createNewComboInstance(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return null;
		}
		FireSki ski = CoreAbility.getAbility(player, FireSki.class);
		if (ski != null) {
			return new BlazeTrail(player, ski);
		}
		return null;
	}

	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		return new ArrayList<>(Arrays.asList(
			new ComboManager.AbilityInformation("FireBurst", ClickType.SHIFT_DOWN),
			new ComboManager.AbilityInformation("FireBurst", ClickType.SHIFT_UP),
			new ComboManager.AbilityInformation("FireBurst", ClickType.SHIFT_DOWN),
			new ComboManager.AbilityInformation("FireBurst", ClickType.SHIFT_UP),
			new ComboManager.AbilityInformation("FireJet", ClickType.LEFT_CLICK),
			new ComboManager.AbilityInformation("FireJet", ClickType.SHIFT_DOWN)
		));
	}
}
