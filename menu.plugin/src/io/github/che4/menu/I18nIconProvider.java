package io.github.che4.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;

import io.github.che4.menu.util.E4Constants;

@SuppressWarnings("restriction")
public class I18nIconProvider {
	
	private final static Map<String,String> langMap = new HashMap<String,String>();
	private final static String LANG_ICON_DIR_URI = "platform:/plugin/io.github.che4.menu/icons/flags/";
	static {
		langMap.put("ru", "rus");
		langMap.put("en", "gbr");
		langMap.put("de", "deu");
	}

	@Inject
	@org.eclipse.e4.core.di.annotations.Optional
	public void applicationStarted(
			UISynchronize uiSync,
			EModelService modelService,
			MApplication application,
			IEclipseContext context,
			@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		
		uiSync.asyncExec( () -> {
			uiSync.asyncExec( () -> {
				Locale locale = (Locale) context.get(TranslationService.LOCALE);
				updateLangMenu(modelService, application, locale);
			});
		});
	}
	
	@Inject 
	@org.eclipse.e4.core.di.annotations.Optional
	public void updateLocale(
			//@EventTopic(ILocaleChangeService.LOCALE_CHANGE) Event event
			@UIEventTopic(ILocaleChangeService.LOCALE_CHANGE) Locale locale,
			final IEclipseContext iContext,
			final EModelService modelService,
			final MApplication application,
			final UISynchronize uiSync
			) {
		
		uiSync.asyncExec( () -> {
			updateLangMenu(modelService, application, locale);
		});
	}
	
	private void updateLangMenu(EModelService modelService, MApplication application, Locale locale) {
		List<MMenu> menus = modelService.findElements(application, MMenu.class, EModelService.IN_MAIN_MENU , new Selector() {

			@Override
			public boolean select(MApplicationElement element) {
				return element.getElementId().equals(E4Constants.LANGUAGE_MENU_ID);
			}
		});
		menus.stream().forEach( menu -> {
			menu.setLabel(locale.getDisplayLanguage(locale));
			String icon = langMap.get(locale.getLanguage());
			menu.setIconURI(LANG_ICON_DIR_URI+icon+".png");
		});
	}

}
