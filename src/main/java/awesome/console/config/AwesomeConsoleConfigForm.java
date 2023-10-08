package awesome.console.config;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class AwesomeConsoleConfigForm {
	public static final boolean DEFAULT_DEBUG_MODE = false;
	private static final boolean DEFAULT_SPLIT_ON_LIMIT = false;
	private static final boolean DEFAULT_LIMIT_LINE_LENGTH = true;
	private static final int DEFAULT_LINE_MAX_LENGTH = 1024;
	private static final boolean DEFAULT_SEARCH_URLS = true;
	public static final boolean DEFAULT_USE_IGNORE_PATTERN = true;
	public static final String DEFAULT_IGNORE_PATTERN_TEXT = "^[.\\\\/]+$";

	public JPanel mainpanel;
	public JCheckBox debugModeCheckBox;
	public JCheckBox limitLineMatchingByCheckBox;
	public JFormattedTextField maxLengthTextField;
	public JCheckBox matchLinesLongerThanCheckBox;
	public JCheckBox searchForURLsFileCheckBox;
	public JCheckBox ignorePatternCheckBox;
	public JTextField ignorePatternTextField;

	private void createUIComponents() {
		setupDebugMode();
		setupLineLimit();
		setupSplitLineIntoChunk();
		setupMatchURLs();
		setupIgnorePattern();
	}

	private void setupRestore(JComponent component, ActionListener listener) {
		final JPopupMenu popup = new JPopupMenu("Defaults");
		
		final JMenuItem item = popup.add("Restore defaults");
		item.setMnemonic(KeyEvent.VK_R);
		item.addActionListener(listener);

		component.setComponentPopupMenu(popup);
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
		searchForURLsFileCheckBox = new JCheckBox("searchForURLsFileCheckBox");
		searchForURLsFileCheckBox.setToolTipText("Uncheck if you do not want URLs parsed from the console.");
		JPopupMenu popup = new JPopupMenu("Defaults");
		searchForURLsFileCheckBox.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				searchForURLsFileCheckBox.setSelected(DEFAULT_SEARCH_URLS);
			}
		});
	}

	private void setupIgnorePattern() {
		ignorePatternCheckBox = new JCheckBox();
		ignorePatternCheckBox.addActionListener(e -> {
			final boolean enabled = ignorePatternCheckBox.isSelected();
			ignorePatternTextField.setEnabled(enabled);
			ignorePatternTextField.setEditable(enabled);
		});

		ignorePatternTextField = new JTextField();
		setupRestore(ignorePatternTextField, e -> initIgnorePattern(DEFAULT_USE_IGNORE_PATTERN, DEFAULT_IGNORE_PATTERN_TEXT));
	}

	public void initIgnorePattern(boolean enabled, String text) {
		ignorePatternCheckBox.setSelected(enabled);
		ignorePatternTextField.setText(text);
		ignorePatternTextField.setEnabled(enabled);
		ignorePatternTextField.setEditable(enabled);
	}
}
