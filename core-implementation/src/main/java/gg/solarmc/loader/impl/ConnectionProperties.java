/*
 * DataLoader
 * Copyright © 2021 SolarMC Developers
 *
 * DataLoader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DataLoader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DataLoader. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.loader.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ConnectionProperties {

	private final char urlPropertyPrefix;
	private final char urlPropertySeparator;

	ConnectionProperties(char urlPropertyPrefix, char urlPropertySeparator) {
		this.urlPropertyPrefix = urlPropertyPrefix;
		this.urlPropertySeparator = urlPropertySeparator;
	}

	String formatProperties(Map<String, Object> properties) {
		if (properties.isEmpty()) {
			return "";
		}
		List<String> connectProps = new ArrayList<>(properties.size());
		properties.forEach((key, value) -> connectProps.add(key + "=" + value));

		return urlPropertyPrefix + String.join(Character.toString(urlPropertySeparator), connectProps);
	}
}
