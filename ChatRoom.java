import java.util.*;

class ChatRoom
{
  public static int room_Number = 0;
  private Vector 유저벡터;
  private Hashtable 유저해쉬;
  private String Chatroom;
  private int 최대사용자수;
  private int 사용자수;
  private boolean lock;
  private String password;
  private String admin;

  public ChatRoom(String Chatroom, int 최대사용자수,
                  boolean lock, String password, String admin){
	  room_Number++;
    this.Chatroom = Chatroom;
    this.최대사용자수 = 최대사용자수;
    this.사용자수 = 0;
    this.lock = lock;
    this.password = password;
    this.admin = admin;
    this.유저벡터 = new Vector(최대사용자수);
    this.유저해쉬 = new Hashtable(최대사용자수);
  }

  public boolean addUser(String id, ServerThread client){
    if (사용자수 == 최대사용자수){
      return false;
    }
    유저벡터.addElement(id);
    유저해쉬.put(id, client);
    사용자수++;
    return true;
  }

  public boolean checkPassword(String passwd){
    return password.equals(passwd);
  }

  public boolean checkUserIDs(String id){
    Enumeration ids = 유저벡터.elements();
    while(ids.hasMoreElements()){
      String tempId = (String) ids.nextElement();
      if (tempId.equals(id)) return true;
    }
    return false;
  }

  public boolean locked(){
    return lock;
  }

  public boolean delUser(String id){
	  유저벡터.removeElement(id);
	  유저해쉬.remove(id);
	  사용자수--;
    return 유저벡터.isEmpty();
  }
      
  public synchronized String getUsers(){
    StringBuffer id = new StringBuffer();
    String ids;
    Enumeration enu = 유저벡터.elements();
    while(enu.hasMoreElements()){
      id.append(enu.nextElement());
      id.append("'");
    }
    try{
      ids = new String(id);
      ids = ids.substring(0, ids.length() - 1);
    }catch(StringIndexOutOfBoundsException e){
      return "";
    }
    return ids;
  }

  public Hashtable getClients(){
    return 유저해쉬;
  }

  public String toString(){
    StringBuffer room = new StringBuffer();
    room.append(Chatroom);
    room.append(" = ");
    room.append(String.valueOf(사용자수));
    room.append(" = ");
    room.append(String.valueOf(최대사용자수));
    room.append(" = ");
    if (lock) {
      room.append("비공개");
    } else {
      room.append("공개");
    }
    room.append(" = ");
    room.append(admin);
    return room.toString();
  }

  public static synchronized int getroom_Number(){
    return room_Number;
  }
}
