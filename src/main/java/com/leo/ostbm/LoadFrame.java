package com.leo.ostbm;

import javax.swing.*;
import java.awt.*;

public class LoadFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JLabel loadLabel;

    public LoadFrame(final String loadString, final boolean important) {
        setDefaultCloseOperation(important ? JFrame.EXIT_ON_CLOSE : WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        final Dimension size = new Dimension(320, 120);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setResizable(false);
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(MakerPanel.COLOR_TEXTBOX_B, 4));
        panel.setBackground(MakerPanel.COLOR_TEXTBOX);
        loadLabel = new JLabel(loadString);
        loadLabel.setFont(loadLabel.getFont().deriveFont(Font.BOLD, 20));
        loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadLabel.setForeground(Color.WHITE);
        panel.add(loadLabel, BorderLayout.CENTER);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setIconImages(Resources.getAppIcons());
        setVisible(true);
        requestFocus();
    }

    public LoadFrame(final boolean important) {
        this("Checking for updates...", important);
    }

    public void setLoadString(final String loadString) {
        loadLabel.setText(loadString);
    }

}
