package com.leo.ostbm;

import javax.swing.*;
import java.io.IOException;

public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public SettingsPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		initGeneralPanel();
		initSpoilerPanel();
	}

	private void reloadFacepics() {
		try {
			final MakerPanel panel = Main.getPanel();
			panel.updateCurrentBox();
			Resources.initFaces();
			panel.updateFaces();
		} catch (final IOException e1) {
			Main.resourceError(e1);
		}
	}

	private void initGeneralPanel() {
		final JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));
		generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
		final JCheckBox cloneFace = new JCheckBox(
				"Copy facepic from currently selected when creating new textbox",
				Config.getBoolean(Config.KEY_COPY_FACEPICS, true));
		cloneFace
				.addActionListener(e -> Config.setBoolean(Config.KEY_COPY_FACEPICS, cloneFace.isSelected()));
		generalPanel.add(cloneFace);
		final JButton reloadFacepics = new JButton("Reload facepics");
		reloadFacepics.addActionListener(e -> reloadFacepics());
		generalPanel.add(reloadFacepics);
		add(generalPanel);
	}

	private void initSpoilerPanel() {
		final JPanel spoilerPanel = new JPanel();
		spoilerPanel.setLayout(new BoxLayout(spoilerPanel, BoxLayout.PAGE_AXIS));
		spoilerPanel.setBorder(BorderFactory.createTitledBorder("Spoilers"));
		spoilerPanel.add(new JLabel(
				"<html>By default, OSTBM hides facepics that are exclusive to the Solstice route to prevent spoilers.<br>" +
						"Please note that changing this will reload all facepics.</html>"));
		final JCheckBox solsticeFacepics = new JCheckBox("Hide Solstice facepics",
				Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
		solsticeFacepics.addActionListener(e -> {
			Config.setBoolean(Config.KEY_HIDE_SOLSTICE_FACES, solsticeFacepics.isSelected());
			reloadFacepics();
		});
		spoilerPanel.add(solsticeFacepics);
		add(spoilerPanel);
	}

}
