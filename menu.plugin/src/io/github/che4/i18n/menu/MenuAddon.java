package io.github.che4.i18n.menu;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;

import com.ibm.icu.util.ULocale;

import io.github.che4.i18n.menu.api.I18nMenuConfig;
import io.github.che4.i18n.menu.util.L10nUtil;

@SuppressWarnings("restriction")
public class MenuAddon {
	
	private final String bundleName = FrameworkUtil.getBundle(getClass()).getSymbolicName();
	
	@PostConstruct
	private void init(
			final IEclipseContext context,
			final MApplication application,
			final EModelService modelService,
			final UISynchronize uiSync) {
		
		ServiceLoader<I18nMenuConfig> loader = ServiceLoader.load(I18nMenuConfig.class);
		I18nMenuConfig i18nMenuConfig = new I18nMenuConfig() {};
		Iterator<I18nMenuConfig> iter = loader.iterator();
		while(iter.hasNext()) {
			i18nMenuConfig = iter.next();
		}

		context.set(I18nMenuConfig.class, i18nMenuConfig);
		
		//LanguageMenuProvider lmp = new LanguageMenuProvider();
		//ContextInjectionFactory.inject(lmp, context);
		//context.set(LanguageMenuProvider.class, lmp);
		
		//Locale javaLocale = (Locale) context.get(TranslationService.LOCALE);
		//System.out.println("Java locale: " + javaLocale);
		ULocale displayLocale = ULocale.forLocale(L10nUtil.LOCALE_DEFAULT);
		//ULocale displayLocale = ULocale.createCanonical("et");
		//System.out.println("Display locale: " + displayLocale + " - " + displayLocale.getDisplayLanguage());
		String applocale = application.getPersistedState().get("applocale");
		ULocale ulocale;
		if(applocale != null) {
			//System.out.println("app_locale: " + applocale);
			ulocale = ULocale.createCanonical(applocale);
		} else {
			// compare if javaLocale is in the list of available languages
			Set<String> availableLocale = i18nMenuConfig.getAvailableLocales();
			Optional<ULocale> optTag = availableLocale.stream()
				.map( tag -> ULocale.createCanonical(tag))
				.map(uloc -> {
					//System.out.println(uloc.toLanguageTag() + " " +  uloc.getCountry() + " ?= " + displayLocale.toLanguageTag());
					return uloc;
				})
				.filter( uloc -> uloc.toLanguageTag().equals(displayLocale.toLanguageTag()) || uloc.getLanguage().equals(displayLocale.getLanguage()) )
				.map( uloc -> {
					//System.out.println("Apply locale: " + uloc);
					return uloc;
				})
				.findFirst();
			
			ulocale =  optTag.orElseGet( () -> ULocale.ENGLISH );
			//System.out.println("Assigned locale: " + ulocale.toLocale());
		}
		context.set(TranslationService.LOCALE, ulocale.toLocale());
		boolean rtlMode = ulocale.isRightToLeft();
		application.getTransientData().put(E4Workbench.RTL_MODE, rtlMode);
		
		
		LanguageMenuProvider lmp = ContextInjectionFactory.make(LanguageMenuProvider.class, context);
		context.set(LanguageMenuProvider.class, lmp);
		
		I18nMenu i18nMenu = ContextInjectionFactory.make(I18nMenu.class, context);
		context.set(I18nMenu.class, i18nMenu);
		
	}
	
	@PreDestroy
	private void stopping(final MApplication application, IModelResourceHandler xmi) {
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
	@org.eclipse.e4.core.di.annotations.Optional
	private void applicationStarted(
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
