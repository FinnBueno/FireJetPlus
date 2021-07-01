package me.finnbueno.firejetplus.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import me.finnbueno.firejetplus.combo.FireRush;
import me.finnbueno.firejetplus.config.ConfigValue;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import me.finnbueno.firejetplus.listener.FireJetListener;
import me.finnbueno.firejetplus.util.FireUtil;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Finn Bon
 *
 */
public class FireJet extends OverriddenFireAbility implements AddonAbility {

	private enum State {
		CHARGING, FLYING
	}

	public static final String AUTHOR = "FinnBueno", VERSION = "1.0.0";

	private Listener listener;

	private static final String CHARGE_BAR_TITLE = "Charging FireJet";

	@ConfigValue()
	private long cooldown = 4000;
	@ConfigValue()
	private long maxChargeTime = 2500;
	@ConfigValue()
	private long minChargeTime = 500;
	@ConfigValue()
	private long maxDuration = 2500;
	@ConfigValue()
	private long minDuration = 1000;
	@ConfigValue()
	private double minSpeed = .5;
	@ConfigValue()
	private double maxSpeed = 1.2;
	@ConfigValue()
	private double maxSteeringAngle = 7;
	@ConfigValue()
	private boolean lockDirectionOnSlotSwap = true;
	@ConfigValue()
	private boolean igniteOnMaxCharge = true;
	@ConfigValue()
	private int fireTicks = 40;
	@ConfigValue()
	private boolean cancelChargeOnDamage = true;
	@ConfigValue()
	private long chargeCancelCooldown = 750;
	@ConfigValue()
	private boolean cancelJetOnDamage = true;

	private State state;
	private BossBar chargeBar;
	private Vector direction;
	private double duration;
	private double speed;
	private boolean ignite;
	private long flyStart;
	private Set<LivingEntity> lit;
	private double chargeFactor;

	public FireJet(Player player) {
		super(player);

		ConfigValueHandler.get().setFields(this);

		FireJet existingFireJet = getAbility(player, getClass());
		if (existingFireJet != null) {
			return;
		}

		this.state = State.CHARGING;
		this.chargeBar = Bukkit.getServer().createBossBar(CHARGE_BAR_TITLE, BarColor.WHITE, BarStyle.SEGMENTED_10);
		this.chargeBar.addPlayer(player);
		this.chargeBar.setProgress(0);
		this.direction = player.getLocation().getDirection();
		start();
	}

	@Override
	public void progress() {
		switch (state) {
			case CHARGING:
				handleCharging();
				break;
			case FLYING:
				handleFlying();
				break;
		}
	}

	private void handleCharging() {
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		long timeSinceStart = System.currentTimeMillis() - getStartTime();

		// player stopped sneaking, check if we can start the move
		if (!player.isSneaking()) {
			// if the minimum charge time hasn't passed, stop the move. Else, start the move!
			if (timeSinceStart < minChargeTime) {
				remove();
			} else {
				startFlying();
			}
			return;
		}

		// get the difference between the max and min charge time
		double chargeDuration = maxChargeTime - minChargeTime;
		// subtract the minimum charge time from the time since start
		double actualChargeTime = timeSinceStart - minChargeTime;
		if (actualChargeTime <= 0) {
			return;
		}

		double progress = bPlayer.isAvatarState() ? 1 : actualChargeTime / chargeDuration;
		// clamp progress in between 0 and 1
		this.chargeBar.setProgress(clamp(progress));

		if (this.chargeBar.getProgress() < .33) {
			this.chargeBar.setColor(BarColor.WHITE);
			this.chargeBar.setTitle(ChatColor.WHITE + CHARGE_BAR_TITLE);
		} else if (this.chargeBar.getProgress() < .66) {
			this.chargeBar.setColor(BarColor.YELLOW);
			this.chargeBar.setTitle(ChatColor.YELLOW + CHARGE_BAR_TITLE);
		} else {
			this.chargeBar.setColor(BarColor.RED);
			this.chargeBar.setTitle(ChatColor.RED + CHARGE_BAR_TITLE);
		}

		if (progress >= 1) {
			if (Math.random() < .4) {
				playFirebendingSound(getLocation());
			}
			playFirebendingParticles(getLocation().add(0, 0.6, 0), 6, 0.4, 0.4, 0.4);
		}

	}

	private void startFlying() {
		this.chargeFactor = chargeBar.getProgress();
		this.duration = (maxDuration - minDuration) * chargeFactor + minDuration;
		this.speed = getDayFactor((maxSpeed - minSpeed) * chargeFactor + minSpeed);
		this.ignite = chargeFactor >= 1 && igniteOnMaxCharge;
		this.direction = player.getLocation().getDirection().multiply(this.speed);
		this.flyStart = System.currentTimeMillis();
		this.flightHandler.createInstance(player, this.getName());
		this.player.setAllowFlight(true);
		this.lit = new HashSet<>();
		this.state = State.FLYING;
	}

	private void handleFlying() {
		long flyTime = System.currentTimeMillis() - this.flyStart;
		if (flyTime > this.duration && !bPlayer.isAvatarState()) {
			removeWithCooldown();
		}

		double progress = (1 - clamp(flyTime / this.duration)) * this.chargeFactor;
		this.chargeBar.setProgress(bPlayer.isAvatarState() ? 1 : progress);

		player.setVelocity(this.direction);
		player.setFallDistance(0);
		if (Math.random() < .5) {
			playFirebendingSound(this.player.getLocation());
		}

		this.playFirebendingParticles(this.player.getLocation(), 10, 0.3D, 0.3D, 0.3D);

		if (!lockDirectionOnSlotSwap || bPlayer.getBoundAbilityName().equals(getName())) {
			setDirection();
		}

		if (this.ignite) {
			GeneralMethods.getEntitiesAroundPoint(getLocation(), 1).stream()
				.filter(e -> e.getUniqueId() != player.getUniqueId())
				.filter(e -> e instanceof LivingEntity)
				.filter(e -> !lit.contains(e))
				.map(e -> (LivingEntity) e)
				.forEach(e -> {
					lit.add(e);
					e.setFireTicks(fireTicks);
				});
		}
	}

	private void setDirection() {
		Vector player = getLocation().getDirection();
		Vector initial = this.direction.normalize().clone();
		if (Math.toDegrees(player.angle(initial)) > this.maxSteeringAngle) {
			Vector cross = player.getCrossProduct(initial);
			this.direction = GeneralMethods.rotateVectorAroundVector(cross, initial, -this.maxSteeringAngle).normalize();
		} else {
			this.direction = player;
		}
		this.direction.multiply(this.speed);
	}

	private void removeWithCooldown() {
		remove();
		if (!bPlayer.isAvatarState()) {
			bPlayer.addCooldown(this);
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.flightHandler.removeInstance(this.player, getName());
		this.chargeBar.removeAll();
	}

	public void handleLeftClick() {
		if (state == State.FLYING) {
			removeWithCooldown();
		}
	}

	public void handleDamage() {
		switch (state) {
			case CHARGING:
				if (cancelChargeOnDamage) {
					// shorter cooldown cause only the charge got interrupted
					bPlayer.addCooldown(this, chargeCancelCooldown);
					remove();
				}
				break;
			case FLYING:
				if (cancelJetOnDamage) {
					removeWithCooldown();
				}
				break;
		}
	}

	public FireRush attemptFireRush() {
		player.sendMessage("Test 5");
		if (state == State.FLYING) {
			player.sendMessage("Test 6");
			new FireRush(player, this);
		}
		return null;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setMaxSteeringAngle(double maxSteeringAngle) {
		this.maxSteeringAngle = maxSteeringAngle;
	}

	public double getChargeFactor() {
		return this.chargeFactor;
	}

	private double clamp(double v) {
		return Math.max(0, Math.min(1, v));
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
		return "FireJet";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {
		listener = new FireJetListener(this);
		FireUtil.registerLanguage(this, "This ability provides a firebender with great mobility and repositioning options. This move has 2 use with their own cooldowns.",
			"\nJet: This function allows a firebender to fly using jets of fire. To use, hold shift. A bar will appear at the top of your screen to show charging progress. " +
			"At any point you can let go to start the move. The fuller the bar, the faster and longer you will fly. Switching slots during flight does not cancel the " +
			"move, but instead locks the direction. Making turns during flight is limited to a certain angle.\n" +
			"Dash: This function allows a firebender to make a single large jump in a direction. To do this, simply left click. Shortly after activating this and while still " +
			"in the air, you may hold shift. When you do this, you start surfing across the ground for a longer period of time. Changing slots is also possible during this, " +
			"and will also lock your direction. However, you must hold shift, or this move will stop.");
		ConfigValueHandler.get().registerDefaultValues(this);
	}

	@Override
	public void stop() {
		HandlerList.unregisterAll(listener);
	}

	@Override
	public String getAuthor() {
		return AUTHOR;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}
}
