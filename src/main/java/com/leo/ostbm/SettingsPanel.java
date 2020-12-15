package com.leo.ostbm;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private GridBagConstraints rootC;

    public SettingsPanel() {
        setLayout(new GridBagLayout());
        rootC = new GridBagConstraints();
        rootC.gridx = rootC.gridy = 0;
        rootC.fill = GridBagConstraints.HORIZONTAL;
        rootC.weightx = 1;
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
        generalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
        final JCheckBox cloneFace = new JCheckBox(
                "Copy facepic from currently selected when creating new textbox",
                Config.getBoolean(Config.KEY_COPY_FACEPICS, true));
        cloneFace
                .addActionListener(e -> Config.setBoolean(Config.KEY_COPY_FACEPICS, cloneFace.isSelected()));
        generalPanel.add(cloneFace, c);
        c.gridy++;
        final JCheckBox opaqueTextboxes = new JCheckBox(
                "Use opaque textbox for non-animated textboxes",
                Config.getBoolean(Config.KEY_OPAQUE_TEXTBOXES, false));
        opaqueTextboxes
                .addActionListener(e -> Config.setBoolean(Config.KEY_OPAQUE_TEXTBOXES, opaqueTextboxes.isSelected()));
        generalPanel.add(opaqueTextboxes, c);
        c.gridy++;
        final JButton reloadFacepics = new JButton("Reload facepics");
        reloadFacepics.addActionListener(e -> reloadFacepics());
        generalPanel.add(reloadFacepics, c);
        c.gridy++;
        add(generalPanel, rootC);
        rootC.gridy++;
    }

    private void initSpoilerPanel() {
        final JPanel spoilerPanel = new JPanel();
        spoilerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        spoilerPanel.setBorder(BorderFactory.createTitledBorder("Spoilers"));
        spoilerPanel.add(new JLabel(
                "<html>By default, OSTBM hides facepics that are exclusive to the Solstice route to prevent spoilers.<br>" +
                        "Please note that changing this will reload all facepics.</html>"), c);
        c.gridy++;
        final JCheckBox solsticeFacepics = new JCheckBox("Hide Solstice facepics",
                Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
        solsticeFacepics.addActionListener(e -> {
            Config.setBoolean(Config.KEY_HIDE_SOLSTICE_FACES, solsticeFacepics.isSelected());
            reloadFacepics();
        });
        spoilerPanel.add(solsticeFacepics, c);
        c.gridy++;
        add(spoilerPanel, rootC);
        rootC.gridy++;
    }

}
