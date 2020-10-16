import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private final static String USAGE = "Usage: ./Client HOST PORT MIN_FILE_SIZE(kb)";

	public static void main(String[] args) {
		InetAddress addr = null;
		int port = -1;
		int min_file_size = -1;
		try {
			if (args.length == 3) {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				min_file_size = Integer.parseInt(args[2]);
			} else {
				System.out.println(USAGE);
				System.exit(-1);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println(USAGE);
			System.exit(-1);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println(USAGE);
			System.exit(-1);
		}

		FileInputStream inputFile = null;
		DataInputStream inputSocket = null;
		DataOutputStream outputSocket = null;

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String request_dir = null;
		System.out.println("Inserisci la directory");
		try {
			request_dir = stdIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File dir = new File(request_dir);
		//Path dir = Path.of(request_dir);

		if (!dir.isDirectory()) {
			System.out.println("La directory '" + request_dir + "' non esiste.");
			System.exit(-1);
		}

		try (Socket socket = new Socket(addr, port)) { // creazione socket
			socket.setSoTimeout(30000);
			inputSocket = new DataInputStream(socket.getInputStream());
			outputSocket = new DataOutputStream(socket.getOutputStream());

			//TODO: rendere la lista array
			File[] files = dir.listFiles();
			/*
			List<File> files = null;
			// try-with-resources per chiudere lo stream
			try (Stream<Path> stream = Files.walk(dir)) {
				stream.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
			} catch (IOException e) {
				e.printStackTrace();
			}
			 */

			for (File f : files) {
				if(f.length() > min_file_size) {
					try {
						// Utilizziamo il carattere ':' come separatore perché è un char vietato dal
						// file system come nome file
						outputSocket.writeUTF(f.getName() + ":" + f.length());
						String response = inputSocket.readUTF();

						// il server restituisce "true" se è il client è autorizzato a procedere
						if (Boolean.parseBoolean(response)) {
							// trasferisci il file
							FileInputStream fis = new FileInputStream(f);
							FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(fis), outputSocket);
							// close del file
							fis.close();
						} else {
							System.out.println(f.getName() + " già presente sul file system del server");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			//chiusura socket
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
