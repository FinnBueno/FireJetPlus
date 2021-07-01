package me.finnbueno.firejetplus.util;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.config.ConfigValueHandler;
import org.bukkit.entity.Player;

/**
 * @author Finn Bon
 */
public abstract class OverriddenFireAbility extends FireAbility {

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
}
