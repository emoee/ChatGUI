package lect.chat.client;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

public class OnList extends MyList{
	public OnList() {
		super();
		
	}
	
	public void addUserStatus(ArrayList <ChatUser> users) {
		// ������ �޾Ƽ� ��Ͽ� �ϳ��� �߰�
		DefaultListModel newModel = new DefaultListModel();
		for(ChatUser user: users) {
			newModel.addElement(user.status);
		}
		setModel(newModel);
	}
	
	public void addNewChatUsers(ArrayList <ChatUser> users) {
		// ������ �޾Ƽ� ��Ͽ� �ϳ��� �߰�
		DefaultListModel newModel = new DefaultListModel();
		for(ChatUser user: users) {
			newModel.addElement(user);
		}
		setModel(newModel);
	}
	
	public ChatUser getUserByChatName(String chatName) {
		for (int i = 0; i < getModel().getSize(); i++) {
	        ChatUser user = (ChatUser) getModel().getElementAt(i);
	        if (user.getName().equals(chatName)) {
	            return user;
	        }
	    }
	    return null;
	}
	
}
