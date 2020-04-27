import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class CreateRoomDisplay extends JDialog implements ActionListener, ItemListener {
	private ClientThread client;
	private String roomName, str_password;
	private int roomMaxUser, lock;

	private JFrame main;
	private Container c;
	private JTextField tf;
	private JPanel personPanel;
	private JRadioButton person1, person2, person3, person4, llock, unllock;
	private JPasswordField password;
	private JButton ok, cancle;

	public CreateRoomDisplay(JFrame frame, ClientThread client) {
		super(frame, true);
		main = frame;
		setTitle("대화방 생성");
		this.client = client;
		lock = 0;
		roomMaxUser = 2;
		str_password = "0";

		c = getContentPane();
		c.setLayout(null);

		JLabel label;
		label = new JLabel("방제목");
		label.setBounds(10, 10, 100, 20);
		label.setForeground(Color.blue);
		c.add(label);

		tf = new JTextField();
		tf.setBounds(10, 30, 270, 20);
		c.add(tf);

		label = new JLabel("최대인원");
		label.setForeground(Color.blue);
		label.setBounds(10, 60, 100, 20);
		c.add(label);

		personPanel = new JPanel();
		personPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		ButtonGroup group = new ButtonGroup();
		person1 = new JRadioButton("2명");
		person1.setSelected(true);
		person1.addItemListener(this);
		group.add(person1);
		person2 = new JRadioButton("3명");
		person2.addItemListener(this);
		group.add(person2);
		person3 = new JRadioButton("4명");
		person3.addItemListener(this);
		group.add(person3);
		person4 = new JRadioButton("5명");
		person4.addItemListener(this);
		group.add(person4);
		personPanel.add(person1);
		personPanel.add(person2);
		personPanel.add(person3);
		personPanel.add(person4);
		personPanel.setBounds(10, 80, 280, 20);
		c.add(personPanel);

		label = new JLabel("공개여부");
		label.setForeground(Color.blue);
		label.setBounds(10, 110, 100, 20);
		c.add(label);

		personPanel = new JPanel();
		personPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		group = new ButtonGroup();
		unllock = new JRadioButton("공개");
		unllock.setSelected(true);
		unllock.addItemListener(this);
		group.add(unllock);
		llock = new JRadioButton("비공개");
		llock.addItemListener(this);
		group.add(llock);
		personPanel.add(unllock);
		personPanel.add(llock);
		personPanel.setBounds(10, 130, 280, 20);
		c.add(personPanel);

		label = new JLabel("비밀번호");
		label.setForeground(Color.blue);
		label.setBounds(10, 160, 100, 20);
		c.add(label);

		password = new JPasswordField();
		password.setBounds(10, 180, 150, 20);
		password.setEditable(false);
		c.add(password);

		ok = new JButton("확 인");
		ok.setForeground(Color.blue);
		ok.setBounds(75, 220, 70, 30);
		ok.addActionListener(this);
		c.add(ok);

		cancle = new JButton("취 소");
		cancle.setForeground(Color.blue);
		cancle.setBounds(155, 220, 70, 30);
		cancle.addActionListener(this);
		c.add(cancle);

		Dimension dim = getToolkit().getScreenSize();
		setSize(300, 300);
		setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);
		show();
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				tf.requestFocusInWindow();
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == unllock) {
			lock = 0;
			str_password = "0";
			password.setText("");
			password.setEditable(false);
		} else if (ie.getSource() == llock) {
			lock = 1;
			password.setEditable(true);
		} else if (ie.getSource() == person1) {
			roomMaxUser = 2;
		} else if (ie.getSource() == person2) {
			roomMaxUser = 3;
		} else if (ie.getSource() == person3) {
			roomMaxUser = 4;
		} else if (ie.getSource() == person4) {
			roomMaxUser = 5;
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == ok) {
			if (tf.getText().equals("")) {
				JOptionPane.showMessageDialog(main, "방제목을 입력하세요", "대화방 생성.", JOptionPane.ERROR_MESSAGE);
			} else {
				roomName = tf.getText();
				if (lock == 1) {
					str_password = password.getText();
				}
				if (lock == 1 && str_password.equals("")) {
					JOptionPane.showMessageDialog(main, "비밀번호를 입력하세요", "대화방 생성.", JOptionPane.ERROR_MESSAGE);
				} else {
					client.방생성요청(roomName, roomMaxUser, lock, str_password);
					dispose();
				}
			}
		} else {
			dispose();
		}
	}
}
