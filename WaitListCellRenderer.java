import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

class WaitListCellRenderer extends JLabel implements ListCellRenderer {
	protected static Border w_noFocusBorder;
	protected FontMetrics w_fm = null;
	protected Insets w_insets = new Insets(0, 0, 0, 0);
	protected int w_defaultTab = 50;
	protected int[] w_tabs = null;

	private int count;

	public WaitListCellRenderer() {
		super();
		w_noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		setOpaque(true);
		setBorder(w_noFocusBorder);
		count = 0;
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setText(value.toString());
		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

		setFont(list.getFont());
		setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : w_noFocusBorder);

		return this;
	}

	public void setDefaultTab(int defaultTab) {
		w_defaultTab = defaultTab;
	}

	public int getDefaultTab() {
		return w_defaultTab;
	}

	public void setTabs(int[] tabs) {
		w_tabs = tabs;
	}

	public int[] getTabs() {
		return w_tabs;
	}

	public int getTab(int index) {
		if (w_tabs == null)
			return w_defaultTab * index;
		int len = w_tabs.length;
		if (index >= 0 && index < len)
			return w_tabs[index];
		return w_tabs[len - 1] + w_defaultTab * (index - len - 1);
	}

	public void paint(Graphics g) {
		w_fm = g.getFontMetrics();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());

		g.setColor(getForeground());
		g.setFont(getFont());
		w_insets = getInsets();
		int x = w_insets.left;
		int y = w_insets.top + w_fm.getAscent();

		StringTokenizer st = new StringTokenizer(getText(), "=");
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			g.drawString(temp, x, y);
			x += w_fm.stringWidth(temp);
			if (!st.hasMoreTokens())
				break;
			int index = 0;
			while (x >= getTab(index))
				index++;
			x = getTab(index);
		}
	}
}
