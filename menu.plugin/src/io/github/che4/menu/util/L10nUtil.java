package io.github.che4.menu.util;

import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class L10nUtil {
	public static final Locale LOCALE_DEFAULT = Locale.ENGLISH;
	static {
		Locale.setDefault(LOCALE_DEFAULT);
	}

	public static Set<String> getAvailableLocales() {
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
	 * @param directory in <code>bundle</code> where resource files reside
	 * @param resourceFile the name of general resource file or pattern: <code>bundle.properties</code> or <code>bundle_*.properties</code>
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
				String regex = "^(?:.*)?"+directoryPattern+((includeSubfolders)?"(?:.*?\\/)?":"")+"(?:"+resourceNamePattern+")(?:_)?([a-zA-Z]{2,3})?"+fileExtPattern;
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
}
