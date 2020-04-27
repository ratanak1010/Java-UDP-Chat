import java.util.*;

class WaitRoom {
	private static final int 최대방_수 = 10;
	private static final int 최대사용자_수 = 50;
	private static final int 사용중인_유저 = 3001;
	private static final int 서버포화 = 5552;
	private static final int 방포화 = 5554;
	private static final int 사용자수포화 = 5555;
	private static final int 틀린비밀번호 = 7575;

	private static Vector 유저벡터, 방벡터;
	private static Hashtable 유저해쉬, 방해쉬;

	private static int 사용자_수;
	private static int 방_수;

	static {
		유저벡터 = new Vector(최대사용자_수);
		방벡터 = new Vector(최대방_수);
		유저해쉬 = new Hashtable(최대사용자_수);
		방해쉬 = new Hashtable(최대방_수);
		사용자_수 = 0;
		방_수 = 0;
	}

	public WaitRoom() {
	}

	public synchronized int addUser(String id, ServerThread client) {
		if (사용자_수 == 최대사용자_수)
			return 서버포화;

		Enumeration ids = 유저벡터.elements();
		while (ids.hasMoreElements()) {
			String tempID = (String) ids.nextElement();
			if (tempID.equals(id))
				return 사용중인_유저;
		}
		Enumeration rooms = 방벡터.elements();
		while (rooms.hasMoreElements()) {
			ChatRoom tempRoom = (ChatRoom) rooms.nextElement();
			if (tempRoom.checkUserIDs(id))
				return 사용중인_유저;
		}

		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		client.server_thread_ID = id;
		client.server_thread_room_Number = 0;
		사용자_수++;

		return 0;
	}

	public synchronized void delUser(String id) {
		유저벡터.removeElement(id);
		유저해쉬.remove(id);
		사용자_수--;
	}

	public synchronized String getRooms() {
		StringBuffer room = new StringBuffer();
		String rooms;
		Integer roomNum;
		Enumeration enu = 방해쉬.keys();
		while (enu.hasMoreElements()) {
			roomNum = (Integer) enu.nextElement();
			ChatRoom tempRoom = (ChatRoom) 방해쉬.get(roomNum);
			room.append(String.valueOf(roomNum));
			room.append(" = ");
			room.append(tempRoom.toString());
			room.append("'");
		}
		try {
			rooms = new String(room);
			rooms = rooms.substring(0, rooms.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			return "empty";
		}
		return rooms;
	}

	public synchronized int addRoom(ChatRoom room) {
		if (방_수 == 최대방_수)
			return 방포화;

		방벡터.addElement(room);
		방해쉬.put(new Integer(ChatRoom.room_Number), room);
		방_수++;
		return 0;
	} // 대화방 추가 (최대 방 개수일 경우 방 포화 에러 출력)

	public synchronized String getUsers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Enumeration enu = 유저벡터.elements();
		while (enu.hasMoreElements()) {
			id.append(enu.nextElement());
			id.append("'");
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}

	public String getWaitRoomInfo() {
		StringBuffer roomInfo = new StringBuffer();
		roomInfo.append(getRooms());
		roomInfo.append(":");
		roomInfo.append(getUsers());
		return roomInfo.toString();
	}

	public synchronized int joinRoom(String id, ServerThread client, int room_Number, String password) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		if (room.locked()) {
			if (room.checkPassword(password)) {
				if (!room.addUser(id, client)) {
					return 사용자수포화;
				}
			} else {
				return 틀린비밀번호;
			}
		} else if (!room.addUser(id, client)) {
			return 사용자수포화;
		}
		유저벡터.removeElement(id);
		유저해쉬.remove(id);

		return 0;
	}

	public String getRoomInfo(int room_Number) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		return room.getUsers();
	}

	public synchronized boolean quitRoom(String id, int room_Number, ServerThread client) {
		boolean returnValue = false;
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		if (room.delUser(id)) {
			방벡터.removeElement(room);
			방해쉬.remove(roomNum);
			방_수--;
			returnValue = true;
		}
		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		return returnValue;
	}

	public synchronized Hashtable getClients(int room_Number) {
		if (room_Number == 0)
			return 유저해쉬;

		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		return room.getClients();
	}
}