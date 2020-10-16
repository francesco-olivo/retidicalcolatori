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
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		try {

			//TODO: ciclo per prendere tutti i file
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
			DataOutputStream outFile = null;
			// ricezione file: caso di errore
			if (nomeFile == null) {
				clientSocket.close();
				return;
			} else { // controllo esistenza file
				File curFile = new File(nomeFile);

				//scrive true se il file non esiste e il client può procedere, false altrimenti
				Boolean esito = Boolean.valueOf(!curFile.exists());
				outSock.writeUTF(esito.toString());

				outFile = new DataOutputStream(new FileOutputStream(nomeFile));
				FileUtility.trasferisci_a_byte_file_binario(inSock, outFile);


			}
		} catch (Exception e) {
			System.exit(3);
		}
	} // run
} // PutFileServerThread
