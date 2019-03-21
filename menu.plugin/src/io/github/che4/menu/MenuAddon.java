package io.github.che4.menu;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;

import io.github.che4.menu.util.E4Constants;
import io.github.che4.menu.util.L10nUtil;

@SuppressWarnings("restriction")
public class MenuAddon {
	
	private final String bundleName = FrameworkUtil.getBundle(getClass()).getSymbolicName();
	
	@PostConstruct
	public void init(
			final IEclipseContext context,
			final MApplication application,
			final EModelService modelService,
			final UISynchronize uiSync) {
		//LanguageMenuProvider lmp = new LanguageMenuProvider();
		//ContextInjectionFactory.inject(lmp, context);
		//context.set(LanguageMenuProvider.class, lmp);
		LanguageMenuProvider lmp = ContextInjectionFactory.make(LanguageMenuProvider.class, context);
		context.set(LanguageMenuProvider.class, lmp);
		
		ContextInjectionFactory.make(I18nIconProvider.class, context);
		
		
		
		/*
		List<MMenuContribution> dynamicMenuContributions = application.getChildren().stream()
			.filter( window -> window.getMainMenu() != null )
			//.map( window -> {
			//	window.setIconURI("platform:/plugin/io.github.che4.menu/icons/che-guevara-64.png");
			//	return window;
			//})
			.map( window -> window.getMainMenu().getElementId() )
			.filter( id -> id != null)
			// We want Menu contribution (see fragment.e4xmi) with ID "com.itranga.e4.tray.menucontribution",
			// that don't have parent specified to contribute to GUI of all windows with main menu 
			//
			.flatMap( id -> {
				return application.getMenuContributions().stream()
					.filter( mmc -> mmc.getElementId().equals(E4Constants.ID_MENU_CONTRIBUTION))
					.map( mmc -> (MMenuContribution) eModelService.cloneElement(mmc, application))
					.map( mmc2 -> { mmc2.setParentId(id); return mmc2; });
			})
			.collect( Collectors.toList() );
		
		application.getMenuContributions().addAll(dynamicMenuContributions);
		*/
	}
	
	@PreDestroy
	public void stopping(final MApplication application, IModelResourceHandler xmi) {
		boolean menuChanged = application.getMenuContributions().removeIf( this :: hasApplicationElement );
		boolean addonChanged = application.getAddons().removeIf( this :: hasApplicationElement );
		boolean handlerChanged = application.getHandlers().removeIf( this :: hasApplicationElement );
		boolean changed = menuChanged || addonChanged || handlerChanged;
		if(changed) try {
			xmi.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Inject
	@Optional
	public void applicationStarted(
			final MApplication application,
			final ECommandService commandService,
			final EHandlerService handlerService,
			final IEclipseContext iContext,
			@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		
		/*
		UISynchronize uiSync = iContext.get(UISynchronize.class);
		uiSync.asyncExec( () -> {
			Display display = iContext.get(Display.class);
			Tray tray = display.getSystemTray();
			if(tray!=null) {
				Shell shell = new Shell(display);
				application.getMenuContributions().stream()
					.filter(mmc -> mmc.getElementId().equals(E4Constants.ID_MENU_CONTRIBUTION))
					.flatMap( mmc -> mmc.getChildren().stream())
					.filter(me -> me instanceof MMenu)
					.filter(me -> me.getElementId().equals(E4Constants.ID_MENU_CONTRIBUTION + ".0"))
					.findFirst().ifPresent( me -> {
						MMenu menuBar = (MMenu) me;
						TrayItem trayItem = new TrayItem(tray, SWT.NONE);
						trayItem.addListener(SWT.MenuDetect, e -> {
							menuBar.setVisible(true);
							IPresentationEngine renderer = iContext.get(IPresentationEngine.class);
							Object widget = renderer.createGui(menuBar, shell, iContext);
							Menu menu = (Menu) widget;
							menu.setVisible(true);
						});
						//InputStream stream = getClass().getClassLoader().getResourceAsStream("/icons/icon.png");
						try ( InputStream stream = URI.create(menuBar.getIconURI()).toURL().openStream() ) {
							ImageData id = new ImageData(stream);
							Image image = new Image(null, id);
							trayItem.setImage(image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				
			}
		});
		*/
	}
	
	private boolean hasApplicationElement(MApplicationElement elem) {
		String contributor = elem.getContributorURI();
		if(contributor != null) {
			return contributor.equals("platform:/plugin/"+bundleName);
		}
		return false;	
	}
}
