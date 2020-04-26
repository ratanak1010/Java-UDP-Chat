import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread
{
  private Socket severthread_sock;
  private DataInputStream severthread_in;
  private DataOutputStream severthread_out;
  private StringBuffer severthread_buffer;
  private WaitRoom severthread_waitRoom;
  public String severthread_ID;
  public int severthread_room_Number;
  private static final int WAITROOM = 0;

  private static final int 로그인요청 = 1001;
  private static final int 방생성요청 = 1011;
  private static final int 방입장요청 = 1021;
  private static final int 방퇴장요청 = 1031;
  private static final int 로그아웃요청 = 1041;
  private static final int 송신자요청 = 1051;
  private static final int 수신자요청 = 1052;
  private static final int 코스요청 = 1053;
  private static final int 파일전송요청 = 1061;

  private static final int 로그인수락 = 2001;
  private static final int 로그인거절 = 2002;
  private static final int 방생성수락 = 2011;
  private static final int 방생성거절 = 2012;
  private static final int 방입장수락 = 2021;
  private static final int 방입장거절 = 2022;
  private static final int 방퇴장수락 = 2031;
  private static final int 로그아웃수락 = 2041;
  private static final int 송신자수락 = 2051;
  private static final int 수신자수락 = 2052;
  private static final int 수신자거절 = 2053;
  private static final int 코스요청수락 = 2054;
  private static final int 파일전송수락 = 2061;
  private static final int 파일전송거절 = 2062;
  private static final int 대기자수정 = 2003;
  private static final int 대기자정보수정 = 2013;
  private static final int 방사용자수정 = 2023;

  private static final int 사용중인_유저 = 3001;
  private static final int ERR_SERVERFULL = 3002;
  private static final int 방포화 = 3011;
  private static final int 사용자수포화 = 3021;
  private static final int 틀린비밀번호 = 3022;
  private static final int 거부됨 = 3031;
  private static final int 사용자없음 = 3032;

  public ServerThread(Socket sock){
    try{
      severthread_sock = sock;
      severthread_in = new DataInputStream(sock.getInputStream());
      severthread_out = new DataOutputStream(sock.getOutputStream());
      severthread_buffer = new StringBuffer(2048);
      severthread_waitRoom = new WaitRoom();
    }catch(IOException e){
      System.out.println(e);
    }
  }

  private void sendErrCode(int message, int errCode) throws IOException{
    severthread_buffer.setLength(0);
    severthread_buffer.append(message);
    severthread_buffer.append(":");
    severthread_buffer.append(errCode);
    send(severthread_buffer.toString());
  }

  private void modifyWaitRoom() throws IOException{
    severthread_buffer.setLength(0);
    severthread_buffer.append(대기자정보수정);
    severthread_buffer.append(":");
    severthread_buffer.append(severthread_waitRoom.getWaitRoomInfo());
    broadcast(severthread_buffer.toString(), WAITROOM);
  }  
    
  private void modifyWaitUser() throws IOException{
    String ids = severthread_waitRoom.getUsers();
    severthread_buffer.setLength(0);
    severthread_buffer.append(대기자수정);
    severthread_buffer.append(":");
    severthread_buffer.append(ids);
    broadcast(severthread_buffer.toString(), WAITROOM);
  }

  private void modifyRoomUser(int room_Number, String id, int code) throws IOException{
    String ids = severthread_waitRoom.getRoomInfo(room_Number);
    severthread_buffer.setLength(0);
    severthread_buffer.append(방사용자수정);
    severthread_buffer.append(":");
    severthread_buffer.append(id);
    severthread_buffer.append(":");
    severthread_buffer.append(code);
    severthread_buffer.append(":");
    severthread_buffer.append(ids);
    broadcast(severthread_buffer.toString(), room_Number);
  }

  private void send(String sendData) throws IOException{
    synchronized(severthread_out){

      System.out.println(sendData);

      severthread_out.writeUTF(sendData);
      severthread_out.flush();
    }
  }

  private synchronized void broadcast(String sendData, int room_Number) throws IOException{
    ServerThread client;
    Hashtable clients = severthread_waitRoom.getClients(room_Number);
    Enumeration enu = clients.keys();
    while(enu.hasMoreElements()){
      client = (ServerThread) clients.get(enu.nextElement());
      client.send(sendData);
    }
  }
    
  public void run(){
    try{
      while(true){
        String recvData = severthread_in.readUTF();

        System.out.println(recvData);

        StringTokenizer st = new StringTokenizer(recvData, ":");
        int command = Integer.parseInt(st.nextToken());
        switch(command){
          case 로그인요청 : {
            severthread_room_Number = WAITROOM;
            int result;
            severthread_ID = st.nextToken();
            result = severthread_waitRoom.addUser(severthread_ID, this);
            severthread_buffer.setLength(0);
            if(result == 0){
              severthread_buffer.append(로그인수락);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_waitRoom.getRooms());
              send(severthread_buffer.toString());
              modifyWaitUser();
              System.out.println(severthread_ID + "의 연결요청 승인");
            } else {
              sendErrCode(로그인거절, result);
            }
            break;
          }
          case 방생성요청 : {
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

            ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser,
            		lock, password, id);
            result = severthread_waitRoom.addRoom(chatRoom);
            if (result == 0) {
              severthread_room_Number = ChatRoom.getroom_Number();
              boolean temp = chatRoom.addUser(severthread_ID, this);
              severthread_waitRoom.delUser(severthread_ID);

              severthread_buffer.setLength(0);
              severthread_buffer.append(방생성수락);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              send(severthread_buffer.toString());
              modifyWaitRoom();
              modifyRoomUser(severthread_room_Number, id, 1);
            } else {
              sendErrCode(방생성거절, result);
            }
            break;
          }
          case 방입장요청 : {
            String id, password;
            int room_Number, result;
            id = st.nextToken();
            room_Number = Integer.parseInt(st.nextToken());
            try{
              password = st.nextToken();
            }catch(NoSuchElementException e){
              password = "0";
            }
            result = severthread_waitRoom.joinRoom(id, this, room_Number, password);

            if (result == 0){
              severthread_buffer.setLength(0);
              severthread_buffer.append(방입장수락);
              severthread_buffer.append(":");
              severthread_buffer.append(room_Number);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_room_Number = room_Number;
              send(severthread_buffer.toString());
              modifyRoomUser(room_Number, id, 1);
              modifyWaitRoom();
            } else {
              sendErrCode(방입장거절, result);
            }
            break;
          }
          case 방퇴장요청 : {
            String id;
            int room_Number;
            boolean updateWaitInfo;
            id = st.nextToken();
            room_Number = Integer.parseInt(st.nextToken());

            updateWaitInfo = severthread_waitRoom.quitRoom(id, room_Number, this);

            severthread_buffer.setLength(0);
            severthread_buffer.append(방퇴장수락);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            send(severthread_buffer.toString());
            severthread_room_Number = WAITROOM;

            if (updateWaitInfo) {
              modifyWaitRoom();
            } else {
              modifyWaitRoom();
              modifyRoomUser(room_Number, id, 0);
            }
            break;
          }
          case 로그아웃요청 : {
            String id = st.nextToken();
            severthread_waitRoom.delUser(id);

            severthread_buffer.setLength(0);
            severthread_buffer.append(로그아웃수락);
            send(severthread_buffer.toString());
            modifyWaitUser();
            release();
            break;
          }
          case 송신자요청 : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());

            severthread_buffer.setLength(0);
            severthread_buffer.append(송신자수락);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            severthread_buffer.append(":");
            severthread_buffer.append(severthread_room_Number);
            severthread_buffer.append(":");
            try{
              String data = st.nextToken();
              severthread_buffer.append(data);
            }catch(NoSuchElementException e){}

            broadcast(severthread_buffer.toString(), room_Number);
            break;
          }
          case 수신자요청 : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken(); 
            
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            if ((client = (ServerThread) room.get(idTo)) != null){            
              severthread_buffer.setLength(0);
              severthread_buffer.append(수신자수락);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              severthread_buffer.append(":");
              try{
                String data = st.nextToken();
                severthread_buffer.append(data);
              }catch(NoSuchElementException e){}
              client.send(severthread_buffer.toString());
              send(severthread_buffer.toString());
              break;
            } else {
              severthread_buffer.setLength(0);
              severthread_buffer.append(수신자거절);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              send(severthread_buffer.toString());
              break;
            }
          }
          case 파일전송요청 : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken(); 
            
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            if ((client = (ServerThread) room.get(idTo)) != null){
              severthread_buffer.setLength(0);
              severthread_buffer.append(파일전송요청);
              severthread_buffer.append(":");
              severthread_buffer.append(id);
              severthread_buffer.append(":");
              severthread_buffer.append(severthread_room_Number);
              client.send(severthread_buffer.toString());
              break;
            } else {
              severthread_buffer.setLength(0);
              severthread_buffer.append(파일전송거절);
              severthread_buffer.append(":");
              severthread_buffer.append(사용자없음);
              severthread_buffer.append(":");
              severthread_buffer.append(idTo);
              send(severthread_buffer.toString());
              break;
            }
          }
          case 파일전송거절 : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();

            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);

            severthread_buffer.setLength(0);
            severthread_buffer.append(파일전송거절);
            severthread_buffer.append(":");
            severthread_buffer.append(거부됨);
            severthread_buffer.append(":");
            severthread_buffer.append(id);

            client.send(severthread_buffer.toString());
            break;
          }
          case 파일전송수락 : {
            String id = st.nextToken();
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();
            String hostaddr = st.nextToken();

            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);

            severthread_buffer.setLength(0);
            severthread_buffer.append(파일전송수락);
            severthread_buffer.append(":");
            severthread_buffer.append(id);
            severthread_buffer.append(":");
            severthread_buffer.append(hostaddr);

            client.send(severthread_buffer.toString());
            break;
          }
          case 코스요청 : {
            int room_Number = Integer.parseInt(st.nextToken());
            String idTo = st.nextToken();
            boolean updateWaitInfo;
            Hashtable room = severthread_waitRoom.getClients(room_Number);
            ServerThread client = null;
            client = (ServerThread) room.get(idTo);
            updateWaitInfo = severthread_waitRoom.quitRoom(idTo, room_Number, client);

            severthread_buffer.setLength(0);
            severthread_buffer.append(코스요청수락);
            client.send(severthread_buffer.toString());
            client.severthread_room_Number = 0;

            if (updateWaitInfo) {
              modifyWaitRoom();
            } else {
              modifyWaitRoom();
              modifyRoomUser(room_Number, idTo, 2);
            }
            break;
          }
        }
        Thread.sleep(100);
      }
    }catch(NullPointerException e){
    }catch(InterruptedException e){
      System.out.println(e);

      if(severthread_room_Number == 0){
        severthread_waitRoom.delUser(severthread_ID);
      } else {
        boolean temp = severthread_waitRoom.quitRoom(severthread_ID, severthread_room_Number, this);
        severthread_waitRoom.delUser(severthread_ID);
      } 
      release();
    }catch(IOException e){
      System.out.println(e);

      if(severthread_room_Number == 0){
        severthread_waitRoom.delUser(severthread_ID);
      } else {
        boolean temp = severthread_waitRoom.quitRoom(severthread_ID, severthread_room_Number, this);
        severthread_waitRoom.delUser(severthread_ID);
      } 
      release();
    }
  }

  public void release(){
    try{
      if(severthread_in != null) severthread_in.close();
    }catch(IOException e1){
    }finally{
      severthread_in = null;
    }
    try{
      if(severthread_out != null) severthread_out.close();
    }catch(IOException e1){
    }finally{
      severthread_out = null;
    }
    try{
      if(severthread_sock != null) severthread_sock.close();
    }catch(IOException e1){
    }finally{
      severthread_sock = null;
    }

    if(severthread_ID != null){
      System.out.println(severthread_ID + "와 연결을 종료합니다.");
      severthread_ID = null;
    }
  }
}
            
