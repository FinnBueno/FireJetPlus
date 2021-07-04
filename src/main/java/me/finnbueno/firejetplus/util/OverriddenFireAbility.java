package me.finnbueno.firejetplus.util;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Optional;

/**
 * @author Finn Bon
 */
public abstract class OverriddenFireAbility extends FireAbility implements AddonAbility {

	public OverriddenFireAbility(Player player) {
		super(player);
	}

	@Override
	public String getInstructions() {
		String elementName = this.getElement().getName();
		if (this.getElement() instanceof Element.SubElement) {
			elementName = ((Element.SubElement) this.getElement()).getParentElement().getName();
		}
		if (this instanceof ComboAbility) {
			elementName = elementName + ".Combo";
		}
		return ConfigManager.languageConfig.get().contains("ExtraAbilities." + FireJet.AUTHOR + "." + elementName + "." + this.getName() + ".Instructions") ?
			ConfigManager.languageConfig.get().getString("ExtraAbilities." + FireJet.AUTHOR + "." + elementName + "." + this.getName() + ".Instructions") :
			super.getInstructions();
	}

	@Override
	public String getDescription() {
		String elementName = this.getElement().getName();
		if (this.getElement() instanceof Element.SubElement) {
			elementName = ((Element.SubElement) this.getElement()).getParentElement().getName();
		}
		if (this instanceof PassiveAbility) {
			return ConfigManager.languageConfig.get().getString("ExtraAbilities." + FireJet.AUTHOR + "." + elementName + ".Passive." + this.getName() + ".Description");
		} else if (this instanceof ComboAbility) {
			return ConfigManager.languageConfig.get().getString("ExtraAbilities." + FireJet.AUTHOR + "." + elementName + ".Combo." + this.getName() + ".Description");
		}
		return ConfigManager.languageConfig.get().getString("ExtraAbilities." + FireJet.AUTHOR + "." + elementName + "." + this.getName() + ".Description");
	}

	@Override
	public void load() {
		String permNode = "bending.ability." + getName();
		if (Bukkit.getServer().getPluginManager().getPermission(permNode) == null) {
			Permission perm = new Permission(permNode);
			perm.setDefault(PermissionDefault.TRUE);
			Bukkit.getServer().getPluginManager().addPermission(perm);
		}
	}

	@Override
	public boolean isEnabled() {
		Optional<Boolean> isEnabled = ConfigValueHandler.get().checkManually(this, "Enabled");
		return isEnabled.orElse(true);
	}

}
