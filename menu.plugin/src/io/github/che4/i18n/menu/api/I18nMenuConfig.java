package io.github.che4.i18n.menu.api;

import java.util.Locale;
import java.util.Set;

import io.github.che4.i18n.menu.util.L10nUtil;

public interface I18nMenuConfig {
	/**
	 * Gets the fallback locale, i.e. if there is no translation for the locale. Usually, 
	 * and default of this interface too, the fallback locale is English &ndash; {@linkplain Locale#ENGLISH}
	 * 
	 * <p>The <code>fallback locale</code> is added to the list of available localizations.
	 * The fallback localization property file doesn't contain any indication in it's name &ndash;
	 * compare filenames <code>bundle.properties</code> (fallback) and <code>bundle_es.properties</code> (Spanish) &ndash; so we have to
	 * know somehow what is the first file localization &ndash; there are translations for fallback locale.
	 * @return the fallback locale
	 */
	default Locale getFallbackLocale() {
		return Locale.ENGLISH;
	}
	/**
	 * Gets localizations available in the application.
	 * 
	 * <p>In general there two options:</p>
	 * <ul>
	 *   <li>{@linkplain L10nUtil#getChe4AvailableLocales()} &ndash; to get localizations from Eclipse fragments to the bundle <code>io.github.che4.i18n.menu</code></li>
	 *   <li>{@linkplain L10nUtil#getBundlesLocalizations()} &ndash; to get localizations from the every bundle of the application</li>
	 * </ul>
	 * @return the set of ISO language codes (e.g.: <code>en, trk, zh_CN</code>)
	 */
	default Set<String> getAvailableLocales(){
		//return L10nUtil.getChe4AvailableLocales();
		return L10nUtil.getBundlesLocalizations();
	}

}
