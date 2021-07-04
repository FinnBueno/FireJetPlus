package me.finnbueno.firejetplus.config;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import java.lang.reflect.Field;

/**
 * @author Finn Bon
 */
public class ConfigField {

	private final Field field;
	private final Object value;

	public ConfigField(Field field, String path) {
		this.field = field;
		this.value = ConfigManager.defaultConfig.get().get(path);
	}

	public void load(CoreAbility ability) {
		try {
			field.set(ability, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
