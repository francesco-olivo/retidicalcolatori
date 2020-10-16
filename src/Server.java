import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	private final static int PORT = 4000;
	private final static int TIMEOUT = 30 * 1000;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			serverSocket.setReuseAddress(true);
		} catch (Exception e) {
			System.exit(1);
		}

		try {
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(TIMEOUT);
				} catch (Exception e) {
					continue;
				}
				try {
					new PutFileServerThread(clientSocket).start();
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			System.exit(2);
		}
	}

}



/*
 * 
 * CLASSE THREAD
 * 
 * 
 */

class PutFileServerThread extends Thread {
	private Socket clientSocket = null;

	public PutFileServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		DataInputStream inSock;
		DataOutputStream outSock = null;
		try {
			String nomeFile = null;
			try // creazione stream
			{
				inSock = new DataInputStream(clientSocket.getInputStream());
				outSock = new DataOutputStream(clientSocket.getOutputStream());
				nomeFile = inSock.readUTF();
			} catch (SocketTimeoutException te) {
			} catch (IOException ioe) {
			} catch (Exception e) {
			}
			FileOutputStream outFile = null;
			String esito;
			// ricezione file: caso di errore
			if (nomeFile == null) {
				clientSocket.close();
				return;
			} else { // controllo esistenza file
				File curFile = new File(nomeFile);
				if (curFile.exists()) {
					try // distruggo il vecchio file
					{
						esito = "File sovrascritto";
						curFile.delete();
					} catch (Exception e) {
						return;
					}
				} else
					esito = "Creato nuovo file";
				outFile = new FileOutputStream(nomeFile);
			}
			try {
				// FileUtility.trasferisci_a_byte_file_binario(inSock, new
				// DataOutputStream(outFile));
				outFile.close(); // chiusura file e socket
				// NOTA: è il figlio che fa la close!
				clientSocket.shutdownInput();
				outSock.writeUTF(esito + ", file salvato lato server");
				clientSocket.shutdownOutput();
			} catch (SocketTimeoutException te) {
			} catch (Exception e) {
			}
		} catch (Exception e) {
			System.exit(3);
		}
	} // run
} // PutFileServerThread
