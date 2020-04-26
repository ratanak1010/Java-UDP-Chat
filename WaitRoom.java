import java.util.*;

class WaitRoom {
	private static final int 최대방_수 = 10;
	private static final int 최대사용자_수 = 50;
	private static final int 사용중인_유저 = 3001;
	private static final int 서버포화 = 3002;
	private static final int 방포화 = 3011;
	private static final int 사용자수포화 = 3021;
	private static final int 틀린비밀번호 = 3022;

	private static Vector 유저벡터, roomVector;
	private static Hashtable 유저해쉬, roomHash;

	private static int userCount;
	private static int roomCount;

	static {
		유저벡터 = new Vector(최대사용자_수);
		roomVector = new Vector(최대방_수);
		유저해쉬 = new Hashtable(최대사용자_수);
		roomHash = new Hashtable(최대방_수);
		userCount = 0;
		roomCount = 0;
	}

	public WaitRoom() {
	}

	public synchronized int addUser(String id, ServerThread client) {
		if (userCount == 최대사용자_수)
			return 서버포화;

		Enumeration ids = 유저벡터.elements();
		while (ids.hasMoreElements()) {
			String tempID = (String) ids.nextElement();
			if (tempID.equals(id))
				return 사용중인_유저;
		}
		Enumeration rooms = roomVector.elements();
		while (rooms.hasMoreElements()) {
			ChatRoom tempRoom = (ChatRoom) rooms.nextElement();
			if (tempRoom.checkUserIDs(id))
				return 사용중인_유저;
		}

		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		client.severthread_ID = id;
		client.severthread_room_Number = 0;
		userCount++;

		return 0;
	}

	public synchronized void delUser(String id) {
		유저벡터.removeElement(id);
		유저해쉬.remove(id);
		userCount--;
	}

	public synchronized String getRooms() {
		StringBuffer room = new StringBuffer();
		String rooms;
		Integer roomNum;
		Enumeration enu = roomHash.keys();
		while (enu.hasMoreElements()) {
			roomNum = (Integer) enu.nextElement();
			ChatRoom tempRoom = (ChatRoom) roomHash.get(roomNum);
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

	public synchronized int addRoom(ChatRoom room) {
		if (roomCount == 최대방_수)
			return 방포화;

		roomVector.addElement(room);
		roomHash.put(new Integer(ChatRoom.room_Number), room);
		roomCount++;
		return 0;
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
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
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
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getUsers();
	}

	public synchronized boolean quitRoom(String id, int room_Number, ServerThread client) {
		boolean returnValue = false;
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		if (room.delUser(id)) {
			roomVector.removeElement(room);
			roomHash.remove(roomNum);
			roomCount--;
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
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getClients();
	}
}
