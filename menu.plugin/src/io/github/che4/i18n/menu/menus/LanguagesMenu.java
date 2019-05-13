package io.github.che4.i18n.menu.menus;

import java.util.List;

import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;

import io.github.che4.i18n.menu.LanguageMenuProvider;

public class LanguagesMenu {
	@AboutToShow
	private void aboutToShow(List<MMenuElement> items, LanguageMenuProvider lmp) {
		lmp.getLanguageItems().forEach( mel -> items.add(mel));
	}
}