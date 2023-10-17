package awesome.console.config;

import com.intellij.openapi.util.text.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import org.jetbrains.annotations.NotNull;

public class AwesomeConsoleConfigForm implements AwesomeConsoleDefaults {
	public JPanel mainpanel;
	public JCheckBox debugModeCheckBox;
	public JCheckBox limitLineMatchingByCheckBox;
	public JFormattedTextField maxLengthTextField;
	public JCheckBox matchLinesLongerThanCheckBox;
	public JCheckBox searchForURLsCheckBox;
	public JCheckBox searchForFilesCheckBox;
	public JCheckBox searchForClassesCheckBox;
	public JCheckBox filePatternCheckBox;
	public JTextArea filePatternTextArea;
	public JLabel filePatternLabel;
	public JCheckBox ignorePatternCheckBox;
	public JTextField ignorePatternTextField;
	public JCheckBox fixChooseTargetFileCheckBox;
	public JCheckBox fileTypesCheckBox;
	public JTextField fileTypesTextField;

	private Map<JCheckBox, List<JComponent>> bindMap;

	private void createUIComponents() {
		bindMap = new HashMap<>();
		setupDebugMode();
		setupLineLimit();
		setupSplitLineIntoChunk();
		setupMatchURLs();
		setupMatchFiles();
		setupIgnorePattern();
		setupFixChooseTargetFileCheckBox();
		setupFileTypes();
	}

	private void setupRestore(@NotNull JComponent component, ActionListener listener) {
		final JPopupMenu popup = new JPopupMenu("Defaults");
		final JMenuItem item = popup.add("Restore defaults");
		item.setMnemonic(KeyEvent.VK_R);
		item.addActionListener(listener);
		component.setComponentPopupMenu(popup);
	}

	private void setupRestoreCheckBox(@NotNull JCheckBox checkBox, boolean defaultSelected) {
		setupRestore(checkBox, e -> setupCheckBox(checkBox, defaultSelected));
	}

	private void setupRestoreText(@NotNull JTextComponent textComponent, String defaultText) {
		setupRestore(textComponent, e -> textComponent.setText(defaultText));
	}

	private void bindCheckBoxAndComponents(@NotNull JCheckBox checkBox, @NotNull JComponent... components) {
		bindMap.computeIfAbsent(checkBox, __ -> new ArrayList<>()).addAll(List.of(components));
	}

	private void onCheckBoxChange(@NotNull JCheckBox checkBox) {
		boolean enabled = checkBox.isSelected();
		List<JComponent> components = bindMap.computeIfAbsent(checkBox, __ -> new ArrayList<>());
		for (JComponent component : components) {
			setComponentEnabled(component, enabled);
		}
		if (checkBox == searchForFilesCheckBox || checkBox == filePatternCheckBox) {
			enabled = searchForFilesCheckBox.isSelected() && filePatternCheckBox.isSelected();
			setComponentEnabled(filePatternTextArea, enabled);
		}
	}

	private void setComponentEnabled(@NotNull JComponent component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof JTextComponent) {
			((JTextComponent) component).setEditable(enabled);
		}
	}

	private void setupCheckBoxAndText(@NotNull JCheckBox checkBox, boolean selected, @NotNull JTextComponent textComponent, String text) {
		setupCheckBox(checkBox, selected);
		textComponent.setText(text);
	}

	private void setupCheckBox(@NotNull JCheckBox checkBox, boolean selected) {
		checkBox.setSelected(selected);
		onCheckBoxChange(checkBox);
	}

	private JCheckBox initCheckBox(boolean defaultSelected) {
		final JCheckBox checkBox = new JCheckBox();
		checkBox.addActionListener(e -> onCheckBoxChange(checkBox));
		setupRestoreCheckBox(checkBox, defaultSelected);
		return checkBox;
	}

	private JTextField initTextField(String defaultText) {
		final JTextField textField = new JTextField();
		setupRestoreText(textField, defaultText);
		return textField;
	}

	private JTextArea initTextArea(String defaultText) {
		final JTextArea textArea = new JTextArea();
		setupRestoreText(textArea, defaultText);
		return textArea;
	}

	private void setupDebugMode() {
		debugModeCheckBox = initCheckBox(DEFAULT_DEBUG_MODE);
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
		searchForURLsCheckBox = initCheckBox(DEFAULT_SEARCH_URLS);
		searchForURLsCheckBox.setToolTipText("Uncheck if you do not want URLs parsed from the console.");
	}

	private void setupMatchFiles() {
		searchForFilesCheckBox = initCheckBox(DEFAULT_SEARCH_FILES);
		searchForFilesCheckBox.setToolTipText("Uncheck if you do not want file paths parsed from the console.");
		searchForClassesCheckBox = initCheckBox(DEFAULT_SEARCH_CLASSES);
		searchForClassesCheckBox.setToolTipText("Uncheck if you do not want classes parsed from the console.");

		filePatternCheckBox = initCheckBox(DEFAULT_USE_FILE_PATTERN);
		filePatternCheckBox.setToolTipText("Check this to custom File Pattern. (experimental)");
		filePatternTextArea = initTextArea(DEFAULT_FILE_PATTERN_TEXT);
		filePatternTextArea.setLineWrap(true);
		final String groupExample = FILE_PATTERN_REQUIRED_GROUPS[0];
		filePatternLabel = new JLabel(String.format(
				"      * Required regex group names: [%s], where %s,%s1...%s%d all correspond to the group %s.",
				StringUtil.join(FILE_PATTERN_REQUIRED_GROUPS, ", "),
				groupExample, groupExample, groupExample, DEFAULT_GROUP_RETRIES, groupExample
		));

		bindCheckBoxAndComponents(searchForFilesCheckBox, searchForClassesCheckBox, filePatternCheckBox);
	}

	public void initMatchFiles(boolean enableFiles, boolean enableClasses, boolean enableFilePattern, String filePattern) {
		setupCheckBox(searchForFilesCheckBox, enableFiles);
		setupCheckBox(searchForClassesCheckBox, enableClasses);
		setupCheckBoxAndText(filePatternCheckBox, enableFilePattern, filePatternTextArea, filePattern);
	}

	private void setupIgnorePattern() {
		ignorePatternCheckBox = initCheckBox(DEFAULT_USE_IGNORE_PATTERN);
		ignorePatternTextField = initTextField(DEFAULT_IGNORE_PATTERN_TEXT);
		bindCheckBoxAndComponents(ignorePatternCheckBox, ignorePatternTextField);
	}

	public void initIgnorePattern(boolean enabled, String text) {
		setupCheckBoxAndText(ignorePatternCheckBox, enabled, ignorePatternTextField, text);
	}

	private void setupFixChooseTargetFileCheckBox() {
		fixChooseTargetFileCheckBox = initCheckBox(DEFAULT_FIX_CHOOSE_TARGET_FILE);
		fixChooseTargetFileCheckBox.setToolTipText("Uncheck if this fix is not compatible with your newer version of IDE.");
	}

	private void setupFileTypes() {
		fileTypesCheckBox = initCheckBox(DEFAULT_USE_FILE_TYPES);
		fileTypesCheckBox.setToolTipText("Fix some files still open in external programs, uncheck if you don't need it.");
		fileTypesTextField = initTextField(DEFAULT_FILE_TYPES);
		fileTypesTextField.setToolTipText("Use , to separate types.");
		bindCheckBoxAndComponents(fileTypesCheckBox, fileTypesTextField);
	}

	public void initFileTypes(boolean enabled, String text) {
		setupCheckBoxAndText(fileTypesCheckBox, enabled, fileTypesTextField, text);
	}
}
