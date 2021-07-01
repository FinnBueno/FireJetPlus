package me.finnbueno.firejetplus.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.combo.FireRush;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Finn Bon
 */
public class FireUtil {

	public static final Vector X = new Vector(1, 0, 0), Y = new Vector(0, 1, 0), Z = new Vector(0, 0, 1);
	private static final double DIRECTION_OFFSET = 25;

	public static Vector randomizeVector(Vector vector) {
		return randomizeVector(vector, DIRECTION_OFFSET);
	}

	public static Vector randomizeVector(Vector vector, double offset) {
		double xRotation = (Math.random() - .5) * offset;
		double yRotation = (Math.random() - .5) * offset;
		double zRotation = (Math.random() - .5) * offset;
		vector = GeneralMethods.rotateVectorAroundVector(X, vector, xRotation);
		vector = GeneralMethods.rotateVectorAroundVector(Y, vector, yRotation);
		vector = GeneralMethods.rotateVectorAroundVector(Z, vector, zRotation);
		return vector;
	}

	public static Particle getFireParticle(BendingPlayer player) {
		return player.hasSubElement(Element.SubElement.BLUE_FIRE) ? Particle.SOUL_FIRE_FLAME : Particle.FLAME;
	}

	public static void registerLanguage(CoreAbility ability, String description, String instructions) {
		FileConfiguration langConfig = ConfigManager.languageConfig.get();
		if (ability instanceof ComboAbility) {
			langConfig.addDefault(String.format("ExtraAbilities.%s.Fire.Combo.%s.Description", FireJet.AUTHOR, ability.getName()), description);
			langConfig.addDefault(String.format("ExtraAbilities.%s.Fire.Combo.%s.Instructions", FireJet.AUTHOR, ability.getName()), instructions);
		} else {
			langConfig.addDefault(String.format("ExtraAbilities.%s.Fire.%s.Description", FireJet.AUTHOR, ability.getName()), description);
			langConfig.addDefault(String.format("ExtraAbilities.%s.Fire.%s.Instructions", FireJet.AUTHOR, ability.getName()), instructions);
		}
	}

	public static String generateComboInstructions(ComboAbility combo) {
		List<ComboManager.AbilityInformation> sequence = combo.getCombination();
		LinkedList<String> steps = new LinkedList<>();
		Iterator<ComboManager.AbilityInformation> it = sequence.iterator();
		ClickType previous = null;
		while (it.hasNext()) {
			ComboManager.AbilityInformation step = it.next();
			if (step.getClickType() == ClickType.SHIFT_UP && previous == ClickType.SHIFT_DOWN) {
				steps.removeLast();
			}
			steps.add(
				String.format(
					"%s (%s)",
					step.getAbilityName(),
					step.getClickType() == ClickType.SHIFT_UP && previous == ClickType.SHIFT_DOWN ?
						"Tap Shift" :
						formatClickType(step.getClickType())
				)
			);
			previous = step.getClickType();
		}
		return String.join(" > ", steps);
	}

	private static String formatClickType(ClickType clickType) {
		if (clickType == ClickType.OFFHAND_TRIGGER) {
			return "Unknown";
		}
		return Arrays.stream(clickType.name().split("_"))
			.map(p -> p.charAt(0) + p.substring(1).toLowerCase() + " ")
			.collect(Collectors.joining())
			.trim()
			.replace("Entity", "Target");
	}
}
