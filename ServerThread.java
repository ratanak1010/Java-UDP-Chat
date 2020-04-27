import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
	private Socket server_thread_sock;
	private DataInputStream server_thread_in;
	private DataOutputStream server_thread_out;
	private StringBuffer server_thread_buffer;
	private WaitRoom server_thread_waitRoom;
	public String server_thread_ID;
	public int server_thread_room_Number;
	private static final int WAITROOM = 0;

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

	public ServerThread(Socket sock) {
		try {
			server_thread_sock = sock;
			server_thread_in = new DataInputStream(sock.getInputStream());
			server_thread_out = new DataOutputStream(sock.getOutputStream());
			server_thread_buffer = new StringBuffer(2048);
			server_thread_waitRoom = new WaitRoom();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void sendErrCode(int message, int errCode) throws IOException {
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(message);
		server_thread_buffer.append(":");
		server_thread_buffer.append(errCode);
		send(server_thread_buffer.toString());
	}

	private void 대기자정보수정() throws IOException {
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(대기자정보수정);
		server_thread_buffer.append(":");
		server_thread_buffer.append(server_thread_waitRoom.getWaitRoomInfo());
		broadcast(server_thread_buffer.toString(), WAITROOM);
	}

	private void 대기자수정() throws IOException {
		String ids = server_thread_waitRoom.getUsers();
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(대기자수정);
		server_thread_buffer.append(":");
		server_thread_buffer.append(ids);
		broadcast(server_thread_buffer.toString(), WAITROOM);
	}

	private void 방사용자수정(int room_Number, String id, int code) throws IOException {
		String ids = server_thread_waitRoom.getRoomInfo(room_Number);
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(방사용자수정);
		server_thread_buffer.append(":");
		server_thread_buffer.append(id);
		server_thread_buffer.append(":");
		server_thread_buffer.append(code);
		server_thread_buffer.append(":");
		server_thread_buffer.append(ids);
		broadcast(server_thread_buffer.toString(), room_Number);
	}

	private void send(String sendData) throws IOException {
		synchronized (server_thread_out) {

			System.out.println(sendData);

			server_thread_out.writeUTF(sendData);
			server_thread_out.flush();
		}
	}

	private synchronized void broadcast(String sendData, int room_Number) throws IOException {
		ServerThread client;
		Hashtable clients = server_thread_waitRoom.getClients(room_Number);
		Enumeration enu = clients.keys();
		while (enu.hasMoreElements()) {
			client = (ServerThread) clients.get(enu.nextElement());
			client.send(sendData);
		}
	}

	public void run() {
		try {
			while (true) {
				String recvData = server_thread_in.readUTF();

				System.out.println(recvData);

				StringTokenizer st = new StringTokenizer(recvData, ":");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case 로그인요청: {
					server_thread_room_Number = WAITROOM;
					int result;
					server_thread_ID = st.nextToken();
					result = server_thread_waitRoom.addUser(server_thread_ID, this);
					server_thread_buffer.setLength(0);
					if (result == 0) {
						server_thread_buffer.append(로그인수락);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_waitRoom.getRooms());
						send(server_thread_buffer.toString());
						대기자수정();
						System.out.println(server_thread_ID + "의 연결요청 승인");
					} else {
						sendErrCode(로그인거절, result);
					}
					break;
				}
				case 방생성요청: {
					String id, roomName, password;
					int roomMaxUser, result;
					boolean lock;

					id = st.nextToken();
					String roomInfo = st.nextToken();
					StringTokenizer room = new StringTokenizer(roomInfo, "'");
					roomName = room.nextToken();
					roomMaxUser = Integer.parseInt(room.nextToken());
					lock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
					password = room.nextToken();

					ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser, lock, password, id);
					result = server_thread_waitRoom.addRoom(chatRoom);
					if (result == 0) {
						server_thread_room_Number = ChatRoom.getroom_Number();
						boolean temp = chatRoom.addUser(server_thread_ID, this);
						server_thread_waitRoom.delUser(server_thread_ID);

						server_thread_buffer.setLength(0);
						server_thread_buffer.append(방생성수락);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						send(server_thread_buffer.toString());
						대기자정보수정();
						방사용자수정(server_thread_room_Number, id, 1);
					} else {
						sendErrCode(방생성거절, result);
					}
					break;
				}
				case 방입장요청: {
					String id, password;
					int room_Number, result;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());
					try {
						password = st.nextToken();
					} catch (NoSuchElementException e) {
						password = "0";
					}
					result = server_thread_waitRoom.joinRoom(id, this, room_Number, password);

					if (result == 0) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(방입장수락);
						server_thread_buffer.append(":");
						server_thread_buffer.append(room_Number);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_room_Number = room_Number;
						send(server_thread_buffer.toString());
						방사용자수정(room_Number, id, 1);
						대기자정보수정();
					} else {
						sendErrCode(방입장거절, result);
					}
					break;
				}
				case 방퇴장요청: {
					String id;
					int room_Number;
					boolean updateWaitInfo;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());

					updateWaitInfo = server_thread_waitRoom.quitRoom(id, room_Number, this);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(방퇴장수락);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					send(server_thread_buffer.toString());
					server_thread_room_Number = WAITROOM;

					if (updateWaitInfo) {
						대기자정보수정();
					} else {
						대기자정보수정();
						방사용자수정(room_Number, id, 0);
					}
					break;
				}
				case 로그아웃요청: {
					String id = st.nextToken();
					server_thread_waitRoom.delUser(id);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(로그아웃수락);
					send(server_thread_buffer.toString());
					대기자수정();
					release();
					break;
				}
				case 송신자요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(송신자수락);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					server_thread_buffer.append(":");
					server_thread_buffer.append(server_thread_room_Number);
					server_thread_buffer.append(":");
					try {
						String data = st.nextToken();
						server_thread_buffer.append(data);
					} catch (NoSuchElementException e) {
					}

					broadcast(server_thread_buffer.toString(), room_Number);
					break;
				}
				case 수신자요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(수신자수락);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						server_thread_buffer.append(":");
						try {
							String data = st.nextToken();
							server_thread_buffer.append(data);
						} catch (NoSuchElementException e) {
						}
						client.send(server_thread_buffer.toString());
						send(server_thread_buffer.toString());
						break;
					} else {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(수신자거절);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						send(server_thread_buffer.toString());
						break;
					}
				}
				case 파일전송요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(파일전송요청);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						client.send(server_thread_buffer.toString());
						break;
					} else {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(파일전송거절);
						server_thread_buffer.append(":");
						server_thread_buffer.append(사용자없음);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						send(server_thread_buffer.toString());
						break;
					}
				}
				case 파일전송거절: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(파일전송거절);
					server_thread_buffer.append(":");
					server_thread_buffer.append(거부됨);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);

					client.send(server_thread_buffer.toString());
					break;
				}
				case 파일전송수락: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					String hostaddr = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(파일전송수락);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					server_thread_buffer.append(":");
					server_thread_buffer.append(hostaddr);

					client.send(server_thread_buffer.toString());
					break;
				}
				case 강제요청: {
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					boolean updateWaitInfo;
					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);
					updateWaitInfo = server_thread_waitRoom.quitRoom(idTo, room_Number, client);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(강제요청수락);
					client.send(server_thread_buffer.toString());
					client.server_thread_room_Number = 0;

					if (updateWaitInfo) {
						대기자정보수정();
					} else {
						대기자정보수정();
						방사용자수정(room_Number, idTo, 2);
					}
					break;
				}
				}
				Thread.sleep(100);
			}
		} catch (NullPointerException e) {
		} catch (InterruptedException e) {
			System.out.println(e);

			if (server_thread_room_Number == 0) {
				server_thread_waitRoom.delUser(server_thread_ID);
			} else {
				boolean temp = server_thread_waitRoom.quitRoom(server_thread_ID, server_thread_room_Number, this);
				server_thread_waitRoom.delUser(server_thread_ID);
			}
			release();
		} catch (IOException e) {
			System.out.println(e);

			if (server_thread_room_Number == 0) {
				server_thread_waitRoom.delUser(server_thread_ID);
			} else {
				boolean temp = server_thread_waitRoom.quitRoom(server_thread_ID, server_thread_room_Number, this);
				server_thread_waitRoom.delUser(server_thread_ID);
			}
			release();
		}
	}

	public void release() {
		try {
			if (server_thread_in != null)
				server_thread_in.close();
		} catch (IOException e1) {
		} finally {
			server_thread_in = null;
		}
		try {
			if (server_thread_out != null)
				server_thread_out.close();
		} catch (IOException e1) {
		} finally {
			server_thread_out = null;
		}
		try {
			if (server_thread_sock != null)
				server_thread_sock.close();
		} catch (IOException e1) {
		} finally {
			server_thread_sock = null;
		}

		if (server_thread_ID != null) {
			System.out.println(server_thread_ID + "와 연결을 종료합니다.");
			server_thread_ID = null;
		}
	}
}
