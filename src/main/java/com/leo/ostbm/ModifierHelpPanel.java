package com.leo.ostbm;

import java.awt.Color;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

public class ModifierHelpPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public ModifierHelpPanel() {
		setBorder(UIManager.getBorder("TitledBorder.border"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel(
				"<html>Modifiers are formatted like so:<br><code>\\m[parameters]</code><br>If a modifier does not need any parameters, the square brackets can be omitted.</html>"));
		add(new JSeparator());
		add(new JLabel(
				"<html><b>\\c</b> - Changes the text color<br>3 parameters: RGB color<br>1 parameter: preset color<br>no parameters: default color<br>Available preset colors are:</html>"));

		for (Map.Entry<Integer, Color> entry : MakerPanel.TEXTBOX_PRESET_COLORS.entrySet()) {
			String presets = "<html><p style=\"color:";
			Color color = entry.getValue();
			String redStr = Integer.toHexString(color.getRed());
			if (redStr.length() == 1)
				redStr = "0" + redStr;
			String greenStr = Integer.toHexString(color.getGreen());
			if (greenStr.length() == 1)
				greenStr = "0" + greenStr;
			String blueStr = Integer.toHexString(color.getBlue());
			if (blueStr.length() == 1)
				blueStr = "0" + blueStr;
			String colorStr = "#" + redStr + greenStr + blueStr;
			presets += colorStr + ";";
			if (entry.getKey() == 0 || entry.getKey() == 3 || entry.getKey() == 6)
				presets += "background-color:black;";
			presets += "\">" + (entry.getKey() + 1) + " - " + MakerPanel.TEXTBOX_PRESET_COLOR_NAMES.get(entry.getKey())
					+ "</p></html>";
			add(new JLabel(presets));
		}
		add(new JLabel("Any other number will reset to the default color."));
		add(new JLabel("<html><p style=\"color:red;\">ALL MODIFIERS UNDER THIS LINE ONLY WORK IN ANIMATED TEXTBOXES!</p></html>"));
		add(new JSeparator());
		add(new JLabel("<html><b>\\d[frames]</b> - Delays the text for <code>frames</code> frames</html>"));
		add(new JLabel("<html><b>\\@[face]</b> - Changes the textbox's facepic to <code>face</code></html>"));
		add(new JLabel("<html><b>\\i</b> - Causes the next textbox to interrupt the current textbox. <b>ONLY WORKS AT THE END OF A TEXTBOX!</b></html>"));
	}

}
