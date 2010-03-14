package aephyr.swing.nimbus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;


class CodeTransfer implements ActionListener, ChangeListener {
	
	private static final String LOCATION_EXPORT = "Save to Directory";
	private static final String LOCATION_IMPORT = "Open File";
	private static final String DIALOG_EXPORT = "Export";
	private static final String DIALOG_IMPORT = "Import";
	private static final String TAB_FILE_EXPORT = "Export to File";
	private static final String TAB_TEXT_EXPORT = "Export to Text";
	private static final String TAB_FILE_IMPORT = "Import from File";
	private static final String TAB_TEXT_IMPORT = "Import from Text";
	private static final int FILE_TAB = 0;
	private static final int TEXT_TAB = 1;
	
	CodeTransfer(JFrame frame) {

		JLabel pkgLabel = new JLabel("Package Name:");
		JLabel clsLabel = new JLabel("Class Name:");
		JLabel mtdLabel = new JLabel("Method Name:");
		JLabel indLabel = new JLabel("Indentation:");
		packageField = new JTextField();
		classField = new JTextField("NimbusTheme");
		methodField = new JTextField("loadTheme");
		indentTabs = new JRadioButton("Tabs", true);
		indentJava = new JRadioButton("Java Convention", false);
		ButtonGroup group = new ButtonGroup();
		group.add(indentTabs);
		group.add(indentJava);
		location = new JTextField(25);
		JButton browse = new JButton("Browse...");
		browse.addActionListener(this);
		
		options = NimbusThemeCreator.titled(new JPanel(null), "Options");
		GroupLayout layout = new GroupLayout(options);
		options.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
				.addComponent(pkgLabel).addComponent(clsLabel).addComponent(mtdLabel).addComponent(indLabel))
			.addGap(5)
			.addGroup(layout.createParallelGroup()
				.addComponent(packageField).addComponent(classField).addComponent(methodField)
				.addGroup(layout.createSequentialGroup()
						.addComponent(indentTabs)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(indentJava))));
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createBaselineGroup(false, true)
				.addComponent(pkgLabel).addComponent(packageField))
			.addGroup(layout.createBaselineGroup(false, true)
				.addComponent(clsLabel).addComponent(classField))
			.addGroup(layout.createBaselineGroup(false, true)
				.addComponent(mtdLabel).addComponent(methodField))
			.addGap(3).addGroup(layout.createBaselineGroup(false, true)
				.addComponent(indLabel).addComponent(indentTabs).addComponent(indentJava)));
		
		JPanel locationPanel = new JPanel(null);
		locationBorder = new TitledBorder(LOCATION_EXPORT);
		locationPanel.setBorder(locationBorder);
		layout = new GroupLayout(locationPanel);
		locationPanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(location).addComponent(browse));
		int prf = GroupLayout.PREFERRED_SIZE;
		layout.setVerticalGroup(layout.createBaselineGroup(false, true)
				.addComponent(location, prf, prf, prf)
				.addComponent(browse, prf, prf, prf));
		
		JPanel file = new JPanel(null);
		layout = new GroupLayout(file);
		file.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(options).addComponent(locationPanel));
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(options, prf, prf, prf).addComponent(locationPanel, prf, prf, prf));
		
		text = new JTextArea();
		text.setEditable(false);
		
		tabs = new JTabbedPane();
		tabs.addChangeListener(this);
		tabs.addTab(TAB_FILE_EXPORT, file);
		tabs.addTab(TAB_TEXT_EXPORT, new JScrollPane(text,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		ok = new JButton("OK");
		ok.addActionListener(this);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		
		JPanel content = new JPanel(null);
		layout = new GroupLayout(content);
		content.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(tabs).addGroup(layout.createSequentialGroup()
				.addGap(0, 200, Short.MAX_VALUE).addComponent(ok)
				.addGap(3).addComponent(cancel).addGap(5)));
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(tabs, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addGroup(layout.createBaselineGroup(false, true)
				.addComponent(ok).addComponent(cancel))
			.addGap(5));
		layout.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		
		dialog = new JDialog(frame, true);
		dialog.setContentPane(content);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
	}
	
	private JDialog dialog;
	private JTabbedPane tabs;
	private boolean isExport;
	
	private JComponent options;
	private JTextField packageField;
	private JTextField classField;
	private JTextField methodField;
	private JRadioButton indentTabs;
	private JRadioButton indentJava;
	
	private JTextField location;
	private TitledBorder locationBorder;
	
	private JButton ok;
	
	private JTextArea text;
	private boolean validTextArea;
	
	private JFileChooser browse;
	
	void showImportDialog() {
		isExport = false;
		location.setText(null);
		locationBorder.setTitle(LOCATION_IMPORT);
		tabs.setTitleAt(FILE_TAB, TAB_FILE_IMPORT);
		tabs.setTitleAt(TEXT_TAB, TAB_TEXT_IMPORT);
		tabs.setSelectedIndex(FILE_TAB);
		options.setVisible(false);
		text.setEditable(true);
		dialog.setTitle(DIALOG_IMPORT);
		ok.getRootPane().setDefaultButton(ok);
		dialog.setVisible(true);
	}
	
	void showExportDialog() {
		isExport = true;
		location.setText(null);
		locationBorder.setTitle(LOCATION_EXPORT);
		tabs.setTitleAt(FILE_TAB, TAB_FILE_EXPORT);
		tabs.setTitleAt(TEXT_TAB, TAB_TEXT_EXPORT);
		tabs.setSelectedIndex(FILE_TAB);
		options.setVisible(true);
		text.setEditable(false);
		validTextArea = false;
		dialog.setTitle(DIALOG_EXPORT);
		ok.getRootPane().setDefaultButton(ok);
		dialog.setVisible(true);
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (isExport && !validTextArea && tabs.getSelectedIndex() == TEXT_TAB) {
			validTextArea = true;
			StringWriter writer = new StringWriter();
			try {
				doExport(writer, null);
				text.setText(writer.toString());
			} catch (IOException x) {}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Browse...") {
			if (browse == null) {
				browse = new JFileChooser();
				browse.setMultiSelectionEnabled(false);
			}
			browse.setFileSelectionMode(isExport ?
					JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
			if (JFileChooser.APPROVE_OPTION == (isExport ? 
					browse.showSaveDialog(null) : browse.showOpenDialog(null))) {
				File file = browse.getSelectedFile();
				location.setText(file.getPath());
			}
		} else if (e.getActionCommand() == "OK") {
			if (isExport ? doExport() : doImport())
				dispose();
		} else if (e.getActionCommand() == "Cancel") {
			dispose();
		}
	}
	
	private void dispose() {
		text.setText(null);
		dialog.dispose();
	}
	
	/**
	 * @return true if exportation was successful
	 */
	private boolean doExport() {
		if (tabs.getSelectedIndex() == FILE_TAB) {
			String pkg = packageField.getText();
			String cls = classField.getText();
			String mtd = methodField.getText();
			File dir = new File(location.getText());
			try {
				if (!dir.isDirectory()) {
					if (dir.isFile())
						return CodeTransfer.error(
								"Invalid location:\n\t" +
								dir.getCanonicalPath() +
								"\nLocation must be a directory.");
					if (!CodeTransfer.confirm(
							"Directory does not exist:\n\t" +
							dir.getCanonicalPath() +
							"\nCreate?"))
						return false;
					dir.mkdirs();
					if (!dir.isDirectory())
						return CodeTransfer.error(
								"Unable to create directory:\n\t" +
								dir.getCanonicalPath());
				}
				File file = new File(dir, cls.concat(".java"));
				if (file.exists()) {
					if (!CodeTransfer.confirm(
							"File already exists:\n\t" +
							file.getCanonicalPath() +
							"\nOverwrite?"))
						return false;
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				if (pkg != null && !pkg.isEmpty()) {
					writer.write("package ");
					writer.write(pkg);
					writer.write(';');
					writer.newLine();
					writer.newLine();
				}
				writer.write("import javax.swing.*;");
				writer.newLine();
//				writer.write("import javax.swing.plaf.*;");
//				writer.newLine();
				writer.write("import java.awt.*;");
				writer.newLine();
				writer.newLine();
				writer.write("public class ");
				writer.write(cls);
				writer.write(" {");
				writer.newLine();
				String indent = indentTabs.isSelected() ? "\t" : "    ";
				writer.write(indent);
				writer.write("public static void ");
				writer.write(mtd);
				writer.write("() {");
				writer.newLine();
				doExport(writer, indentTabs.isSelected() ? "\t\t" : "\t");
				writer.write(indent);
				writer.write('}');
				writer.newLine();
				writer.write('}');
				writer.flush();
				writer.close();
			} catch (IOException x) {
				return CodeTransfer.error("IOException: "+x.getMessage());
			}
		}
		return true;
	}
	

	private void doExport(Writer writer, String prefix) throws IOException {
		BufferedWriter buf = writer instanceof BufferedWriter ? (BufferedWriter)writer : null;
		UIDefaults def = UIManager.getDefaults();
		UIDefaults lnfDef = UIManager.getLookAndFeelDefaults();
		for (Object key : def.keySet()) {
			Object obj = def.get(key);
			if (obj.equals(lnfDef.get(key)))
				continue;
			Type type = Type.getType(obj);
			switch (type) {
			// unsupported types
			case Painter: case Icon: case Object:
				continue;
			}
			if (prefix != null)
				writer.write(prefix);
			writer.write("UIManager.put(\"");
			writer.write(key.toString());
			switch (type) {
			case Color:
				Color color = (Color)obj;
//				writer.write("\", new ColorUIResource(0x");
				writer.write("\", new Color(0x");
				writer.write(Integer.toHexString(color.getRGB() & 0xffffff));
				writer.write("));");
				break;
			case Painter:
				throw new IllegalStateException();
			case Insets:
				Insets insets = (Insets)obj;
//				writer.write("\", new InsetsUIResource(");
				writer.write("\", new Insets(");
				writer.write(Integer.toString(insets.top));
				writer.write(", ");
				writer.write(Integer.toString(insets.left));
				writer.write(", ");
				writer.write(Integer.toString(insets.bottom));
				writer.write(", ");
				writer.write(Integer.toString(insets.right));
				writer.write("));");
				break;
			case Font:
				Font font = (Font)obj;
//				writer.write("\", new FontUIResource(\"");
				writer.write("\", new Font(\"");
				writer.write(font.getFamily());
				writer.write("\", ");
				String style = font.isBold() ? "Font.BOLD" : null;
				style = font.isItalic() ?
						style == null ? "Font.ITALIC" : style + " | " + "Font.ITALIC"
								: "Font.PLAIN";
				writer.write(style);
				writer.write(", ");
				writer.write(font.getSize());
				writer.write("));");
				break;
			case Boolean:
				writer.write("\", Boolean.");
				writer.write(obj == Boolean.TRUE ? "TRUE" : "FALSE");
				writer.write(");");
				break;
			case Integer:
				writer.write("\", new Integer(");
				writer.write(obj.toString());
				writer.write("));");
				break;
			case String:
				writer.write("\", \"");
				writer.write(obj.toString());
				writer.write('"');
				writer.write(");");
				break;
			case Icon:
				throw new IllegalStateException();
			case Dimension:
				Dimension size = (Dimension)obj;
				writer.write("\", new Dimension(");
//				writer.write("\", new DimensionUIResource(");
				writer.write(Integer.toString(size.width));
				writer.write(", ");
				writer.write(Integer.toString(size.height));
				writer.write("));");
				break;
			case Object:
				throw new IllegalStateException();
			}
			if (buf != null) {
				buf.newLine();
			} else {
				writer.write('\n');
			}
		}
	}
	
	static boolean importThemeFromFile(File file) throws IOException {
		FileReader reader = new FileReader(file);
		long len = file.length();
		char[] c = new char[(int)len+1];
		for (int r, o=0, l=c.length; (r=reader.read(c, o, l))>=0;) {
			o += r;
			l -= r;
		}
		String java = new String(c, 0, c.length-1);
		Matcher matcher = Pattern.compile("UIManager\\.put[^;]+").matcher(java);
		ArrayList<String> statements = new ArrayList<String>();
		while (matcher.find())
			statements.add(matcher.group());
		return doImport(statements.toArray(new String[statements.size()]));
	}
	
	
	/**
	 * @return true if importation was successful
	 */
	private boolean doImport() {
		if (tabs.getSelectedIndex() == FILE_TAB) {
			try {
				File file = new File(location.getText());
				if (!file.isFile())
					return CodeTransfer.error("Invalid File:\n\t" + file.getCanonicalPath());
				return importThemeFromFile(file);
			} catch (IOException x) {
				return CodeTransfer.error("IOException: "+x.getMessage());
			}
		} else if (tabs.getSelectedIndex() == TEXT_TAB) {
			String statements = text.getText();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null)
				return doImport(statements.split(";"));
			StringBuilder s = new StringBuilder(statements.length()+200);
			String cls = "NimbusTheme";
			s.append("import javax.swing.*;\n");
			s.append("import javax.swing.plaf.*;\n");
			s.append("import java.awt.*;\n");
			s.append("public class ").append(cls).append(" {\n");
			s.append("\tpublic static void loadTheme() {\n");
			s.append(statements);
			s.append("\t}\n}");
			
			CompilationTask task = compiler.getTask(null, null, null, null, null,
					Arrays.asList(new MemoryFileObject(cls, s.toString())));
			boolean success = task.call();
			if (!success)
				return CodeTransfer.error("Unable to compile code.");
			try {
				Class.forName(cls).getDeclaredMethod("loadTheme", (Class[])null)
					.invoke(null, (Object[])null);
				File file = new File(".", cls.replace('.', File.separatorChar).concat(".class"));
				if (file.exists())
					file.delete();
			} catch (Exception x) {
				return CodeTransfer.error(x.getClass().getSimpleName() + ": " + x.getMessage());
			}
		}
		return true;
	}

	/**
	 * @param statements an array of statements to interpret
	 * @return true if all statements were successfully interpreted
	 */
	private static boolean doImport(String[] statements) {
		Matcher matcher = Pattern.compile(
				"\\QUIManager.put(\\E\"([^\"]+)\",\\s*(.+)\\s*\\)$").matcher("");
		ArrayList<String> error = new ArrayList<String>();
		LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
		for (String statement : statements) {
			matcher.reset(statement);
			if (matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				try {
					if (value.startsWith("new ")) {
						int idx = value.indexOf('(');
						String name = value.substring(4, idx);
						Class<?> cls;
						if (name.equals("Color")) {
							cls = Color.class;
						} else if (name.equals("Insets")) {
							cls = Insets.class;
						} else if (name.equals("Integer")) {
							cls = Integer.class;
						} else if (name.equals("Font")) {
							cls = Font.class;
						} else if (name.equals("Dimension")) {
							cls = Dimension.class;
						} else {
							throw new Exception();
						}
						String[] params = value.substring(idx+1, value.length()-1).split(",");
						Class[] types = new Class[params.length];
						Object[] args = new Object[params.length];
						for (int i=params.length; --i>=0;) {
							String p = params[i].trim();
							if (Character.isDigit(p.charAt(0))) {
								types[i] = int.class;
								args[i] = new Integer(p.startsWith("0x") ?
										Integer.parseInt(p.substring(2), 16) :
										Integer.parseInt(p));
							} else if (p.charAt(0) == '"') {
								if (p.charAt(p.length()-1) != '"')
									throw new Exception();
								types[i] = String.class;
								args[i] = p.substring(1, p.length()-1);
							} else if (p.charAt(0) == 'F') {
								p = p.replace("\\s+", "");
								types[i] = int.class;
								if (p.equals("Font.PLAIN")) {
									args[i] = new Integer(Font.PLAIN);
								} else if (p.equals("Font.BOLD")) {
									args[i] = new Integer(Font.BOLD);
								} else if (p.equals("Font.ITALIC")) {
									args[i] = new Integer(Font.ITALIC);
								} else if (p.equals("Font.BOLD|Font.ITALIC") || p.equals("Font.ITALIC|Font.BOLD")) {
									args[i] = new Integer(Font.BOLD | Font.ITALIC);
								} else {
									types[i] = String.class;
									if (p.equals("Font.DIALOG")) {
										args[i] = Font.DIALOG;
									} else if (p.equals("Font.DIALOG_INPUT")) {
										args[i] = Font.DIALOG_INPUT;
									} else if (p.equals("Font.MONOSPACED")) {
										args[i] = Font.MONOSPACED;
									} else if (p.equals("Font.SANS_SERIF")) {
										args[i] = Font.SANS_SERIF;
									} else if (p.equals("Font.SERIF")) {
										args[i] = Font.SERIF;
									} else {
										throw new Exception();
									}
								}
							} else {
								throw new Exception();
							}
						}
						map.put(key, cls.getConstructor(types).newInstance(args));
					} else if (value.charAt(0) == 'B') {
						if (value.equals("Boolean.TRUE")) {
							map.put(key, Boolean.TRUE);
						} else if (value.equals("Boolean.FALSE")) {
							map.put(key, Boolean.FALSE);
						} else {
							throw new Exception();
						}
					} else if (value.charAt(0) == '"' && value.charAt(value.length()-1) == '"') {
						map.put(key, value.substring(1, value.length()-1));
					} else {
						throw new Exception();
					}
					continue;
				} catch (Exception x) {
					//x.printStackTrace(System.out);
					// any exceptions (e.g NumberFormatException or the jungle of exceptions thrown by newInstance)
					// should be caught and the statement added to the error list
					// also, plain Exceptions are thrown above if a statement isn't recognized
				}
			} else {
				if (statement.trim().isEmpty())
					continue;
			}
			error.add(statement);
		}
		if (map.isEmpty())
			return error("No valid statements were found.");
		if (!error.isEmpty()) {
			JPanel message = new JPanel(new BorderLayout());
			message.add(new JLabel("The following statements were not recognized:"), BorderLayout.NORTH);
			message.add(new JScrollPane(new JList(error.toArray())), BorderLayout.CENTER);
			message.add(new JLabel("Continue importing the recognized statements?"), BorderLayout.SOUTH);
			if (!confirm(message))
				return false;
		}
		for (Entry<String,Object> entry : map.entrySet())
			UIManager.put(entry.getKey(), entry.getValue());
		return true;
	}

	/**
	 * @param msg confirmation message
	 * @return true if the user confirmed
	 */
	private static boolean confirm(Object msg) {
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
				null, msg, "Confirm",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * @param msg error message
	 * @return always returns false
	 */
	private static boolean error(String msg) {
		JOptionPane.showMessageDialog(
				null, msg, "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	

	/**
	 * Implementation from JavaDoc for {@link javax.tools.JavaCompiler} (JavaSourceFromString)
	 */
	static class MemoryFileObject extends SimpleJavaFileObject {
		final String code;

		MemoryFileObject(String name, String code) {
			super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
	
}
