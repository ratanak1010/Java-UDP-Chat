import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

class MessageBoxLess extends JDialog implements ActionListener {
	private Frame client;
	private Container c;

	public MessageBoxLess(JFrame parent, String title, String message) {
		super(parent, true);
		setTitle(title);
		c = getContentPane();
		c.setLayout(null);
		JLabel lbl = new JLabel(message);
		lbl.setFont(new Font("µ¸¿ò", Font.PLAIN, 12));
		lbl.setBounds(20, 10, 190, 20);
		c.add(lbl);

		JButton button = new JButton("O K");
		button.setBounds(60, 40, 70, 25);
		button.setFont(new Font("µ¸¿ò", Font.PLAIN, 12));
		button.addActionListener(this);
		button.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		c.add(button);

		Dimension dim = getToolkit().getScreenSize();
		setSize(200, 100);
		setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);
		show();
		client = parent;
	}

	public void actionPerformed(ActionEvent ae) {
		dispose();
		System.exit(0);
	}
}
