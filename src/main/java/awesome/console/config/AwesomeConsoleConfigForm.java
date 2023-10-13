package awesome.console.config;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class AwesomeConsoleConfigForm implements AwesomeConsoleDefaults {
	public JPanel mainpanel;
	public JCheckBox debugModeCheckBox;
	public JCheckBox limitLineMatchingByCheckBox;
	public JFormattedTextField maxLengthTextField;
	public JCheckBox matchLinesLongerThanCheckBox;
	public JCheckBox searchForURLsCheckBox;
	public JCheckBox searchForFilesCheckBox;
	public JCheckBox ignorePatternCheckBox;
	public JTextField ignorePatternTextField;
	public JCheckBox fixChooseTargetFileCheckBox;
	public JCheckBox fileTypesCheckBox;
	public JTextField fileTypesTextField;

    private void createUIComponents() {
		setupDebugMode();
		setupLineLimit();
		setupSplitLineIntoChunk();
		setupMatchURLs();
		setupMatchFiles();
		setupIgnorePattern();
		setupFixChooseTargetFileCheckBox();
		setupFileTypes();
	}

	private void setupRestore(JComponent component, ActionListener listener) {
		final JPopupMenu popup = new JPopupMenu("Defaults");
		
		final JMenuItem item = popup.add("Restore defaults");
		item.setMnemonic(KeyEvent.VK_R);
		item.addActionListener(listener);

		component.setComponentPopupMenu(popup);
	}

	private void bindCheckBoxTextField(JCheckBox checkBox, JTextField textField, boolean defaultEnabled, String defaultText) {
		bindCheckBoxTextField(checkBox, textField);
		setupRestore(textField, e -> setupCheckBoxTextField(checkBox, defaultEnabled, textField, defaultText));
	}

	private void bindCheckBoxTextField(JCheckBox checkBox, JTextField textField) {
		checkBox.addActionListener(e -> {
			final boolean enabled = checkBox.isSelected();
			textField.setEnabled(enabled);
			textField.setEditable(enabled);
		});
	}

	private void setupCheckBoxTextField(JCheckBox checkBox, boolean enabled, JTextField textField, String text) {
		checkBox.setSelected(enabled);
		textField.setText(text);
		textField.setEnabled(enabled);
		textField.setEditable(enabled);
	}

	private void setupDebugMode() {
		debugModeCheckBox = new JCheckBox("debugModeCheckBox");
		setupRestore(debugModeCheckBox, e -> debugModeCheckBox.setSelected(DEFAULT_DEBUG_MODE));
	}

	private void setupLineLimit() {
		limitLineMatchingByCheckBox = new JCheckBox("limitLineMatchingByCheckBox");
		limitLineMatchingByCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final boolean selected = limitLineMatchingByCheckBox.isSelected();
				maxLengthTextField.setEnabled(selected);
				maxLengthTextField.setEditable(selected);
				matchLinesLongerThanCheckBox.setEnabled(selected);
			}
		});

		final DecimalFormat decimalFormat = new DecimalFormat("#####");
		final NumberFormatter formatter = new NumberFormatter(decimalFormat);
		formatter.setMinimum(0);
		formatter.setValueClass(Integer.class);
		maxLengthTextField = new JFormattedTextField(formatter);
		maxLengthTextField.setColumns(5);

		JPopupMenu popup = new JPopupMenu("Defaults");
		maxLengthTextField.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				maxLengthTextField.setText(String.valueOf(DEFAULT_LINE_MAX_LENGTH));
				maxLengthTextField.setEnabled(true);
				maxLengthTextField.setEditable(true);
				limitLineMatchingByCheckBox.setSelected(DEFAULT_LIMIT_LINE_LENGTH);
				matchLinesLongerThanCheckBox.setEnabled(true);
			}
		});
	}

	private void setupSplitLineIntoChunk() {
		matchLinesLongerThanCheckBox = new JCheckBox("matchLinesLongerThanCheckBox");
		matchLinesLongerThanCheckBox.setToolTipText("Check this to keep on matching the text of a line longer than the defined limit. Keep in mind: The text will be matched chunk by chunk, so it might miss some links.");
		JPopupMenu popup = new JPopupMenu("Defaults");
		matchLinesLongerThanCheckBox.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				matchLinesLongerThanCheckBox.setSelected(DEFAULT_SPLIT_ON_LIMIT);
			}
		});
	}

	private void setupMatchURLs() {
		searchForURLsCheckBox = new JCheckBox();
		searchForURLsCheckBox.setToolTipText("Uncheck if you do not want URLs parsed from the console.");
		setupRestore(searchForURLsCheckBox, e -> searchForURLsCheckBox.setSelected(DEFAULT_SEARCH_URLS));
	}

	private void setupMatchFiles() {
		searchForFilesCheckBox = new JCheckBox();
		searchForFilesCheckBox.setToolTipText("Uncheck if you do not want file paths parsed from the console.");
		setupRestore(searchForFilesCheckBox, e -> searchForFilesCheckBox.setSelected(DEFAULT_SEARCH_URLS));
	}

	private void setupIgnorePattern() {
		ignorePatternCheckBox = new JCheckBox();
		ignorePatternTextField = new JTextField();
		bindCheckBoxTextField(ignorePatternCheckBox, ignorePatternTextField, DEFAULT_USE_IGNORE_PATTERN, DEFAULT_IGNORE_PATTERN_TEXT);
	}

	public void initIgnorePattern(boolean enabled, String text) {
		setupCheckBoxTextField(ignorePatternCheckBox, enabled, ignorePatternTextField, text);
	}

	private void setupFixChooseTargetFileCheckBox() {
		fixChooseTargetFileCheckBox = new JCheckBox("fixChooseTargetFileCheckBox");
		fixChooseTargetFileCheckBox.setToolTipText("Uncheck if this fix is not compatible with your newer version of IDE.");
		setupRestore(fixChooseTargetFileCheckBox, e -> fixChooseTargetFileCheckBox.setSelected(DEFAULT_FIX_CHOOSE_TARGET_FILE));
	}

	private void setupFileTypes() {
		fileTypesCheckBox = new JCheckBox();
		fileTypesCheckBox.setToolTipText("Fix some files still open in external programs, uncheck if you don't need it.");
		fileTypesTextField = new JTextField();
		fileTypesTextField.setToolTipText("Use , to separate types.");
		bindCheckBoxTextField(fileTypesCheckBox, fileTypesTextField, DEFAULT_USE_FILE_TYPES, DEFAULT_FILE_TYPES);
	}

	public void initFileTypes(boolean enabled, String text) {
		setupCheckBoxTextField(fileTypesCheckBox, enabled, fileTypesTextField, text);
	}
}
