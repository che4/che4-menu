package io.github.che4.menu;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import io.github.che4.menu.handlers.ChangeLocaleHandler;
import io.github.che4.menu.util.E4Constants;
import io.github.che4.menu.util.E4ModelUtil;
import io.github.che4.menu.util.L10nUtil;

public class LanguageMenuProvider {
	
	@SuppressWarnings("restriction")
	@Inject
	private org.eclipse.e4.core.commands.EHandlerService handlerService;
	@SuppressWarnings("restriction")
	@Inject
	private org.eclipse.e4.core.commands.ECommandService commandService;
	@Inject private IEclipseContext context;
		
	private static final String COMMAND_ID = "io.github.che4.tray.command.changeLocale";

	
	private static final String BUNDLE_SYMNAME = Activator.getContext().getBundle().getSymbolicName();
	private static final String CONTRIBUTOR_URI = "platform:/plugin/" + BUNDLE_SYMNAME;
	
	private static final MCommand COMMAND;
	private static final MHandler HANDLER;
	
	static {
		MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setElementId(COMMAND_ID);
		command.setCommandName("ChangeLocaleCommand");
		command.setDescription("Change application locale");
		command.setContributorURI(CONTRIBUTOR_URI);
		
		MCommandParameter langParam = MCommandsFactory.INSTANCE.createCommandParameter();
		langParam.setElementId(E4Constants.ID_COMMAND_PARAMETER_CHANGE_LOCALE);
		langParam.setName("new_app_locale");
		langParam.setOptional(false);
		langParam.setContributorURI(CONTRIBUTOR_URI);
		
		command.getParameters().add(langParam);
		
		COMMAND = command;
		
		MHandler handler = MCommandsFactory.INSTANCE.createHandler();
		handler.setContributorURI(CONTRIBUTOR_URI);
		handler.setContributionURI("bundleclass://"+BUNDLE_SYMNAME+"/" + ChangeLocaleHandler.class.getName());
		handler.setCommand(COMMAND);
		
		HANDLER = handler;
		

	}
	
	@SuppressWarnings("restriction")
	@PostConstruct
	public void init() {
		//ContextInjectionFactory.inject(HANDLER, context);
		//IContributionFactory cf = context.get(IContributionFactory.class);
		//HANDLER.setObject(cf.create(HANDLER.getContributionURI(), context));
		HANDLER.setObject(new ChangeLocaleHandler());
		handlerService.activateHandler(COMMAND.getElementId(), HANDLER.getObject());
		
		Category category = commandService.defineCategory("io.github.che4.tray.command.category.l10n", "Localization", "Localization commands");
		commandService.defineCommand(
				COMMAND.getElementId(),
				COMMAND.getCommandName(),
				COMMAND.getDescription(),
				category,
				E4ModelUtil.convertToCoreParams(COMMAND.getParameters()) );
	}
	
	public MMenu getLanguageMenu() {
		MMenu menu = MMenuFactory.INSTANCE.createMenu();
		menu.setContributorURI(CONTRIBUTOR_URI);
		menu.setLabel(E4Constants.L10N_MENU_ITEM_LANGUAGES);
		menu.setEnabled(true);
		//the 2nd approach to generate menu item - modelService.createModelElement(MDirectMenuItem.class);
		
		Set<String> supportedLanguages = L10nUtil.getAvailableLocales();
		if(supportedLanguages.isEmpty()) return menu;
		if(supportedLanguages.size() == 1 &&
				supportedLanguages.contains(L10nUtil.LOCALE_DEFAULT.getLanguage())) return menu;
		
		supportedLanguages.add(L10nUtil.LOCALE_DEFAULT.getLanguage());
		
		Locale currentLocale = (Locale) context.get(TranslationService.LOCALE);
		String currentLang = currentLocale == null ? null : currentLocale.getLanguage();
		
		supportedLanguages.stream()
			.map( lang -> getLocaleMenuItem(lang, currentLang))
			.filter( optLang -> optLang.isPresent() )
			.map ( optLang -> optLang.get() )
			.reduce(
				new TreeMap<String, MHandledMenuItem>(),
				(treeMap, entry) -> {
					treeMap.put( entry.getKey(), entry.getValue() );
					return treeMap;
				}, 
				(tm1, tm2) -> {
					tm1.putAll(tm2);
					return tm1;
				}
			)
			.forEach( (k, v) -> menu.getChildren().add(v) );
		
		return menu;
	}
	
	@SuppressWarnings("restriction")
	private Optional<Map.Entry<String, MHandledMenuItem>> getLocaleMenuItem(String langTag, String currentLang){
		Locale locale = Locale.forLanguageTag(langTag);
		String lang = locale.getLanguage();
		if(lang.isEmpty()) return Optional.empty();
		MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem();
		item.setLabel(locale.getDisplayLanguage(locale));
		item.setContributorURI(CONTRIBUTOR_URI);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(E4Constants.ID_COMMAND_PARAMETER_CHANGE_LOCALE, lang);
		ParameterizedCommand paramCommand = commandService.createCommand(COMMAND.getElementId(), parameters);
		item.setWbCommand(paramCommand);
		item.setCommand(COMMAND);
		item.setType(ItemType.CHECK);
		item.setEnabled(true);
		item.setElementId("io.github.che4.menu.0.items.Languages.items."+lang);
		
		if(currentLang != null && currentLang.equals(locale.getLanguage())) {
			item.setSelected(true);
		}
		
		return Optional.of( new AbstractMap.SimpleEntry<String, MHandledMenuItem>(lang,  item) );
	}
}
