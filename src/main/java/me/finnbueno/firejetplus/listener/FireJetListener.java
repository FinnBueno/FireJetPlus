package me.finnbueno.firejetplus.listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.ability.FireSki;
import me.finnbueno.firejetplus.ability.FireDash;
import me.finnbueno.firejetplus.combo.FireStomp;
import me.finnbueno.firejetplus.passive.FireCushion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author Finn Bon
 */
public class FireJetListener implements Listener {

	private final FireJet fireJet;
	private CoreAbility fireHop;

	public FireJetListener(FireJet fireJet) {
		this.fireJet = fireJet;
		Bukkit.getServer().getPluginManager().registerEvents(this, ProjectKorra.plugin);
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (!event.isSneaking()) {
			return;
		}

		BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bendingPlayer == null) {
			return;
		}
		FireDash stride = CoreAbility.getAbility(event.getPlayer(), FireDash.class);
		if (stride != null) {
			if (stride.attemptFireSki()) {
				return;
			}
		}
		if (bendingPlayer.canBend(fireJet)) {
			new FireJet(event.getPlayer());
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (!event.getAction().name().contains("LEFT")) {
			return;
		}
		BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bendingPlayer == null) {
			return;
		}
		FireJet fjp = CoreAbility.getAbility(event.getPlayer(), FireJet.class);
		if (fjp != null && bendingPlayer.getBoundAbility().getName().equals(fjp.getName())) {
			fjp.handleLeftClick();
			return;
		}
		if (
			bendingPlayer.canBendIgnoreBinds(getFireHop()) &&
			bendingPlayer.canBendIgnoreCooldowns(fireJet) &&
			CoreAbility.getAbility(event.getPlayer(), FireSki.class) == null
		) {
			new FireDash(event.getPlayer());
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);
		if (bendingPlayer == null) {
			return;
		}
		FireJet fjp = CoreAbility.getAbility(player, FireJet.class);
		if (fjp != null) {
			fjp.handleDamage();
		}
		FireSki fsk = CoreAbility.getAbility(player, FireSki.class);
		if (fsk != null) {
			fsk.handleDamage();
		}
	}

	@EventHandler
	public void onFall(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player) || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
			return;
		}

		Player player = (Player) event.getEntity();
		FireStomp stomp = CoreAbility.getAbility(player, FireStomp.class);
		if (stomp != null) {
			stomp.handleFallDamage(event);
		} else {
			new FireCushion(player, event);
		}
	}

	private CoreAbility getFireHop() {
		if (fireHop == null) {
			fireHop = CoreAbility.getAbility(FireDash.class);
		}
		return fireHop;
	}

}
