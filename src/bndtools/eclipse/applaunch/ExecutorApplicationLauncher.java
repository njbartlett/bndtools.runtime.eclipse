/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package bndtools.eclipse.applaunch;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.eclipse.osgi.service.runnable.ParameterizedRunnable;

public class ExecutorApplicationLauncher implements ApplicationLauncher {
	
	private final Logger log = Logger.getLogger(ExecutorApplicationLauncher.class.getPackage().getName());
	
	final Executor executor;

	public ExecutorApplicationLauncher(Executor executor) {
		this.executor = executor;
	}

	public void launch(final ParameterizedRunnable runnable, final Object context) {
		executor.execute(new Runnable() {
			public void run() {
				try {
					log.log(Level.FINE, "Executing appplication runnable on thread {0} ({1}).",
							new Object[] { Thread.currentThread().getName(), Thread.currentThread().getId() });
					runnable.run(context);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error executing application runnable", e);
				}
			}
		});
	}

	public void shutdown() {
		log.warning("Ignoring shutdown call");
	}
}
