package io.github.che4.i18n.menu;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import com.ibm.icu.util.ULocale;

import io.github.che4.i18n.menu.api.I18nMenuConfig;
import io.github.che4.i18n.menu.handlers.ChangeLocaleHandler;
import io.github.che4.i18n.menu.util.E4Constants;
import io.github.che4.i18n.menu.util.E4ModelUtil;

public class LanguageMenuProvider {
	
	@SuppressWarnings("restriction")
	@Inject
	private org.eclipse.e4.core.commands.EHandlerService handlerService;
	@SuppressWarnings("restriction")
	@Inject
	private org.eclipse.e4.core.commands.ECommandService commandService;
	@Inject private IEclipseContext context;
	
	//@Inject
	//@Named(TranslationService.LOCALE)
	//private Locale initialLocale;
	
		
	private static final String COMMAND_ID = "io.github.che4.tray.command.changeLocale";

	
	private static final String BUNDLE_SYMNAME = Activator.getContext().getBundle().getSymbolicName();
	private static final String CONTRIBUTOR_URI = "platform:/plugin/" + BUNDLE_SYMNAME;
	
	private static final MCommand COMMAND;
	private static final MHandler HANDLER;
	
	private Collection<ULocale> knownLanguages;
	
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
	private void init(final I18nMenuConfig config) {
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
		
		Collection<ULocale> supportedLanguages = config.getAvailableLocales().stream()
				.map( lang_country -> ULocale.createCanonical(lang_country))
				.collect( Collectors.toSet() );
		ULocale uDefaultLocale = ULocale.forLocale( config.getFallbackLocale() );
		if(supportedLanguages.size() == 1 && supportedLanguages.contains(uDefaultLocale)) {
			knownLanguages = Collections.emptyList();
		} else {
			supportedLanguages.add(uDefaultLocale);
			knownLanguages = supportedLanguages;
		}
	}
	/*
	@Deprecated
	public MMenu getLanguageMenus() {
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
	*/
	public Collection<? extends MMenuElement> getLanguageItems() {
		/*
		Collection<ULocale> supportedLanguages = config.getAvailableLocales().stream()
				.map( lang_country -> ULocale.createCanonical(lang_country))
				.collect( Collectors.toSet() );
		*/
		//if(supportedLanguages.isEmpty()) Collections.emptyList();
		//ULocale initialULocale = ULocale.forLocale(initialLocale);
		//ULocale uDefaultLocale = ULocale.forLocale(L10nUtil.LOCALE_DEFAULT);
		//FIXME we decide that English is default, i.e. bundle.properties is in English
		//ULocale uDefaultLocale = ULocale.ENGLISH;
		//ULocale uDefaultLocale = ULocale.forLocale( config.getFallbackLocale() );
		//if(supportedLanguages.size() == 1 &&
		//		supportedLanguages.contains(uDefaultLocale)) return Collections.emptyList();
		
		//supportedLanguages.add(uDefaultLocale);
		
		Locale currentLocale = (Locale) context.get(TranslationService.LOCALE);
		//String currentLang = currentLocale == null ? null : currentLocale.getLanguage();
		ULocale uCurrentLocale = ULocale.forLocale(currentLocale);
		return knownLanguages.stream()
			.map( uloc -> getLocaleMenuItem(uloc, uCurrentLocale))
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
			.values();
	}
	
	@SuppressWarnings("restriction")
	private Optional<Map.Entry<String, MHandledMenuItem>> getLocaleMenuItem(ULocale ulocale, ULocale uCurrentLocale){
		String lang3 = ulocale.getISO3Language();
		if(lang3.isEmpty()) return Optional.empty();
		MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem();
		StringJoiner sj = new StringJoiner("-");
		sj.add(lang3.toUpperCase());
		String country = ulocale.getISO3Country();
		if(!country.isEmpty()) sj.add(country);
		
		String lang_tag = ulocale.toLanguageTag();
		
		String languageNameLocal = ULocale.getDisplayLanguage(lang_tag, lang_tag);
		
		item.setLabel(sj.toString() + "  " + languageNameLocal);
		//FIXME
		//item.getTransientData().put(IPresentationEngine.STYLE_OVERRIDE_KEY, SWT.RIGHT_TO_LEFT);
		item.setContributorURI(CONTRIBUTOR_URI);
		String tooltipLang = ulocale.getDisplayLanguage(uCurrentLocale);
		String tooltipCountry = ulocale.getDisplayCountry(uCurrentLocale);
		String tooltip = tooltipCountry.isEmpty() ? tooltipLang : tooltipLang + " (" + tooltipCountry + ")";
		item.setTooltip( tooltip );
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(E4Constants.ID_COMMAND_PARAMETER_CHANGE_LOCALE, ulocale.toLanguageTag());
		ParameterizedCommand paramCommand = commandService.createCommand(COMMAND.getElementId(), parameters);
		item.setWbCommand(paramCommand);
		item.setCommand(COMMAND);
		item.setType(ItemType.CHECK);
		item.setEnabled(true);
		item.setElementId("io.github.che4.menu.0.items.Languages.items."+ulocale.toLanguageTag());
		if(uCurrentLocale != null && uCurrentLocale.getISO3Language().equals(ulocale.getISO3Language()) 
				&& uCurrentLocale.getISO3Country().equals(ulocale.getISO3Country())) {
			item.setSelected(true);
		} else {
			if(!country.isEmpty())
				item.setIconURI(I18nMenu.getCountryFlagURI(ulocale.toLocale()));
		}
		
		return Optional.of( new AbstractMap.SimpleEntry<String, MHandledMenuItem>(lang_tag,  item) );
	}
}
