package lect.chat.client;
import lect.chat.client.event.*;
import lect.chat.protocol.ChatCommandUtil;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.util.*;
@SuppressWarnings("serial")
public class ChatPanel extends JPanel implements MessageReceiver, ActionListener, ChatSocketListener {
   JTextField chatTextField;
   ChatTextPane chatDispArea;
   UserList userList;
   ConnectButton connectDisconnect;
   StatusBtn onOff;
   JButton whisper;
   ArrayList<ChatUser> list;
   private ArrayList<ChatUser> chatUsers = new ArrayList<>();

   // ui 추가 변수
   SaveBtn save; // 저장 버튼
   JButton init; // 초기화 버튼

   JLabel statusField; // 상태 표시 라벨
   OnList onList; // 상태 표시 리스트
   
   String ipAddress;
   String portStr;
   
   // 

   PrintWriter writer;
   ChatConnector connector;
   ChatONOFF chaton;
   StringBuilder msgBuilder = new StringBuilder();
   private JLabel titleLabel_1;
   private JScrollPane scrollPane_1;
   private JScrollPane scrollPane_2;

   public ChatPanel(ChatConnector c) {
      initUI();
      connector = c;


      chatTextField.addActionListener(this);
      connectDisconnect.addActionListener(this);
      whisper.addActionListener(this);
      onOff.addActionListener(this);
      init.addActionListener(this);
      save.addActionListener(this);
      // UI 생성
   }

   public void clearText() {
      chatDispArea.setText("");
   }

   private void initUI() {
      chatTextField = new JTextField();
      chatTextField.setBounds(2, 267, 295, 21); 
      chatDispArea = new ChatTextPane();
      userList = new UserList();
      onList = new OnList();
      connectDisconnect = new ConnectButton();
      connectDisconnect.setBounds(305, 266, 90, 23);
      whisper = new JButton("  👂  ");
      whisper.setBounds(397, 266, 90, 23);

      // ui 변수 선언
      statusField = new JLabel(" 버튼을 통해 현재 상태를 알려주세요");
      statusField.setBounds(2, 294, 284, 15);
      init = new JButton("   🔄   ");
      init.setBounds(306, 290, 90, 23);
      save = new SaveBtn(chatDispArea);
      onOff = new StatusBtn();
      onOff.setBounds(230, 290, 60, 23);
      save.setBounds(397, 290, 90, 23);

      chatTextField.setEnabled(false);
      chatDispArea.setEditable(false);
      whisper.setEnabled(false);
      save.setEnabled(false);
      init.setEnabled(false);
      statusField.setEnabled(false);
      onOff.setEnabled(false);
      setLayout(null);
      JLabel titleLabel = new JLabel("채팅방", JLabel.CENTER);
      titleLabel.setBounds(77, 2, 142, 15);
      add(titleLabel);
      titleLabel_1 = new JLabel("사용자 목록", JLabel.CENTER);
      titleLabel_1.setBounds(320, 2, 84, 15);
      add(titleLabel_1);
      JLabel titleLabel2 = new JLabel("상태", JLabel.CENTER);
      titleLabel2.setBounds(416, 2, 84, 15);
      add(titleLabel2);
      JScrollPane scrollPane = new JScrollPane(chatDispArea);
      scrollPane.setBounds(2, 20, 300, 245);
      add(scrollPane);
   
      scrollPane_1 = new JScrollPane(userList);
      scrollPane_1.setBounds(306, 20, 120, 245);
      add(scrollPane_1);   
      scrollPane_2 = new JScrollPane(onList);
      scrollPane_2.setBounds(430, 20, 60, 245);
      add(scrollPane_2);   
      add(chatTextField);
      add(connectDisconnect);
      add(whisper);
      add(statusField);
      add(init);
      add(save);
      add(onOff);

   }

   @Override
   public void messageArrived(String msg) {
      // 메세지 출력
      char command = ChatCommandUtil.getCommandType(msg);
      System.out.println(msg);
      msg = msg.replaceFirst("\\[{1}[a-z]\\]{1}", "");
      // 메세지의 첫글자 추출해 switch문에서 해당하는 명령 실행
      // 채팅 메세지 스타일 적용하게됨, 귓속말은 핑크, 퇴장알림은 파랑 등등 chatTextPane에 구현되어있음
      switch(command) {
         case ChatCommandUtil.NORMAL:
         case ChatCommandUtil.ENTER_ROOM:
         case ChatCommandUtil.WHISPER:
         case ChatCommandUtil.EXIT_ROOM:
         case ChatCommandUtil.MSG:
            chatDispArea.append(msg + "\n", command);
            break;
         case ChatCommandUtil.USER_LIST:
            displayUserList(msg);
            break;
         case ChatCommandUtil.CHANGE_STATUS:
            processChangeStatus(msg); // 상태변경
            break;
         case ChatCommandUtil.DUPLICATE_USER: // 동일한 사용자가 있는 경우
        	JOptionPane.showMessageDialog(null, "동일한 사용자가 존재합니다.", "에러", JOptionPane.ERROR_MESSAGE);
        	connector.disConnect(); // 소켓을 끊고
        	connector.connect(ipAddress, Integer.parseInt(portStr)); // 다시 채팅 입력을 시도
        	break;
         default:
            break;
         }
   }
   
   private void processChangeStatus(String msg) {
      String chatName = msg;
      ChatUser userToChangeStatus = getUserByChatName(chatName);
      if (userToChangeStatus != null) {
         if (userToChangeStatus.getStatus() == 0) {
            userToChangeStatus.setStatus(1);
         } else {
            userToChangeStatus.setStatus(0);
         }
         String msgToSend = userToChangeStatus.getName() + " 상태: " + Integer.toString(userToChangeStatus.getStatus());
         sendMessage(ChatCommandUtil.MSG, msgToSend);
         onList.addUserStatus(list);
      }
   }

   public ChatUser getUserByChatName(String chatName) {
      for (ChatUser user : chatUsers) {
         if (user.getName().equals(chatName)) {
            return user;
         }
      }
      return null;
   }
   
   @Override
   public void socketClosed() {
      //  소켓 닫히면 호출되어 UI이 비활성하고 연결 버튼으로 변경됨
      chatTextField.setEnabled(false);
      chatDispArea.setEnabled(false);
      whisper.setEnabled(false);
      userList.setEnabled(false);
      save.setEnabled(false);
      init.setEnabled(false);
      statusField.setEnabled(false);
      onOff.setEnabled(false);
      connectDisconnect.changeButtonStatus(ConnectButton.CMD_CONNECT);
   }  
   @Override
   public void socketConnected(Socket s) throws IOException {
      // 소켓 연결되면 출력스트림 생성 및 UI 활성화, 사용자 정보 초기화해 서버 전송
      writer = new PrintWriter(s.getOutputStream(), true);
      writer.println(createMessage(ChatCommandUtil.INIT_ALIAS, String.format("%s|%s", connector.getName(), connector.getId()) ));
      chatTextField.setEnabled(true);
      chatDispArea.setEnabled(true);
      whisper.setEnabled(true);
      userList.setEnabled(true);
      statusField.setEnabled(true);
      save.setEnabled(true);
      init.setEnabled(true);
      onOff.setEnabled(true);
   }  
   @Override
   public void actionPerformed(ActionEvent e) {
      // 메세지 입력, 연결 및 해제, 귓속말등 버튼 이벤트 클릭시 호출됨
      Object sourceObj = e.getSource();
      if(sourceObj == chatTextField) { // 엔터키 인식해 실행됨
         String msgToSend = chatTextField.getText();
         if(msgToSend.trim().equals("")) return;
         if(connector.socketAvailable()) {
            sendMessage(ChatCommandUtil.NORMAL, msgToSend);
         }
         chatTextField.setText(""); // 입력창 비우기
         // 일반 메세지 출력
      } else if(sourceObj == connectDisconnect) { // 연결 상태면 해제, 해제 상태면 연결 실행
    	 if(e.getActionCommand().equals(ConnectButton.CMD_CONNECT)) {
	       	ipAddress = JOptionPane.showInputDialog(this, "서버 IP 주소를 입력하세요:");
	        if (ipAddress == null) return; // 취소되었을 경우 종료  
	        portStr = JOptionPane.showInputDialog(this, "서버 PORT 를 입력하세요:");
	        if (portStr == null) return; // 취소되었을 경우 종료 
	        int port = Integer.parseInt(portStr); 
	        if (connector.connect(ipAddress, port)) {
	           connectDisconnect.changeButtonStatus(ConnectButton.CMD_DISCONNECT);
	        }
         }
         else {//when clicked Disconnect button
            connector.disConnect();
            connectDisconnect.changeButtonStatus(ConnectButton.CMD_CONNECT);
     	 }
      } else if(sourceObj == onOff) { // 자리비움 , 온라인 상태표시 실행
    	  sendMessage(ChatCommandUtil.CHANGE_STATUS, "changeStatus");
         chatTextField.setText("");
         if(e.getActionCommand().equals(StatusBtn.CMD_ONLINE)) {
            onOff.changeButton(StatusBtn.CMD_OFFLINE);
         } else {//when clicked Disconnect button
        	   onOff.changeButton(StatusBtn.CMD_ONLINE);
         }
      } else if (sourceObj == whisper) {//whisper button
         ChatUser userToWhisper = (ChatUser)userList.getSelectedValue();
         if(userToWhisper == null) {
            JOptionPane.showMessageDialog(this, "User to whisper to must be selected", "Whisper", JOptionPane.WARNING_MESSAGE);
            return;
         }
         String msgToSend = chatTextField.getText();
         if(msgToSend.trim().equals("")) return;
         sendMessage(ChatCommandUtil.WHISPER, String.format("%s|%s", userToWhisper.getId(), msgToSend));
         chatTextField.setText("");
         // 귓속말 버튼 클릭시 호출
      } else if (sourceObj == init){
         // 초기화
         String msgToSend = "reset";
         sendMessage(ChatCommandUtil.INITIALIZE, msgToSend);
         clearText();
      } else if (sourceObj == save){
         save.saveline();
      }
   }

   private void displayUserList(String users) {
      // 서버에서 사용자 목록 받아서 목록 업데이트 GroupManager에서 호출됨 
      //format should be like 'name1,id1,host1|name2,id2,host2|...'
      //System.out.println(users);
      String [] strUsers = users.split("\\|");
      String [] nameWithIdHost;
      list = new ArrayList<ChatUser>();
      for(String strUser : strUsers) {
         nameWithIdHost = strUser.split(",");
         if(connector.getId().equals(nameWithIdHost[1])) continue;
         list.add(new ChatUser(nameWithIdHost[0], nameWithIdHost[1], nameWithIdHost[2]));
      }
      chatUsers = list;
      userList.addNewChatUsers(list);
      onList.addUserStatus(list);
   }

   private void sendMessage(char command, String msg) {
      writer.println(createMessage(command, msg));
   }

   private String createMessage(char command, String msg) {
      msgBuilder.delete(0, msgBuilder.length());
      msgBuilder.append("[");
      msgBuilder.append(command);
      msgBuilder.append("]");
      msgBuilder.append(msg);
      return msgBuilder.toString();
   }
}