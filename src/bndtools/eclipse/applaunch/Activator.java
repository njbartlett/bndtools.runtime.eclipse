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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	
	private MainThreadExecutorTracker executorTracker;

	public void start(BundleContext context) throws Exception {
		// Track the main-thread executor and register the Eclipse ApplicationLauncher around it
		executorTracker = new MainThreadExecutorTracker(context);
		executorTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		executorTracker.close();
	}
}

class MainThreadExecutorTracker extends ServiceTracker {
	
	final Logger log = Logger.getLogger(MainThreadExecutorTracker.class.getPackage().getName());
	
	MainThreadExecutorTracker(BundleContext context) {
		super(context, createFilter(), null);
	}
	static Filter createFilter() {
		try {
			String filterStr = String.format("(&(%s=%s)(thread=main))",
					Constants.OBJECTCLASS, Executor.class.getName());
			return FrameworkUtil.createFilter(filterStr);
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void open(boolean trackAllServices) {
		log.log(Level.FINE, "Starting to track main-thread executor service. Filter = {0}.", filter);
		super.open(trackAllServices);
	}
	@Override
	public void close() {
		super.close();
		log.fine("Stopped tracking main-thread executor service.");
	}
	@Override
	public Object addingService(ServiceReference reference) {
		Executor executor = (Executor) context.getService(reference);
		log.fine("Got instance of main-thread executor service -- registering ApplicationLauncher service."); 
		ServiceRegistration reg = context.registerService(ApplicationLauncher.class.getName(), new ExecutorApplicationLauncher(executor), null);
		return reg;
	}
	@Override
	public void removedService(ServiceReference reference, Object service) {
		ServiceRegistration reg = (ServiceRegistration) service;
		log.fine("Main-thread executor service going away -- unregistering ApplicationLauncher service."); 
		reg.unregister();
		
		context.ungetService(reference);
	}
}

class NewThreadExecutor implements Executor {
	public void execute(Runnable command) {
		new Thread(command).start();
	}
}
