package io.github.che4.i18n.menu.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.ULocale;

import io.github.che4.i18n.menu.Activator;
import io.github.che4.i18n.menu.util.E4Constants;

import java.util.Locale;

import javax.inject.Named;

@SuppressWarnings("restriction")
public class ChangeLocaleHandler {
	/*
	@org.eclipse.e4.core.di.annotations.CanExecute
	public boolean canExcecute() {
		return false;
	}
	*/
	
	@Execute
	private void execute(
			ILocaleChangeService lcs,
			MApplication application,
			TranslationService translate,
			IEclipseContext iContext,
			@Named(TranslationService.LOCALE) Locale currentLocale,
			@Named(E4Constants.ID_COMMAND_PARAMETER_CHANGE_LOCALE) String lang_tag) {

		
		Boolean previousRtlMode = (Boolean) application.getTransientData().get(E4Workbench.RTL_MODE);
		ULocale uNewLocale = ULocale.forLanguageTag(lang_tag);
		if( previousRtlMode != uNewLocale.isRightToLeft()) {
			String message = translate.translate("%io.github.che4.changeOrientationDialog", Activator.getContributorURI());
			ULocale uCurrentLocale = ULocale.forLocale(currentLocale);
			Shell shell = (Shell) iContext.get(IServiceConstants.ACTIVE_SHELL);
			if (MessageDialog.openConfirm(shell, uNewLocale.getDisplayLanguage(uCurrentLocale), message)) {
				lcs.changeApplicationLocale(uNewLocale.toLocale());
			}
		} else {
			lcs.changeApplicationLocale(uNewLocale.toLocale());
		}
	}
}