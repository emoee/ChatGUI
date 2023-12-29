package lect.chat.client.p2p;
import java.io.*;
import java.net.*;
import java.util.Vector;

import lect.chat.client.p2p.event.FileProgressListener;

public class FileReceiver implements Runnable {
	Socket socket;
	InputStream in;
	static final File downloadedDir = new File("download");
	static Vector<FileProgressListener> listeners = new Vector<FileProgressListener>();
	public FileReceiver(Socket s) throws IOException {
		socket = s;
		in = socket.getInputStream();
	}
	public static void addFileProgressListener(FileProgressListener l) {
		listeners.add(l);
	}
	public void run() {
		int byteRead;
		String fileName;
		int startPacket, delimeter;
		long fileLength, totalBytes;
		OutputStream fileOut = null;
		File saveFile;
		int percent;
		try {
			DataInputStream dis  = new DataInputStream(in);
			while(socket.isConnected()) {
				startPacket = dis.readInt();
				fileName = dis.readUTF();
				delimeter = dis.readInt();
				if((startPacket != FileSender.START) || (delimeter != FileSender.DELIMETER)) {
					System.err.println("invalid START or DELIMETER packet received...");
					break;
					// 파일 전송 형식? 확인
				}
				fileLength = dis.readLong();
				if(!downloadedDir.exists()) {
					downloadedDir.mkdir();
					// 폴더 없으면 만들기
				}
				saveFile = new File(downloadedDir, fileName);
				if(saveFile.exists()) {
					saveFile = new File(downloadedDir, String.format("%s%s","dup_", fileName));
					// 파일 이미 존재하면 dup 문구 넣어서 만들기
				}
				for(FileProgressListener listener: listeners) {
					listener.progressWillStart();
					// 프로그레스바 만들어서 출력
				}
				fileOut = new BufferedOutputStream(new FileOutputStream(saveFile));
				// new?? get outputstream
				totalBytes = 0;
				while((byteRead = dis.read()) != -1) {
					totalBytes++;
					fileOut.write(byteRead);
					percent = (int)((totalBytes * 100.)/fileLength);
					for(FileProgressListener listener: listeners) {
						listener.fileProgressed(String.format("downloading %s %d%%", fileName, percent), percent);
					}
					if(fileLength == totalBytes) {
						break;
					}
					// 파일에 내용 쓰기
				}
				fileOut.close();
				if(dis.readInt() != FileSender.END) {
					System.err.println("invalid END Packet");
					break;
				}
			}
		} catch(IOException e) {
			try { if(fileOut != null) fileOut.close(); } catch(IOException ex) {}
			try { in.close(); } catch(IOException ex) {}
		} finally {
			for(FileProgressListener listener: listeners) {
				listener.progressFinished();
			}
		}
	}
}
