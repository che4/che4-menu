package io.github.che4.i18n.menu;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}
	
	public static Bundle getBundle() {
		return getContext().getBundle();
	}
	
	public static String getBundleSymbolicName() {
		return getContext().getBundle().getSymbolicName();
	}
	
	public static String getContributorURI() {
		return "platform:/plugin/" + getBundleSymbolicName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		//org.eclipse.e4.core.di.InjectorFactory.getDefault().addBinding(LanguagesMenu.class).implementedBy(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
