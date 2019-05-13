package io.github.che4.i18n.menu.util;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.icu.util.ULocale;

public class L10nUtil {
	/**
	 * The locale of the operation system, to be precise, it's GUI.
	 */
	public static final Locale LOCALE_DEFAULT = Locale.getDefault(Locale.Category.DISPLAY);
	//public static final Locale LOCALE_DEFAULT = new Locale("ar");
	static {
		Locale.setDefault(LOCALE_DEFAULT);
	}
	/**
	 * Gets available localizations from the <code>OSGI-INF/l10n/bundle*</code> files of the bundle itself and
	 * it's fragments
	 * @return
	 */
	public static Set<String> getChe4AvailableLocales() {
		Bundle bundle = FrameworkUtil.getBundle(L10nUtil.class);
		return getAvailableLocales(bundle, "OSGI-INF/l10n", "bundle.properties", true);
	}
	/**
	 * Gets available translations in the <code>bundle</code>.
	 * <p>Sample data 
	 * <ul>
	 * <li><code>directory</code> = <code>OSGI-INF/l10n</code> </li>
	 * <li><code>resourceFile</code> = <code>bundle.properties</code></li>
	 * </ul>
	 * The method finds any files in <code>bundle</code> in <code>directory</code>,
	 * where resources are defined in <code>resourceFile</code> and returns the set of endings, which are bundle locales.
	 * <p>
	 * if <code>resourceFile</code> is defined as <code>messages.properties</code> or <code>messages_*.properties</code>, then
	 * all files in <code>directory</code> are considered. For example: 
	 * for messages_en.properties, messages_zh_CN.properties, messages_ru.properties returns the {@linkplain Set}&lt;String&gt; of [en, zh_CN, ru].
	 * </p>
	 * @param bundle the OSGI bundle to look into
	 * @param directory within the <code>bundle</code> where resource files reside
	 * @param resourceFile the name of general resource file or pattern: <code>bundle.properties</code> or <code>bundle_*.properties</code>
	 * &ndash; they are the same &ndash; the <code>.properties</code> suffix is for filtering files, but <code>bundle</code> become a pattern
	 * <code>bundle_*</code> where we then figure out localizations from the <code>_*</code> part of the pattern.
	 * @param includeSubfolders whether to look in sub-folders also
	 * @return the <code>set</code> of languages ISO codes.
	 */
	public static Set<String> getAvailableLocales(Bundle bundle, String directory, String resourceFile, boolean includeSubfolders){
		if(bundle == null) throw new IllegalArgumentException("bundle is NULL");
		if(resourceFile == null) throw new IllegalArgumentException("resourceFile is NULL");
		Set<String> languages = new TreeSet<>();
		String directoryPattern = "";
		if(directory != null && !directory.isEmpty()) {
			if(!directory.endsWith("/")) directory += "/";
			directoryPattern = "(?:"+Pattern.quote(directory)+")";
		}
		
		if(resourceFile.indexOf("_*")<0) {
			String[] parts = resourceFile.split("\\.");
			if(parts.length>1) {
				StringBuilder sb = new StringBuilder();
				sb.append(parts[0]);
				for( int i = 1; i<parts.length-1; i++) {
					sb.append(".").append(parts[i]);
				}
				sb.append("_*.").append(parts[parts.length-1]);
				resourceFile = sb.toString();
			} else {
				resourceFile +="_*";
			}
		}
		Enumeration<URL> langResources = bundle.findEntries(directory, resourceFile, includeSubfolders);
		if(langResources!=null) {
			String resourceRegex = "(.*)(?:_\\*)(\\..*)?";
			Pattern p = Pattern.compile(resourceRegex);
			Matcher m = p.matcher(resourceFile);
			if(m.find()){
				String resourceNamePattern = Pattern.quote(m.group(1));
				String fileExtPattern = Pattern.quote(m.group(2));
				String regex = "^(?:.*)?"+directoryPattern+((includeSubfolders)?"(?:.*?\\/)?":"")+"(?:"+resourceNamePattern+")(?:_)?([a-zA-Z]{2,3}(?:_[a-zA-Z]{2,3})?)?"+fileExtPattern;
				Pattern jsessionpat = Pattern.compile(regex);
				while(langResources.hasMoreElements()){
					URL langRes = langResources.nextElement();
					String entryName = langRes.toString();
					Matcher jm = jsessionpat.matcher(entryName);
					if(jm.find()){
						String langCode = jm.group(1);
						if(langCode!=null && !langCode.isEmpty()){
							languages.add(langCode);
						}
					}
				}
			}
		}
		

		return languages;
	}
	/**
	 * Gets localizations from bundles according <code>Bundle-Localization</code> parameter in MANIFEST.MF and
	 * if it isn't defined the default search pattern is <code>OSGI-INF/l10n/bundle</code> as defined in 
	 * <a href="https://osgi.org/specification/osgi.core/7.0.0/framework.module.html#i3189740">OSGi specification</a>.
	 */
	public static Set<String> getBundlesLocalizations() {
		Bundle bundles[] = FrameworkUtil.getBundle(L10nUtil.class).getBundleContext().getBundles();
		Set<String> bundleLocales = new HashSet<>();
		for (Bundle bundle : bundles) {
			String l10nFile = bundle.getHeaders().get("Bundle-Localization");
			if(l10nFile==null) l10nFile = "OSGI-INF/l10n/bundle";
			String file = getFilename(l10nFile);
			String directory = getBundlePath(l10nFile);
			//System.out.println(bundle.getSymbolicName()+" : " + l10nFile + ", dir:" + directory + ", file:" + file);
			if(file!=null) {
				bundleLocales.addAll( getAvailableLocales(bundle, directory, file, false) );
			}
		}
		
		//System.out.println("======LOCALES========");
		//bundleLocales.forEach( loc -> System.out.println(loc) );
		return bundleLocales;
	}

	
	/**
	 * Returns a three-letter abbreviation for this locale's country/region
	 * 
	 * @param the locale to fetch country code
	 * @return an uppercase ISO 3166 3-letter country code or empty string
	 * 
	 * @see ULocale#addLikelySubtags(ULocale)
	 * @see ULocale#getISO3Country()
	 */
	public static String getISO3Country(Locale locale) {
		ULocale ulocale = ULocale.addLikelySubtags(ULocale.forLocale(locale));
		return ulocale.getISO3Country();
	}
	
	//////////// PRIVATE METHODS ////////////////
	private static String getBundlePath(String filename) {
		int lastSeparatorIndex = filename.lastIndexOf("/");
		if(lastSeparatorIndex < 0 ) return "/";
		String uPath = filename.substring(0, lastSeparatorIndex);
		if(filename.startsWith("/")) return uPath;
		return "/" + uPath;
	}
	
	private static String getFilename(String filename) {
		int lastSeparatorIndex = filename.lastIndexOf("/");
		if(lastSeparatorIndex < 0 ) return filename + ".properties";
		if(filename.length() == lastSeparatorIndex+1) return null;
		return filename.substring(lastSeparatorIndex+1)+".properties";
	}
}
