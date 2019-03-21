package io.github.che4.menu.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.ui.model.application.MApplication;

import io.github.che4.menu.util.E4Constants;

import javax.inject.Named;

public class ChangeLocaleHandler {
	@Execute
	public void execute(
			ILocaleChangeService lcs,
			@Named(E4Constants.ID_COMMAND_PARAMETER_CHANGE_LOCALE) String lang,
			MApplication application) {
		lcs.changeApplicationLocale(lang);
		//application.updateLocalization();
	}
}