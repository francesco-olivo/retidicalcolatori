import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		Socket socket = null;
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

		Path dir = Path.of(request_dir);

		if (!Files.exists(dir)) {
			System.out.println("La directory '" + request_dir + "' non esiste.");
			System.exit(-1);
		}

		try { // creazione socket
			socket = new Socket(addr, port);
			socket.setSoTimeout(30000);
			inputSocket = new DataInputStream(socket.getInputStream());
			outputSocket = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<File> files = null;
		//try-with-resources per chiudere lo stream
		try (Stream<Path> stream = Files.walk(dir)) {
			stream.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> requests = new ArrayList<>();
		for (File f : files) {
			requests.add(f.getName() + "," + f.length() + ",");
		}

	}
}
