import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientThread extends Thread {
	private WaitRoomDisplay client_threadwaitRoom;
	private ChatRoomDisplay client_threadchatRoom;
	private Socket client_threadsock;
	private DataInputStream client_threadin;
	private DataOutputStream client_threadout;
	private StringBuffer client_threadbuffer;
	private Thread thisThread;
	private String client_threadloginID;
	private int client_threadroom_Number;
	private static MessageBox msgBox, loginbox, fileTransBox;

	private static final int 로그인요청 = 9000;
	private static final int 로그인수락 = 9001;
	private static final int 로그인거절 = 9002;
	private static final int 방생성요청 = 8000;
	private static final int 방생성수락 = 8001;
	private static final int 방생성거절 = 8002;
	private static final int 방입장요청 = 7000;
	private static final int 방입장수락 = 7001;
	private static final int 방입장거절 = 7002;
	private static final int 방퇴장요청 = 6000;
	private static final int 방퇴장수락 = 6001;
	private static final int 로그아웃요청 = 0000;
	private static final int 로그아웃수락 = 0001;
	private static final int 송신자요청 = 5000;
	private static final int 송신자수락 = 5001;
	private static final int 수신자요청 = 4000;
	private static final int 수신자수락 = 4001;
	private static final int 수신자거절 = 4002;
	private static final int 강제요청 = 1111;
	private static final int 강제요청수락 = 1112;
	private static final int 파일전송요청 = 7777;
	private static final int 파일전송수락 = 7778;
	private static final int 파일전송거절 = 7779;

	private static final int 대기자수정 = 2003;
	private static final int 대기자정보수정 = 2013;
	private static final int 방사용자수정 = 2023;

	private static final int 사용중인_유저 = 3001;
	private static final int 서버포화 = 5552;
	private static final int 방포화 = 5554;
	private static final int 사용자수포화 = 5555;
	private static final int 틀린비밀번호 = 7575;
	private static final int 거부됨 = 6666;
	private static final int 사용자없음 = 4444;

	public ClientThread() {
		client_threadwaitRoom = new WaitRoomDisplay(this);
		client_threadchatRoom = null;
		try {
			client_threadsock = new Socket(InetAddress.getLocalHost(), 2777);
			client_threadin = new DataInputStream(client_threadsock.getInputStream());
			client_threadout = new DataOutputStream(client_threadsock.getOutputStream());
			client_threadbuffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_threadwaitRoom, "연결에러", "서버에 접속할 수 없습니다.");
			msgout.show();
		}
	}

	public ClientThread(String hostaddr) {
		client_threadwaitRoom = new WaitRoomDisplay(this);
		client_threadchatRoom = null;
		try {
			client_threadsock = new Socket(hostaddr, 2777);
			client_threadin = new DataInputStream(client_threadsock.getInputStream());
			client_threadout = new DataOutputStream(client_threadsock.getOutputStream());
			client_threadbuffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_threadwaitRoom, "연결에러", "서버에 접속할 수 없습니다.");
			msgout.show();
		}
	}

	public void run() {
		try {
			Thread currThread = Thread.currentThread();
			while (currThread == thisThread) {
				String recvData = client_threadin.readUTF();
				StringTokenizer st = new StringTokenizer(recvData, ":");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case 로그인수락: {
					loginbox.dispose();
					client_threadroom_Number = 0;
					try {
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
						Vector roomInfo = new Vector();
						while (st1.hasMoreTokens()) {
							String temp = st1.nextToken();
							if (!temp.equals("empty")) {
								roomInfo.addElement(temp);
							}
						}
						client_threadwaitRoom.roomInfo.setListData(roomInfo);
						client_threadwaitRoom.message.requestFocusInWindow();
					} catch (NoSuchElementException e) {
						client_threadwaitRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 로그인거절: {
					String id;
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 사용중인_유저) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_threadwaitRoom, "중복된 사용자가 있습니다.", "로그인",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getLoginID();
						로그인요청(id);
					} else if (errCode == 서버포화) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_threadwaitRoom, "대화방이 가득찼습니다.", "로그인",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getLoginID();
						로그인요청(id);
					}
					break;
				}
				case 대기자수정: {
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_threadwaitRoom.waiterInfo.setListData(user);
					client_threadwaitRoom.message.requestFocusInWindow();
					break;
				}
				case 방생성수락: {
					client_threadroom_Number = Integer.parseInt(st.nextToken());
					client_threadwaitRoom.hide();
					if (client_threadchatRoom == null) {
						client_threadchatRoom = new ChatRoomDisplay(this);
						client_threadchatRoom.isAdmin = true;
					} else {
						client_threadchatRoom.show();
						client_threadchatRoom.isAdmin = true;
						client_threadchatRoom.resetComponents();
					}
					break;
				}
				case 방생성거절: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 방포화) {
						msgBox = new MessageBox(client_threadwaitRoom, "대화방개설", "대화방생성불가");
						msgBox.show();
					}
					break;
				}
				case 대기자정보수정: {
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "'");

					Vector rooms = new Vector();
					Vector users = new Vector();
					while (st1.hasMoreTokens()) {
						String temp = st1.nextToken();
						if (!temp.equals("empty")) {
							rooms.addElement(temp);
						}
					}
					client_threadwaitRoom.roomInfo.setListData(rooms);

					while (st2.hasMoreTokens()) {
						users.addElement(st2.nextToken());
					}

					client_threadwaitRoom.waiterInfo.setListData(users);
					client_threadwaitRoom.message.requestFocusInWindow();

					break;
				}
				case 방입장수락: {
					client_threadroom_Number = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					client_threadwaitRoom.hide();
					if (client_threadchatRoom == null) {
						client_threadchatRoom = new ChatRoomDisplay(this);
					} else {
						client_threadchatRoom.show();
						client_threadchatRoom.resetComponents();
					}
					break;
				}
				case 방입장거절: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 사용자수포화) {
						msgBox = new MessageBox(client_threadwaitRoom, "대화방입장", "대화방이 가득 찼습니다.");
						msgBox.show();
					} else if (errCode == 틀린비밀번호) {
						msgBox = new MessageBox(client_threadwaitRoom, "대화방입장", "비밀번호를 틀렸습니다.");
						msgBox.show();
					}
					break;
				}
				case 방사용자수정: {
					String id = st.nextToken();
					int code = Integer.parseInt(st.nextToken());

					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_threadchatRoom.roomerInfo.setListData(user);
					if (code == 1) {
						client_threadchatRoom.messages.append("### " + id + "님이 입장하셨습니다. ###\n");
					} else if (code == 2) {
						client_threadchatRoom.messages.append("### " + id + "님이 강제퇴장 되었습니다. ###\n");
					} else {
						client_threadchatRoom.messages.append("### " + id + "님이 퇴장하셨습니다. ###\n");
					}
					client_threadchatRoom.message.requestFocusInWindow();
					break;
				}
				case 방퇴장수락: {
					String id = st.nextToken();
					if (client_threadchatRoom.isAdmin)
						client_threadchatRoom.isAdmin = false;
					client_threadchatRoom.hide();
					client_threadwaitRoom.show();
					client_threadwaitRoom.resetComponents();
					client_threadroom_Number = 0;
					break;
				}
				case 로그아웃수락: {
					client_threadwaitRoom.dispose();
					if (client_threadchatRoom != null) {
						client_threadchatRoom.dispose();
					}
					release();
					break;
				}
				case 송신자수락: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							client_threadwaitRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_threadloginID)) {
								client_threadwaitRoom.message.setText("");
								client_threadwaitRoom.message.requestFocusInWindow();
							}
							client_threadwaitRoom.message.requestFocusInWindow();
						} else {
							client_threadchatRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_threadloginID)) {
								client_threadchatRoom.message.setText("");
							}
							client_threadchatRoom.message.requestFocusInWindow();
						}

					} catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_threadwaitRoom.message.requestFocusInWindow();
						else
							client_threadchatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 수신자수락: {
					String id = st.nextToken();
					String idTo = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							if (id.equals(client_threadloginID)) {
								client_threadwaitRoom.message.setText("");
								client_threadwaitRoom.message.requestFocusInWindow();
							}
						}
						}catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_threadwaitRoom.message.requestFocusInWindow();
						else
							client_threadchatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 파일전송요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String message = id + "로 부터 파일을 받으시겠습니까";
					int value = JOptionPane.showConfirmDialog(client_threadchatRoom, message, "파일수신",
							JOptionPane.YES_NO_OPTION);
					if (value == 1) {
						try {
							client_threadbuffer.setLength(0);
							client_threadbuffer.append(파일전송거절);
							client_threadbuffer.append(":");
							client_threadbuffer.append(client_threadloginID);
							client_threadbuffer.append(":");
							client_threadbuffer.append(room_Number);
							client_threadbuffer.append(":");
							client_threadbuffer.append(id);
							send(client_threadbuffer.toString());
						} catch (IOException e) {
							System.out.println(e);
						}
					} else {
						StringTokenizer addr = new StringTokenizer(InetAddress.getLocalHost().toString(), "/");
						String hostname = "";
						String hostaddr = "";

						hostname = addr.nextToken();
						try {
							hostaddr = addr.nextToken();
						} catch (NoSuchElementException err) {
							hostaddr = hostname;
						}

						try {
							client_threadbuffer.setLength(0);
							client_threadbuffer.append(파일전송수락);
							client_threadbuffer.append(":");
							client_threadbuffer.append(client_threadloginID);
							client_threadbuffer.append(":");
							client_threadbuffer.append(room_Number);
							client_threadbuffer.append(":");
							client_threadbuffer.append(id);
							client_threadbuffer.append(":");
							client_threadbuffer.append(hostaddr);
							send(client_threadbuffer.toString());
						} catch (IOException e) {
							System.out.println(e);
						}
						// 파일 수신 서버실행.
						new ReciveFile();
					}
					break;
				}
				case 파일전송거절: {
					int code = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					fileTransBox.dispose();

					if (code == 거부됨) {
						String message = id + "님이 파일수신을 거부하였습니다.";
						JOptionPane.showMessageDialog(client_threadchatRoom, message, "파일전송",
								JOptionPane.ERROR_MESSAGE);
						break;
					} else if (code == 사용자없음) {
						String message = id + "님은 이 방에 존재하지 않습니다.";
						JOptionPane.showMessageDialog(client_threadchatRoom, message, "파일전송",
								JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
				case 파일전송수락: {
					String id = st.nextToken();
					String addr = st.nextToken();

					fileTransBox.dispose();
					// 파일 송신 클라이언트 실행.
					new SendFile(addr);
					break;
				}
				case 강제요청수락: {
					client_threadchatRoom.hide();
					client_threadwaitRoom.show();
					client_threadwaitRoom.resetComponents();
					client_threadroom_Number = 0;
					client_threadwaitRoom.messages.append("### 방장에 의해 강제퇴장 되었습니다. ###\n");
					break;
				}
				}
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			System.out.println(e);
			release();
		} catch (IOException e) {
			System.out.println(e);
			release();
		}
	}

	public void 로그인요청(String id) {
		try {
			loginbox = new MessageBox(client_threadwaitRoom, "로그인", "서버에 로그인 중입니다.");
			loginbox.show();
			client_threadloginID = id;
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(로그인요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(id);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 방생성요청(String roomName, int roomMaxUser, int lock, String password) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(방생성요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(roomName);
			client_threadbuffer.append("'");
			client_threadbuffer.append(roomMaxUser);
			client_threadbuffer.append("'");
			client_threadbuffer.append(lock);
			client_threadbuffer.append("'");
			client_threadbuffer.append(password);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 방입장요청(int room_Number, String password) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(방입장요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(room_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(password);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 방퇴장요청() {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(방퇴장요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 로그아웃요청() {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(로그아웃요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 송신자요청(String data) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(송신자요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(data);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 수신자요청(String data, String idTo) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(수신자요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			client_threadbuffer.append(":");
			client_threadbuffer.append(data);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 강제퇴장요청(String idTo) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(강제요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void 파일전송요청(String idTo) {
		fileTransBox = new MessageBox(client_threadchatRoom, "파일전송", "상대방의 승인을 기다립니다.");
		fileTransBox.show();
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(파일전송요청);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void send(String sendData) throws IOException {
		client_threadout.writeUTF(sendData);
		client_threadout.flush();
	}

	public void release() {
		if (thisThread != null) {
			thisThread = null;
		}
		try {
			if (client_threadout != null) {
				client_threadout.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadout = null;
		}
		try {
			if (client_threadin != null) {
				client_threadin.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadin = null;
		}
		try {
			if (client_threadsock != null) {
				client_threadsock.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadsock = null;
		}
		System.exit(0);
	}
}
