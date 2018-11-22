package com.leo.ostbm;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ModifierHelpPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public ModifierHelpPanel() {
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(
                "<html>Modifiers are formatted like so:<br /><code>`m[parameters]</code><br />If a modifier does not need any parameters, the square brackets can be omitted.</html>"));
        add(new JSeparator());
        add(new JLabel(
                "<html><b><code>`c</code></b> - Changes the text color<br />3 parameters: custom RGB color (<code>[red,green,blue]</code>)<br />1 parameter: preset color OR hex color (<code>[h:RRGGBB]</code>)<br />no parameters: default color<br>Available preset colors are:</html>"));

        for (final Map.Entry<Integer, Color> entry : TextboxUtil.TEXTBOX_PRESET_COLORS.entrySet()) {
            String presets = "<html><p style=\"color:";
            presets += colorToHTML(entry.getValue()) + ";background-color:" + MakerPanel.HTMLC_TEXTBOX;
            presets += "\">" + (entry.getKey() + 1) + " - " + TextboxUtil.TEXTBOX_PRESET_COLOR_NAMES.get(entry.getKey())
                    + "</p></html>";
            add(new JLabel(presets));
        }
        add(new JLabel("Any other number will reset to the default color."));
        add(new JLabel("<html><b><code>`f[format]</code></b> - Changes the text format<br /><code>format</code> is a string consisting of the following characters:<br/>"
                + "<b>i</b> - <i>Italic</i><br /><b>u</b> - <u>Underline</u><br /><b>s</b> - <s>Strikethrough</s><br />"
                + "If <code>format</code> isn't specified, the text resets to its default format."));
        add(new JLabel(
                "<html><p style=\"color:red;\"><b>ALL MODIFIERS UNDER THIS LINE ONLY WORK IN ANIMATED TEXTBOXES!</b></p></html>"));
        add(new JSeparator());
        add(new JLabel(
                "<html><b><code>`d[frames]</code></b> - Delays the text for <code>frames + text_speed</code> frames</html>"));
        add(new JLabel(
                "<html><b><code>`s[frames]</code></b> - Sets the text speed to one character per <code>frames</code> frames. Basically the same as `d but for <i>all</i> characters</html>"));
        add(new JLabel(
                "<html><b><code>`@[face]</code></b> - Changes the textbox's facepic to <code>face</code>, or removes the facepic if <code>face</code> isn't specified</html>"));
        add(new JLabel(
                "<html><b><code>`i</code></b> - If at the start of a textbox, causes the text to appear instantly;<br/>If at the end of a textbox, causes the next textbox to interrupt the current textbox.</html>"));
    }

    public static String colorToHTML(final Color color) {
        String redStr = Integer.toHexString(color.getRed());
        if (redStr.length() == 1)
            redStr = "0" + redStr;
        String greenStr = Integer.toHexString(color.getGreen());
        if (greenStr.length() == 1)
            greenStr = "0" + greenStr;
        String blueStr = Integer.toHexString(color.getBlue());
        if (blueStr.length() == 1)
            blueStr = "0" + blueStr;
        return "#" + redStr + greenStr + blueStr;
    }

}
