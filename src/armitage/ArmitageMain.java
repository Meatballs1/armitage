package armitage;

import sleep.runtime.*;
import sleep.interfaces.*;
import sleep.console.*;
import sleep.bridges.*;
import sleep.error.*;
import sleep.engine.*;

import java.util.*;

import java.io.*;

/**
 *  This class launches Armitage and loads the scripts that are part of it.
 */
public class ArmitageMain implements RuntimeWarningWatcher, Loadable, Function {
	public void processScriptWarning(ScriptWarning warning) {
		System.out.println(warning);
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		try {
			InputStream i = this.getClass().getClassLoader().getResourceAsStream(BridgeUtilities.getString(args, ""));
			return SleepUtils.getScalar(i);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	protected ScriptVariables variables = new ScriptVariables();

	public void scriptLoaded(ScriptInstance script) {
		script.addWarningWatcher(this);
		script.setScriptVariables(variables);
	}

	public void scriptUnloaded(ScriptInstance script) {
	}

	protected String[] getGUIScripts() {
		return new String[] {
			"scripts/log.sl",
			"scripts/reporting.sl",
			"scripts/gui.sl",
			"scripts/util.sl",
			"scripts/targets.sl",
			"scripts/attacks.sl",
			"scripts/meterpreter.sl",
			"scripts/process.sl",
			"scripts/browser.sl",
			"scripts/pivots.sl",
			"scripts/services.sl",
			"scripts/loot.sl",
			"scripts/tokens.sl",
			"scripts/downloads.sl",
			"scripts/shell.sl",
			"scripts/screenshot.sl",
			"scripts/hosts.sl",
			"scripts/passhash.sl",
			"scripts/jobs.sl",
			"scripts/preferences.sl",
			"scripts/modules.sl",
			"scripts/workspaces.sl",
			"scripts/menus.sl",
			"scripts/collaborate.sl",
			"scripts/armitage.sl"
		};
	}

	protected String[] getServerScripts() {
		return new String[] {
			"scripts/util.sl",
			"scripts/preferences.sl",
			"scripts/reporting.sl",
			"scripts/server.sl"
		};
	}

	public ArmitageMain(String[] args) {
		Hashtable environment = new Hashtable();
		environment.put("&resource", this);

		/* set our command line arguments into a var */
		variables.putScalar("@ARGV", ObjectUtilities.BuildScalar(false, args));

		ScriptLoader loader = new ScriptLoader();
		loader.addSpecificBridge(this);

		/* check for server mode option */
		boolean serverMode = false;

		int x = 0;
		for (x = 0; x < args.length; x++) {
			if (args[x].equals("--server"))
				serverMode = true;
		}

		/* load the appropriate scripts */
		String[] scripts = serverMode ? getServerScripts() : getGUIScripts();

		try {
			for (x = 0; x < scripts.length; x++) {
				InputStream i = this.getClass().getClassLoader().getResourceAsStream(scripts[x]);
				ScriptInstance si = loader.loadScript(scripts[x], i, environment);
				si.runScript();
			}
		}
		catch (YourCodeSucksException yex) {
			System.out.println("*** File: " + scripts[x]);
			yex.printErrors(System.out);
		}
		catch (IOException ex) {
			System.err.println(ex);
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new ArmitageMain(args);
	}
}
