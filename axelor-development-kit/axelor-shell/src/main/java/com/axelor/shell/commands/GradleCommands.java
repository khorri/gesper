/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.shell.commands;

import static com.axelor.common.StringUtils.isBlank;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.gradle.jarjar.com.google.common.collect.Lists;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import com.axelor.shell.core.CommandProvider;
import com.axelor.shell.core.CommandResult;
import com.axelor.shell.core.Shell;
import com.axelor.shell.core.annotations.CliCommand;
import com.axelor.shell.core.annotations.CliOption;
import com.google.common.base.Joiner;

public class GradleCommands implements CommandProvider {
	
	private GradleConnector connector;
	
	private Shell shell;
	
	public GradleCommands(Shell shell) {
		this.shell = shell;
	}

	private CommandResult execute(String... args) {
		return execute(Arrays.asList(args));
	}

	private CommandResult execute(Iterable<String> arguments) {

		if (connector == null) {
			connector = GradleConnector.newConnector();
		}
		
		connector.forProjectDirectory(shell.getWorkingDir());
		ProjectConnection connection = connector.connect();
		
		final OutputStream nullStream = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}
		};

		final PrintStream outStream = System.out;
		final PrintStream errStream = System.err;

		System.setOut(new PrintStream(nullStream));
		System.setErr(new PrintStream(nullStream));

		try {
			final BuildLauncher launcher = connection.newBuild();
			launcher.setStandardOutput(outStream);
			launcher.setStandardError(errStream);
			launcher.withArguments(arguments);
			launcher.run();
		} catch (Exception e) {
			errStream.println("Command failed: " + e);
		} finally {
			connection.close();
			System.setOut(outStream);
			System.setErr(errStream);
		}

		return new CommandResult(true);
	}

	@CliCommand(name = "clean", help = "clean the project build")
	public void clean() {
		execute("clean");
	}

	@CliCommand(name = "build", usage = "[OPTIONS]", help = "build the project")
	public CommandResult build(
			@CliOption(name = "quiet", shortName = 'q', help = "show errors only")
			boolean quiet,
			@CliOption(name = "stacktrace", shortName = 's', help = "show stacktrace for all exceptions")
			boolean stacktrace) {
		final List<String> args = Lists.newArrayList("-x", "test");
		if (quiet) { args.add("-q"); }
		if (stacktrace) { args.add("--stacktrace"); }
		args.add("build");
		return execute(args);
	}
	
	@CliCommand(name = "run", usage = "[OPTIONS]", help = "run the embedded tomcat server")
	public CommandResult run(
			@CliOption(name = "port", shortName = 'p', argName = "PORT", help = "alternative port, default is 8080")
			String port,
			@CliOption(name = "config", shortName = 'c', argName = "FILE", help = "application configuration file")
			String config,
			@CliOption(name = "quiet", shortName = 'v', help = "show errors only")
			boolean quiet) {
		
		final List<String> args = Lists.newArrayList("-x", "test");
		if (quiet) {
			args.add("-q");
		}
		if (!isBlank(config)) {
			args.add("-Daxelor.config=" + config);
		}
		if (!isBlank(port)) {
			args.add("-Phttp.port=" + port);
		}
		args.add("tomcatRun");
		return execute(args);
	}

	@CliCommand(name = "i18n", usage = "[OPTIONS]", help = "extract/update translatable messages")
	public CommandResult i18n(
			@CliOption(name = "project-dir", shortName = 'p', help = "specify the module directory")
			String module,
			@CliOption(name = "extract", shortName = 'e', help = "extract messages.")
			boolean extract,
			@CliOption(name = "update", shortName = 'u', help = "update messages.")
			boolean update,
			@CliOption(name = "with-context", shortName = 'c', help = "extract context details.")
			boolean withContext) {
		final String task = update ? "i18n-update" : "i18n-extract";
		final List<String> args = Lists.newArrayList("-x", "test");
		if (withContext) {
			args.add("-Pwith.context=true");
		}
		if (!isBlank(module)) {
			args.add("-p");
			args.add(module);
		}
		args.add(task);
		return execute(args);
	}

	@CliCommand(name = "init", usage = "[OPTIONS] [MODULES...]", help = "initialize or update the database")
	public CommandResult init(
			@CliOption(name = "config", shortName = 'c', argName = "FILE", help = "application configuration file", required = true)
			String config,
			@CliOption(name = "update", shortName = 'u', help = "update the installed or given modules")
			boolean update,
			String... modules) {

		final List<String> args = Lists.newArrayList("-q", "-x", "test", "init", "-Daxelor.config=" + config);
		if (update) {
			args.add("-Pupdate=true");
		}
		if (modules != null && modules.length > 0) {
			args.add("-Pmodules=" + Joiner.on(",").join(modules));
		}
		return execute(args);
	}

	@CliCommand(name = "migrate", usage = "[OPTIONS]", help = "run database migration scripts")
	public CommandResult migrate(
			@CliOption(name = "config", shortName = 'c', argName = "FILE", help = "application configuration file", required = true)
			String config,
			@CliOption(name = "verbose", shortName = 'v', help = "verbose output")
			boolean verbose) {
		final List<String> args = Lists.newArrayList("-q", "-x", "test", "migrate", "-Daxelor.config=" + config);
		if (verbose) {
			args.add("-Pverbose=true");
		}
		return execute(args);
	}
}
