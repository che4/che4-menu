package io.github.che4.menu.menus;

import java.util.List;

import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;

import io.github.che4.menu.LanguageMenuProvider;

public class LanguagesMenu {
	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, LanguageMenuProvider lmp) {
		lmp.getLanguageItems().forEach( mel -> items.add(mel));
		//items.add(lmp.getLanguageMenus());
	}
}