package me.finnbueno.firejetplus.combo;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ClickType;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.config.ConfigValue;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import me.finnbueno.firejetplus.util.FireUtil;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Finn Bon
 */
public class FireRush extends OverriddenFireAbility implements ComboAbility, AddonAbility {

	private Permission perm;

	@ConfigValue()
	private long cooldown = 1500;
	@ConfigValue()
	private double speed = 1.7;
	@ConfigValue()
	private double knockback = .5;

	private final FireJet jet;

	public FireRush(Player player, FireJet jet) {
		super(player);
		ConfigValueHandler.get().setFields(this);
		this.jet = jet;
		player.sendMessage("Test 7");
		if (this.jet.getChargeFactor() < .6) {
			return;
		}
		player.sendMessage("Test 8");
		this.jet.setSpeed(speed);
		this.jet.setMaxSteeringAngle(1.5);
		this.player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, .7f, .7f);
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (jet.isRemoved()) {
			remove();
			return;
		}

		this.player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 2, .4, .1, .4);

		if (ThreadLocalRandom.current().nextInt(4) == 0) {
			this.player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, .4f);
		}

		GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 1).stream()
			.filter(e -> e.getUniqueId() != player.getUniqueId())
			.forEach(e -> e.setVelocity(GeneralMethods.getDirection(player.getLocation(), e.getLocation()).normalize().multiply(knockback)));
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
		return "FireRush";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {
		perm = new Permission("bending.ability." + getName());
		perm.setDefault(PermissionDefault.TRUE);
		FireUtil.registerLanguage(this, "With this combo, a firebender can greatly accelerate their FireJet, at the cost of steering capacity. To " +
			"use, the FireJet charge bar must be charged to red. When activated, your FireJet will become much faster and knock aside anyone you hit. However, steering " +
			"becomes nearly impossible.", FireUtil.generateComboInstructions(this));
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
		player.sendMessage("Test 1");
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}
		player.sendMessage("Test 2");
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return null;
		}
		player.sendMessage("Test 3");
		FireJet jet = getAbility(player, FireJet.class);
		if (jet != null) {
			player.sendMessage("Test 4");
			return jet.attemptFireRush();
		}
		return null;
	}

	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		return new ArrayList<>(Arrays.asList(
			new ComboManager.AbilityInformation("Blaze", ClickType.SHIFT_DOWN),
			new ComboManager.AbilityInformation("FireBurst", ClickType.LEFT_CLICK),
			new ComboManager.AbilityInformation("FireBurst", ClickType.LEFT_CLICK),
			new ComboManager.AbilityInformation("FireBurst", ClickType.SHIFT_UP),
			new ComboManager.AbilityInformation("FireJet", ClickType.SHIFT_DOWN),
			new ComboManager.AbilityInformation("FireJet", ClickType.SHIFT_UP)
		));
	}
}
