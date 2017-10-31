package com.leo.ostbm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public SettingsPanel() {
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPanel spoilerPanel = new JPanel();
		spoilerPanel.setLayout(new BoxLayout(spoilerPanel, BoxLayout.PAGE_AXIS));
		spoilerPanel.setBorder(BorderFactory.createTitledBorder("Spoilers"));
		spoilerPanel.add(new JLabel(
				"<html>By default, OSTBM hides facepics that are exclusive to the Solstice route to prevent spoilers.<br>Please note that changing this will reload all facepics and <b>remove all custom facepics</b>.</html>"));
		JCheckBox solsticeFacepics = new JCheckBox("Hide Solstice facepics",
				Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
		solsticeFacepics.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Config.setBoolean(Config.KEY_HIDE_SOLSTICE_FACES, solsticeFacepics.isSelected());
				try {
					MakerPanel panel = Main.getPanel();
					panel.updateCurrentBox();
					Resources.initFaces();
					panel.updateFaces();
				} catch (IOException e1) {
					Main.resourceError(e1);
				}
			}
		});
		spoilerPanel.add(solsticeFacepics);
		add(spoilerPanel);
	}

}
