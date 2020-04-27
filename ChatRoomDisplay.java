import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

class ChatRoomDisplay extends JFrame implements ActionListener, KeyListener, ListSelectionListener, ChangeListener {
	private ClientThread chat_room_thread;
	private String idTo;
	private boolean isSelected;
	public boolean isAdmin;

	private JLabel roomer;
	public JList roomerInfo;
	private JButton 강제퇴장, sendWord, sendFile, quitRoom;
	private Font font;
	private JViewport view;
	private JScrollPane jspane;
	public JTextArea messages;
	public JTextField message;

	public ChatRoomDisplay(ClientThread thread) {
		super("대화방");

		chat_room_thread = thread;
		isSelected = false;
		isAdmin = false;
		font = new Font("돋움", Font.PLAIN, 12);

		Container c = getContentPane();
		c.setLayout(null);

		JPanel p = new JPanel();
		p.setLayout(null);
		p.setBounds(425, 10, 140, 175);
		p.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "참여자"));

		roomerInfo = new JList();
		roomerInfo.setFont(font);
		JScrollPane jspane2 = new JScrollPane(roomerInfo);
		roomerInfo.addListSelectionListener(this);
		jspane2.setBounds(15, 25, 110, 135);
		p.add(jspane2);

		c.add(p);

		p = new JPanel();
		p.setLayout(null);
		p.setBounds(10, 10, 410, 340);
		p.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "채팅창"));

		view = new JViewport();
		messages = new JTextArea();
		messages.setFont(font);
		messages.setEditable(false);
		view.add(messages);
		view.addChangeListener(this);
		jspane = new JScrollPane(view);
		jspane.setBounds(15, 25, 380, 270);
		p.add(jspane);

		message = new JTextField();
		message.setFont(font);
		message.addKeyListener(this);
		message.setBounds(15, 305, 380, 20);
		message.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		p.add(message);

		c.add(p);

		강제퇴장 = new JButton("강 퇴 ");
		강제퇴장.setFont(font);
		강제퇴장.addActionListener(this);
		강제퇴장.setBounds(445, 195, 100, 30);
		강제퇴장.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		c.add(강제퇴장);

		sendFile = new JButton("파 일 전 송");
		sendFile.setFont(font);
		sendFile.addActionListener(this);
		sendFile.setBounds(445, 275, 100, 30);
		sendFile.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		c.add(sendFile);

		quitRoom = new JButton("나 가 기");
		quitRoom.setFont(font);
		quitRoom.addActionListener(this);
		quitRoom.setBounds(445, 315, 100, 30);
		quitRoom.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		c.add(quitRoom);

		Dimension dim = getToolkit().getScreenSize();
		setSize(580, 400);
		setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);
		show();

		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				message.requestFocusInWindow();
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				chat_room_thread.방퇴장요청();
			}
		});
	}

	public void resetComponents() {
		messages.setText("");
		message.setText("");
		message.requestFocusInWindow();
	}

	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
			String words = message.getText();
			String data;
			String idTo;
			if (words.startsWith("/w")) {
				StringTokenizer st = new StringTokenizer(words, " ");
				String command = st.nextToken();
				idTo = st.nextToken();
				data = st.nextToken();
				chat_room_thread.수신자요청(data, idTo);
				message.setText("");
			} else {
				chat_room_thread.송신자요청(words);
				message.requestFocusInWindow();
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		isSelected = true;
		idTo = String.valueOf(((JList) e.getSource()).getSelectedValue());
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == 강제퇴장) {
			if (!isAdmin) {
				JOptionPane.showMessageDialog(this, "권한이 없습니다.", "강제퇴장", JOptionPane.ERROR_MESSAGE);
			} else if (!isSelected) {
				JOptionPane.showMessageDialog(this, "강제 퇴장 시킬 닉네임 : ", "강제퇴장", JOptionPane.ERROR_MESSAGE);
			} else {
				chat_room_thread.강제퇴장요청(idTo);
				isSelected = false;
			}
		} else if (ae.getSource() == quitRoom) {
			chat_room_thread.방퇴장요청();
		} else if (ae.getSource() == sendFile) {
			String idTo;
			if ((idTo = JOptionPane.showInputDialog("상대방 닉네임 : ")) != null) {
				chat_room_thread.파일전송요청(idTo);
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		jspane.getVerticalScrollBar().setValue((jspane.getVerticalScrollBar().getValue() + 20));
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
