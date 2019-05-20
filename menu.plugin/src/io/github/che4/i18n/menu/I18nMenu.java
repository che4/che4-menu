package io.github.che4.i18n.menu;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import com.ibm.icu.util.ULocale;

import io.github.che4.i18n.menu.util.E4Constants;
import io.github.che4.i18n.menu.util.L10nUtil;

public class I18nMenu {
	
	private final static String LANG_ICON_DIR_URI = "platform:/plugin/" + Activator.getBundleSymbolicName() + "/flags/";
	private final static String UN_FLAG_ICON_URI = LANG_ICON_DIR_URI+"un.png";
	
	private static final String i18nMenuContributionId = "io.github.che4.i18n.menucontribution";
	
	//private MMenu menu;
	
	@Inject
	private MApplication application;
	@Inject
	private EModelService modelService;
	
	@SuppressWarnings("restriction")
	@Inject
	@org.eclipse.e4.core.di.annotations.Optional
	private Logger logger;
	
	private Selector selector = new Selector() {

		@Override
		public boolean select(MApplicationElement el) {
			String id = el.getElementId();
			if(id==null) return false;
			return el.getElementId().equals(E4Constants.LANGUAGE_MENU_ID);
		}};
	
	@PostConstruct
	private void init( @Named(TranslationService.LOCALE) Locale locale) {
		
		String iconUri = I18nMenu.getCountryFlagURI(locale);
		String localeLabel = ULocale.getDisplayLanguage( locale.toLanguageTag(), locale.toLanguageTag() );
		setLabelAndIcon(localeLabel, iconUri);
		
		//modelService.findElements(application, E4Constants.LANGUAGE_MENU_ID, MMenu.class, Collections.emptyList())
		//		.stream().findFirst().ifPresent( mmenu -> menu = mmenu);
		
		/*
		Optional<MMenu> optMenu = application.getMenuContributions().stream()
			.filter( mmc -> mmc.getElementId().equals(i18nMenuContributionId) )
			.flatMap( mmc -> mmc.getChildren().stream())
			.filter( mme -> mme.getElementId().equals(E4Constants.LANGUAGE_MENU_ID))
			.filter( mme -> mme instanceof MMenu )
			.map( mme -> (MMenu) mme)
			.findFirst();
		
		if(optMenu.isPresent()) {
			menu = optMenu.get();
		} else {
			System.out.println("Crate new I18n menu contribution");

			MMenuContribution menuContribution = MMenuFactory.INSTANCE.createMenuContribution();
			//FIXME add to all windows in main menu
			menuContribution.setElementId(i18nMenuContributionId);
			menuContribution.setParentId("org.eclipse.ui.main.menu");
			menuContribution.setPositionInParent("before=help");
			menuContribution.setToBeRendered(true);
			//FIXME set visible when more than one language detected
			menuContribution.setVisible(true);
			
			menu = MMenuFactory.INSTANCE.createMenu();
			menu.setElementId(E4Constants.LANGUAGE_MENU_ID);

			menu.setLabel(ULocale.getDisplayLanguage(locale.toLanguageTag(), locale.toLanguageTag()));
			String iconUri = I18nMenu.getCountryFlagURI(locale);
			menu.setIconURI(iconUri);
			boolean isToShow = L10nUtil.getAvailableLocales().size() > 1;
			menu.setToBeRendered(isToShow);
			menu.setVisible(isToShow);
			String dynMenuContribId = "io.github.che4.i18n.menu.dyncontrib";

			MDynamicMenuContribution dynMenuContrib = MMenuFactory.INSTANCE.createDynamicMenuContribution();
			dynMenuContrib.setContributionURI("bundleclass://io.github.che4.menu/io.github.che4.menu.menus.LanguagesMenu");
			dynMenuContrib.setElementId(dynMenuContribId);
			
			menu.getChildren().add(dynMenuContrib);
			menuContribution.getChildren().add(menu);
			application.getMenuContributions().add(menuContribution);
		}
		*/
	}
	
	public Optional<MMenu> getMenu() {
		//return Optional.ofNullable(menu);
		//return modelService.findElements(application, E4Constants.LANGUAGE_MENU_ID, MMenu.class, Collections.emptyList())
		//	.stream().findFirst();
		return application.getMenuContributions().stream()
				.filter( mmc -> mmc.getElementId().equals(i18nMenuContributionId) )
				.flatMap( mmc -> mmc.getChildren().stream())
				.filter( mme -> mme.getElementId().equals(E4Constants.LANGUAGE_MENU_ID))
				.filter( mme -> mme instanceof MMenu )
				.map( mme -> (MMenu) mme)
				.findFirst();
	}
	
	@SuppressWarnings("restriction")
	@Inject
	@org.eclipse.e4.core.di.annotations.Optional
	private void localeChanged(
					final IWorkbench workbench,
					final IEclipseContext iContext,
					@UIEventTopic(ILocaleChangeService.LOCALE_CHANGE) Locale locale) {
		
		String iconUri = I18nMenu.getCountryFlagURI(locale);
		String localeLabel = ULocale.getDisplayLanguage(locale.toLanguageTag(), locale.toLanguageTag());

		//modelService.findElements(application, MMenu.class, EModelService.IN_MAIN_MENU, el -> el.getElementId().equals(E4Constants.LANGUAGE_MENU_ID) );
		
		List<MMenu> menus = modelService.findElements(application, MMenu.class, EModelService.IN_MAIN_MENU, selector);
		if(menus!=null) {
			menus.stream()
				.forEach( menu -> {
					//System.out.println("Update menu: " + menu + " Parent: " + menu.getParent().getElementId());
					menu.setLabel(localeLabel);
					menu.setIconURI(iconUri);
				});
		} else {
			if(logger!=null) {
				logger.warn("Menu {} not found", EModelService.IN_MAIN_MENU);
			}
		}
		
		
		
		
		
		setLabelAndIcon(localeLabel, iconUri);

		ULocale ulocale = ULocale.forLocale(locale);
		application.getPersistedState().put("applocale", ulocale.toLanguageTag());
		//set text direction in the application
		Boolean previousRtlMode = (Boolean) application.getTransientData().get(org.eclipse.e4.ui.internal.workbench.E4Workbench.RTL_MODE);
		if(previousRtlMode != ulocale.isRightToLeft()) {
			application.getTransientData().put(org.eclipse.e4.ui.internal.workbench.E4Workbench.RTL_MODE, ulocale.isRightToLeft());
			workbench.restart();
			//FIXME 
			// renderer need to be fixed to change orientation of menus (in main menu), in current implementation workbench restart should be used.
			/*
			// ob brauchen wir wecheseln die Ausrichtung des Textes?
			Shell shell = (Shell) iContext.get(IServiceConstants.ACTIVE_SHELL);
			int textDirection = ULocale.forLocale(locale).isRightToLeft() ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT;
			shell.setOrientation(textDirection);
			*/
		}
	}
	
	private void setLabelAndIcon(String localeLabel, String iconUri) {
		getMenu().ifPresent( mme -> {
				mme.setLabel(localeLabel);
				mme.setIconURI(iconUri);
			});
	}
	
	
	public static String getCountryFlagURI(Locale locale) {
		String code3 = L10nUtil.getISO3Country(locale);
		if(code3.isEmpty()) code3 = locale.getISO3Language();
		if( !code3.isEmpty()) {
			/*
			URL url = Activator.getContext().getBundle().getEntry("flags/"+code3.toLowerCase()+".png");
			//URL url = FileLocator.find(Activator.getContext().getBundle(), new Path("icons/flags/"+country3.toLowerCase()+".png"), null);
			if(url != null) {
				return LANG_ICON_DIR_URI+code3.toLowerCase()+".png";
			}
			*/
			try {
				URL url = new URL(LANG_ICON_DIR_URI+code3.toLowerCase()+".png");
				try (InputStream inputStream = url.openConnection().getInputStream()){
					
				} catch (IOException e) {
					throw e;
				}
				return LANG_ICON_DIR_URI+code3.toLowerCase()+".png";
			} catch (Exception e) {
				//swallow exception and return UN flag
			}
		}
		return UN_FLAG_ICON_URI;
	}
}
