package com.leo.ostbm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MakerPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final FileNameExtensionFilter TBPROJ_FILTER = new FileNameExtensionFilter("Project files", "tbproj");

	public static final Color COLOR_TEXTBOX = Color.decode("0x180C1E");
	public static final Color COLOR_TEXTBOX_B = COLOR_TEXTBOX.brighter().brighter();

	public static final String A_CUSTOM_FACE = "customFace";
	public static final String A_REMOVE_BOX = "removeBox";
	public static final String A_PREV_BOX = "prevBox";
	public static final String A_NEXT_BOX = "nextBox";
	public static final String A_ADD_BOX = "addBox";
	public static final String A_MAKE_TEXTBOX = "makeTextbox";
	public static final String A_SAVE_TEXTBOX = "saveTextbox";

	class Textbox {
		public String face = Resources.FACE_BLANK;
		public String text = "";
	}

	private File projectFile;
	private int currentBox;
	private List<Textbox> boxes;
	private JComboBox<String> faceSelect;
	private JButton customFaceButton;
	private JTextArea textArea;
	private JButton removeBoxButton;
	private JButton prevBoxButton;
	private JLabel boxIndexLabel;
	private JButton nextBoxButton;
	private JButton addBoxButton;
	private JButton makeTextboxButton;

	public MakerPanel() {
		currentBox = 0;
		boxes = new LinkedList<>();
		boxes.add(new Textbox());
		setLayout(new BorderLayout());
		JPanel faceSelectPanel = new JPanel();
		faceSelectPanel.setLayout(new BoxLayout(faceSelectPanel, BoxLayout.LINE_AXIS));
		faceSelectPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		faceSelectPanel.add(new JLabel("Face: "));
		faceSelect = new JComboBox<String>();
		faceSelect.setModel(new DefaultComboBoxModel<>(Resources.getFaces()));
		faceSelect.setBackground(COLOR_TEXTBOX);
		faceSelect.setForeground(Color.WHITE);
		FacesComboBoxRenderer renderer = new FacesComboBoxRenderer();
		faceSelect.setRenderer(renderer);
		faceSelect.setToolTipText("Select a face to add to the textbox");
		faceSelectPanel.add(faceSelect);
		faceSelectPanel.add(new JLabel(" "));
		customFaceButton = new JButton("...");
		customFaceButton.addActionListener(this);
		customFaceButton.setActionCommand(A_CUSTOM_FACE);
		customFaceButton.setToolTipText("Add a custom face image");
		faceSelectPanel.add(customFaceButton);
		add(faceSelectPanel, BorderLayout.PAGE_START);
		textArea = new JTextArea();
		textArea.setFont(Resources.getFontBox());
		textArea.setBackground(COLOR_TEXTBOX);
		textArea.setForeground(Color.WHITE);
		textArea.setCaretColor(Color.WHITE);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		JPanel boxIndexPanel = new JPanel();
		removeBoxButton = new JButton("-");
		removeBoxButton.addActionListener(this);
		removeBoxButton.setActionCommand(A_REMOVE_BOX);
		removeBoxButton.setToolTipText("Remove a textbox");
		boxIndexPanel.add(removeBoxButton);
		prevBoxButton = new JButton("<");
		prevBoxButton.addActionListener(this);
		prevBoxButton.setActionCommand(A_PREV_BOX);
		prevBoxButton.setToolTipText("Go to previous textbox");
		boxIndexPanel.add(prevBoxButton);
		boxIndexLabel = new JLabel();
		updateBoxLabel();
		boxIndexPanel.add(boxIndexLabel);
		nextBoxButton = new JButton(">");
		nextBoxButton.addActionListener(this);
		nextBoxButton.setActionCommand(A_NEXT_BOX);
		nextBoxButton.setToolTipText("Go to next textbox");
		boxIndexPanel.add(nextBoxButton);
		addBoxButton = new JButton("+");
		addBoxButton.addActionListener(this);
		addBoxButton.setActionCommand(A_ADD_BOX);
		addBoxButton.setToolTipText("Add a textbox");
		boxIndexPanel.add(addBoxButton);
		bottomPanel.add(boxIndexPanel, BorderLayout.PAGE_START);
		makeTextboxButton = new JButton("Make a Textbox!");
		makeTextboxButton.addActionListener(this);
		makeTextboxButton.setActionCommand(A_MAKE_TEXTBOX);
		bottomPanel.add(makeTextboxButton, BorderLayout.PAGE_END);
		add(bottomPanel, BorderLayout.PAGE_END);
	}

	private void updateBoxLabel() {
		boxIndexLabel.setText((currentBox + 1) + " / " + boxes.size());
	}

	private void updateCurrentBox() {
		Textbox box = boxes.get(currentBox);
		box.face = faceSelect.getItemAt(faceSelect.getSelectedIndex());
		box.text = textArea.getText();
	}

	private void readCurrentBox() {
		Textbox box = boxes.get(currentBox);
		faceSelect.setSelectedItem(box.face);
		textArea.setText(box.text);
	}

	public boolean isProjectEmpty() {
		if (boxes == null)
			return true;
		if (boxes.isEmpty())
			return true;
		boolean emptyProject = true;
		for (Textbox box : boxes) {
			if (!box.text.isEmpty() || !Resources.FACE_BLANK.equals(box.face)) {
				emptyProject = false;
				break;
			}
		}
		return emptyProject;
	}

	public File getProjectFile() {
		return projectFile;
	}

	public void newProjectFile() throws IOException {
		updateCurrentBox();
		boolean emptyProject = isProjectEmpty();
		if (!emptyProject) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save the current project before creating a new project?",
					"Save before creating new project?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			else if (result == JOptionPane.YES_OPTION)
				saveProjectFile(null);
		}
		projectFile = null;
		boxes.clear();
		boxes.add(new Textbox());
		faceSelect.setSelectedItem(Resources.FACE_BLANK);
		textArea.setText("");
	}

	public void saveProjectFile(File dest) throws IOException {
		if (dest == null) {
			if (projectFile == null) {
				File sel = Main.openFileDialog(true, this, "Save project file", TBPROJ_FILTER);
				if (sel == null)
					return;
				projectFile = sel;
			}
			dest = projectFile;
		}
		if (dest.exists() && !dest.equals(projectFile)) {
			int result = JOptionPane.showConfirmDialog(this, "File \"" + dest + "\" already exists.\nOverwrite it?",
					"Destination file exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.NO_OPTION)
				return;
			dest.delete();
			dest.createNewFile();
		}
		updateCurrentBox();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter fw = new FileWriter(dest); BufferedWriter writer = new BufferedWriter(fw)) {
			gson.toJson(boxes, writer);
		} catch (IOException e) {
			throw e;
		}
		projectFile = dest;
		Config.set(Config.KEY_LAST_PROJECT_FILE, projectFile.getAbsolutePath());
	}

	public void loadProjectFile(File src) throws IOException {
		boolean emptyProject = isProjectEmpty();
		if (!emptyProject) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save the current project before loading another project?",
					"Save before loading other project?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			else if (result == JOptionPane.YES_OPTION)
				saveProjectFile(null);
		}
		if (src == null) {
			src = Main.openFileDialog(false, this, "Load project file", TBPROJ_FILTER);
			if (src == null)
				return;
		}
		Gson gson = new GsonBuilder().create();
		try (FileReader fr = new FileReader(src); BufferedReader reader = new BufferedReader(fr)) {
			Type type = new TypeToken<List<Textbox>>() {
			}.getType();
			boxes = gson.fromJson(reader, type);
		} catch (IOException e) {
			throw e;
		}
		projectFile = src;
		Config.set(Config.KEY_LAST_PROJECT_FILE, projectFile.getAbsolutePath());
		readCurrentBox();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String a = e.getActionCommand();
		JFileChooser fc;
		int ret;
		Textbox box;
		switch (a) {
		case A_CUSTOM_FACE:
			fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle("Open face image(s)");
			fc.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			ret = fc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File[] sels = fc.getSelectedFiles();
				for (File sel : sels) {
					String faceName = sel.getName();
					faceName = faceName.substring(0, faceName.lastIndexOf('.'));
					try {
						BufferedImage image = ImageIO.read(sel);
						if (image.getWidth() != 96 || image.getHeight() != 96) {
							JOptionPane.showMessageDialog(this, "Face must be 96 by 96!", "Bad face dimensions!",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						Resources.addFace(sel, faceName, image);
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this, "An exception occured while loading the face:\n" + e1,
								"Couldn't load face!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			String[] faces = Resources.getFaces();
			faceSelect.setModel(new DefaultComboBoxModel<>(faces));
			faceSelect.setSelectedIndex(faces.length - 1);
			break;
		case A_REMOVE_BOX:
			updateCurrentBox();
			if (!boxes.get(currentBox).text.trim().isEmpty()) {
				int result = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to delete textbox " + (currentBox + 1) + "?", "Confirm deleting textbox",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION)
					break;
			}
			if (boxes.size() == 1) {
				box = boxes.get(currentBox);
				box.face = Resources.FACE_BLANK;
				box.text = "";
			} else
				boxes.remove(currentBox);
			if (currentBox == boxes.size())
				currentBox--;
			if (currentBox < 0)
				currentBox = 0;
			updateBoxLabel();
			readCurrentBox();
			break;
		case A_PREV_BOX:
			updateCurrentBox();
			currentBox--;
			if (currentBox < 0)
				currentBox = 0;
			updateBoxLabel();
			readCurrentBox();
			break;
		case A_NEXT_BOX:
			updateCurrentBox();
			currentBox++;
			if (currentBox > boxes.size() - 1)
				currentBox = boxes.size() - 1;
			updateBoxLabel();
			readCurrentBox();
			break;
		case A_ADD_BOX:
			updateCurrentBox();
			Textbox lastBox = boxes.get(boxes.size() - 1);
			box = new Textbox();
			box.face = lastBox.face;
			boxes.add(box);
			currentBox++;
			if (currentBox > boxes.size() - 1)
				currentBox = boxes.size() - 1;
			updateBoxLabel();
			readCurrentBox();
			break;
		case A_MAKE_TEXTBOX:
			updateCurrentBox();
			BufferedImage boxesImage = new BufferedImage(608, 128 * boxes.size() + 2 * (boxes.size() - 1),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics big = boxesImage.getGraphics();
			for (int i = 0; i < boxes.size(); i++) {
				Textbox b = boxes.get(i);
				String text = b.text.trim();
				if (text.isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"Textbox " + (i + 1) + " is blank!\nPlease write something there.", "Text cannot be blank!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String[] lines = text.split("\n");
				int linesNum = lines.length;
				if (linesNum > 4) {
					JOptionPane.showMessageDialog(this,
							"Textbox " + (i + 1) + " has too many lines!\nIt has " + linesNum
									+ " lines, but a single textbox can only fit 4 lines.",
							"Too many lines!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int maxLen = 47;
				String errorBit = "with a face";
				if (Resources.FACE_BLANK.equals(b.face)) {
					maxLen += 10;
					errorBit = "without a face";
				}
				for (int l = 0; l < lines.length; l++) {
					int lineLen = lines[l].length();
					if (lineLen > maxLen) {
						JOptionPane.showMessageDialog(this,
								"Line " + (l + 1) + " in textbox " + (i + 1) + " is too long!\nIt has " + lineLen
										+ " characters, but textboxes " + errorBit + " can only fit " + maxLen
										+ " characters per line.",
								"Line too long!", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				drawTextbox(big, b.face, text, 0, 130 * i, i < boxes.size() - 1);
			}
			JFrame previewFrame = new JFrame();
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			final Dimension size = new Dimension(636, 128 * 5 + 2 * 4 + 40);
			previewFrame.setPreferredSize(size);
			previewFrame.setMaximumSize(size);
			previewFrame.setMinimumSize(size);
			previewFrame.setResizable(false);
			previewFrame.add(new PreviewPanel(boxesImage));
			previewFrame.pack();
			previewFrame.setLocationRelativeTo(null);
			previewFrame.setTitle("Textbox(es) preview");
			previewFrame.setVisible(true);
			break;
		default:
			System.out.println("Undefined action: " + a);
			break;
		}
	}

	public static BufferedImage drawTextbox(String face, String text) {
		BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		drawTextbox(g, face, text, 0, 0, false);
		return ret;
	}

	public static void drawTextbox(Graphics g, String face, String text, int x, int y, boolean drawArrow) {
		g.drawImage(Resources.getBox(), x, y, null);
		BufferedImage faceImage = Resources.getFace(face);
		if (faceImage != null)
			g.drawImage(faceImage, x + 496, y + 16, null);
		if (drawArrow)
			g.drawImage(Resources.getArrow(), x + 299, y + 118, null);
		drawTextboxString(g, text, x + 20, y + 10);
	}

	private static void drawTextboxString(Graphics g, String str, int x, int y) {
		g.setFont(Resources.getFontBox());
		g.setColor(Color.WHITE);
		final int lineSpace = g.getFontMetrics().getHeight() + 1;
		for (String line : str.split("\n")) {
			y += lineSpace;
			g.drawString(line, x, y);
		}
	}

	class FacesComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

		private static final long serialVersionUID = 1L;

		public FacesComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (isSelected)
				setBackground(COLOR_TEXTBOX_B);
			else
				setBackground(COLOR_TEXTBOX);
			ImageIcon faceIcon = Resources.getFaceIcon(value);
			if (faceIcon == null)
				setIcon(Resources.getFaceIcon(Resources.FACE_BLANK));
			else
				setIcon(faceIcon);
			String text = "<html><font color=white>" + value;
			File faceFile = Resources.getFaceFile(value);
			if (faceFile != null)
				text += "</font><br><font color=gray><i>" + faceFile.getPath() + "</i>";
			text += "</font></html>";
			setText(text);
			return this;
		}

	}

}
