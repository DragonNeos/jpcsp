/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.HLE;

import java.util.HashMap;

import jpcsp.HLE.modules.SysMemUserForUser;
import jpcsp.HLE.modules.SysMemUserForUser.SysMemInfo;
import jpcsp.settings.ISettingsListener;
import jpcsp.settings.Settings;

/**
 *
 * @author fiveofhearts
 */
abstract public class HLEModule {
	private SysMemInfo memory;
	private String name;
	private boolean started = false;

	public HashMap<String, HLEModuleFunction> installedHLEModuleFunctions = new HashMap<String, HLEModuleFunction>();

	public final String getName() {
		if (name == null) {
			name = getClass().getSimpleName();
		}
		return name;
	}

	/**
	 * Returns an installed hle function by name.
	 * 
	 * @param functionName the function name
	 * @return the hle function corresponding to the functionName or null
	 *         if the function was not found in this module.
	 */
	public HLEModuleFunction getHleFunctionByName(String functionName) throws RuntimeException {
		if (!installedHLEModuleFunctions.containsKey(functionName)) {
			Modules.log.error(String.format("Can't find HLE function '%s' in module '%s'", functionName, this));
			return null;
		}

		return installedHLEModuleFunctions.get(functionName);
	}

	public boolean isStarted() {
		return started;
	}

	public void start() {
		started = true;
	}

	public void stop() {
		// Remove all the settings listener defined for this module
		Settings.getInstance().removeSettingsListener(getName());
		started = false;
	}

	protected void setSettingsListener(String option, ISettingsListener settingsListener) {
		Settings.getInstance().registerSettingsListener(getName(), option, settingsListener);
	}

	static protected String getCallingFunctionName(int index) {
    	StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    	return stack[index + 1].getMethodName();
    }

	public int getMemoryUsage() {
		return 0;
	}

	public void load() {
		int memoryUsage = getMemoryUsage();
		if (memoryUsage > 0) {
			if (Modules.log.isDebugEnabled()) {
				Modules.log.debug(String.format("Allocating 0x%X bytes for HLE module %s", memoryUsage, getName()));
			}

			memory = Modules.SysMemUserForUserModule.malloc(SysMemUserForUser.USER_PARTITION_ID, String.format("Module-%s", getName()), SysMemUserForUser.PSP_SMEM_Low, memoryUsage, 0);
		} else {
			memory = null;
		}
	}

	public void unload() {
		if (memory != null) {
			if (Modules.log.isDebugEnabled()) {
				Modules.log.debug(String.format("Freeing 0x%X bytes for HLE module %s", memory.size, getName()));
			}

			Modules.SysMemUserForUserModule.free(memory);
			memory = null;
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
