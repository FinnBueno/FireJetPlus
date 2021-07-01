package me.finnbueno.firejetplus.config;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.finnbueno.firejetplus.ability.FireJet;
import me.finnbueno.firejetplus.util.OverriddenFireAbility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Finn Bon
 */
public class ConfigValueHandler {

	private static final ConfigValueHandler INSTANCE = new ConfigValueHandler();

	public static ConfigValueHandler get() {
		return INSTANCE;
	}

	private static final String BASE_PATH = String.format("ExtraAbilities.%s.Fire.", FireJet.AUTHOR);

	private Map<Class<? extends CoreAbility>, Set<ConfigField>> fieldsPerClass = new HashMap<>();

	public void setFields(CoreAbility ability) {
		Set<ConfigField> configFields = fieldsPerClass.get(ability.getClass());
		configFields.forEach(cf -> cf.load(ability));
	}

	public void registerDefaultValues(CoreAbility ability) {
		registerDefaultValues(ability, null);
	}

	public void registerDefaultValues(CoreAbility ability, String prefix) {
		try {
			String path = BASE_PATH;
			if (ability instanceof ComboAbility) {
				path += "Combos.";
			}
			if (prefix != null) {
				path += prefix + ".";
			}
			path += ability.getName();

			Field[] fields = ability.getClass().getDeclaredFields();

			HashSet<ConfigField> fieldSet = new HashSet<>();

			for (Field field : fields) {
				field.setAccessible(true);
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				Object standardValue = field.get(ability);
				if (standardValue == null) {
					continue;
				}
				ConfigValue configValue = field.getAnnotation(ConfigValue.class);
				if (configValue == null) {
					continue;
				}

				String finalPath = String.format("%s.%s", path, configValue.value().length() == 0 ? formatFieldName(field.getName()) : configValue.value());

				System.out.printf("Field: %s - Value: %s - Path: %s%n", field.getName(), standardValue, finalPath);

				ConfigManager.defaultConfig.get().addDefault(finalPath, standardValue);

				fieldSet.add(new ConfigField(field, finalPath));
			}

			ConfigManager.defaultConfig.save();

			fieldsPerClass.put(ability.getClass(), fieldSet);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private String formatFieldName(String name) {
		return Arrays.stream(name.split("_"))
			.map(s -> s.toUpperCase().charAt(0) + s.substring(1))
			.collect(Collectors.joining());
	}

}
