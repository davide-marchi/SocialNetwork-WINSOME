// ------------------- Marchi Davide 602476 -------------------

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import com.fasterxml.jackson.databind.ObjectMapper;

// Classe con metodo main che permette di comunicare con il server winsome
public class ClientMain {

	// Socketchannel
	private static SocketChannel client;

	// Attributi per capire se si è effettuato un login o si desidera uscire
	private static String loggedAs;
	private static boolean exit = false;

	// Attributi per consentire la comunicazione tramite RMI
	private static RmiServerInterface serverObject;
	private static NotifyEventImplementation clientRmi;
	private static NotifyEventInterface clientInterface;

	// Attributi per consentire la comunicazione tramite MULTICAST
	private static Thread threadMulticast;
	private static MulticastSocket ms;
	private static InetSocketAddress group;
	private static NetworkInterface netIf;
	private static ClientConfiguration configuration;
	private static String multicastAddress;
	private static int multicastPort = 0;

	// Metodo main
	public static void main(String[] args) {

		// Lettura della configurazione del client
		configuration = readConfiguration("ConfigClient.json");

		// ------------------- RMI -------------------
		Remote remoteObject;
		try {

			// Istanziazione del serverObj, ovvero l'oggetto remoto del server
			Registry r = LocateRegistry.getRegistry(configuration.getRegport());
			remoteObject = r.lookup(configuration.getRmiobjname());
			serverObject = (RmiServerInterface) remoteObject;

			// Esportazione dell'oggetto remoto locale per permettere al server la CALLBACK
			clientRmi = new NotifyEventImplementation();
			clientInterface = (NotifyEventInterface) UnicastRemoteObject.exportObject(clientRmi, 0);
		} catch (Exception e) {

			// Stampa e terminazione del processo in caso di errrore
			System.out.println("FATAL ERROR Unable to prapare RMI " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("------------------- CLIENT READY -------------------");
		Scanner keyboard = new Scanner(System.in);

		try {

			// Ciclo fino a che non viene richiesta la chiusura del client tramite "exit"
			while (!exit) {

				// Lettura della richiesta fatta dall'utente
				Request request = fillRequestFromKeyboard(keyboard);

				// Controlo se la richiesta devve essere inoltrata tramite connessione TCP
				if (request != null) {

					// Controllo se il canale di comunicazione TCP non è aperto e ho una login
					if (client == null && request.getRequestType().equals("login")) {

						// Apertura della connessione TCP col server
						client = SocketChannel
								.open(new InetSocketAddress(configuration.getServer(), configuration.getTcpport()));
						// System.out.println(" -> Opened TCP connection");
					}

					// Controllo se sono riuscito ad instaurare la connessoine TCP
					if (client != null) {

						// Invio del messsaggio e ricesione della risposta
						sendRequest(client, request);
						Response response = receiveResponse(client);
						handleResponse(response);

					} else {

						System.out.println("ERROR TCP connection with server not opened");
					}
				}
			}

			// Chiusura della scanner e rimozione dell'oggetto RMI esportato
			keyboard.close();
			UnicastRemoteObject.unexportObject(clientRmi, true);

		} catch (IOException e) {
			System.out.println("FATAL ERROR");
			System.exit(-1);
		}
	}

	// Metodo per la lettura del file di configurazione
	public static ClientConfiguration readConfiguration(String fileName) {

		ObjectMapper mapper = new ObjectMapper();
		try {

			// Lettura della struttura tramite ObjectMapper
			return mapper.readValue(Paths.get(fileName).toFile(), ClientConfiguration.class);
		} catch (IOException e1) {
			System.out.println("errore fatale nel parsing del file config. esco");
			e1.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	// Metodo per la lettura della richiesta da tastiera
	public static Request fillRequestFromKeyboard(Scanner keyboard) {

		try {

			// Lettura della riga scritta dall0utente
			String line = keyboard.nextLine();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String command = tokenizer.nextToken();

			// Switch a seconda della prima parola letta
			switch (command) {

			case "help":
				System.out.println("Possible commands:\n" + " - register <username> <password> <tags>\n"
						+ " - login <username> <password>\n" + " - logout\n" + " - list users\n" + " - list follwers\n"
						+ " - list following\n" + " - follow <username>\n" + " - unfollow <username>\n" + " - blog\n"
						+ " - post \"<title>\" \"<content>\"\n" + " - show feed\n" + " - show post <id>\n"
						+ " - delete <idPost>\n" + " - rewin <idPost>\n" + " - rate <idPost> <vote>\n"
						+ " - comment <idPost> <comment>\n" + " - wallet\n" + " - wallet btc");
				break;

			case "register":
				if (loggedAs != null) {
					System.out.println("ERROR You can't register other accounts while being logged in");
					break;
				}

				// Lettura di username e password
				String username = tokenizer.nextToken();
				String password = tokenizer.nextToken();
				ArrayList<String> tags = new ArrayList<String>();

				// Lettura dei tag degli interessi
				while (tokenizer.hasMoreTokens()) {
					tags.add(new String(tokenizer.nextToken()).toLowerCase());
				}
				if (tags.size() == 0 || tags.size() > 5) {
					System.out.println("ERROR Insert 1 to 5 tags");
					break;
				}

				// Chiamata alla funzione per effettura la registrazione tramite RMI
				register(username, password, tags);
				break;

			case "exit":
				exit = true;
				if (loggedAs != null) {
					return new Request("logout");
				}
				break;

			case "login":
				if (loggedAs != null) {
					System.out.println("ERROR You are already logged in");
					break;
				} else {

					// Lettura di username e password e ritorno della richiesta
					String loginusername = tokenizer.nextToken();
					String loginpassword = tokenizer.nextToken();
					loggedAs = loginusername;
					return new Request("login", loginusername, loginpassword);
				}

			case "logout":
				if (loggedAs == null) {
					System.out.println("ERROR You are not logged in yet");
					break;
				} else {
					return new Request("logout");
				}

			case "list":

				// Lettura della seconda parola per capire che richiesta inviare
				switch (tokenizer.nextToken()) {
				case "users":
					return new Request("listUsers");
				case "followers":
					if (loggedAs != null) {

						// Chiamata alla funzione per rispondere sena interpellare il server
						listFollowers();
					} else {
						System.out.println("ERROR You are not logged in");
					}
					break;
				case "following":
					return new Request("listFollowing");
				default:
					System.out.println("ERROR \"list users\" or \"list followers\" or \"list following\"?");
					break;
				}
				break;

			case "follow":
				return new Request("follow", tokenizer.nextToken());

			case "unfollow":
				return new Request("unfollow", tokenizer.nextToken());

			case "post":

				// Lettura dell'intero post scritta
				String str = "";
				while (tokenizer.hasMoreTokens())
					str += tokenizer.nextToken() + " ";

				// Separazione di titolo e contenuto utilizzando le virgolette
				String[] tokens = str.trim().split("\""); // tokens -> {""; "title"; " "; "content"}
				int numberOfToken = tokens.length;
				if (numberOfToken == 4) {
					return new Request("post", tokens[1], tokens[3]);
				} else {
					System.out.println("ERROR Insert \"title\" \"content\" using quotation marks");
				}
				break;

			case "blog":
				return new Request("viewBlog");

			case "show":

				// Lettura della seconda parola per capire che richiesta inviare
				switch (tokenizer.nextToken()) {
				case "feed":
					return new Request("showFeed");
				case "post":
					return new Request("showPost", Integer.parseInt(tokenizer.nextToken()));
				default:
					System.out.println("ERROR \"show feed\" or \"show post\"?");
					break;
				}
				break;

			case "rewin":
				return new Request("rewin", Integer.parseInt(tokenizer.nextToken()));

			case "delete":
				return new Request("delete", Integer.parseInt(tokenizer.nextToken()));

			case "rate":
				return new Request("ratePost", Integer.parseInt(tokenizer.nextToken()),
						Integer.parseInt(tokenizer.nextToken()));

			// Lettura della seconda parola per capire che richiesta inviare
			case "wallet":
				if (tokenizer.hasMoreTokens()) {
					if (tokenizer.nextToken().equals("btc")) {
						return new Request("getWalletInBitcoin");
					} else {
						System.out.println("ERROR \"wallet\" or \"wallet btc\"?");
					}
				} else {
					return new Request("getWallet");
				}
				break;

			case "comment":

				// Lettura e invio dell'intero commento
				int idPost = Integer.parseInt(tokenizer.nextToken());
				String comment = "";
				while (tokenizer.hasMoreTokens())
					comment += " " + tokenizer.nextToken();
				return new Request("addComment", idPost, comment);

			default:
				System.out.println("ERROR Unrecognised command. Try \"help\"");
			}

		} catch (NoSuchElementException e) {
			System.out.println("ERROR Invalid request lenght");
		}

		return null;
	}

	// Meotodo per inviare la richiesta all'interno del SocketChannel client
	public static void sendRequest(SocketChannel client, Request request) throws IOException {

		// ObjectMapper per convertire la request in una stringa json
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(request);

		// Istanziasione del buffer e invio del messaggio
		ByteBuffer buff = ByteBuffer.wrap(json.getBytes());
		client.write(buff);
		buff.clear();
	}

	// Metodo per la ricezione della risposta inviata dal server
	public static Response receiveResponse(SocketChannel client) throws IOException {

		// Allocazione della memoria per leggere la risposta e lettura
		ByteBuffer response = ByteBuffer.allocate(2048);
		client.read(response);

		// Conversione dei byte in String e poi in Response
		String json = new String(response.array());
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, Response.class);
	}

	// Metodo per gestire le risposte ottenute dal server
	public static void handleResponse(Response response) {

		System.out.println(response.getRisposta());

		// Controllo se la richiesta era una login ed è andata a buon fine
		if (response.getRequestType().equals("login") && response.getResult() == 1) {

			// ------------------- RMI CALLBACK -------------------
			try {

				// Chiamata al metodo dell'oggetto condiviso del server per ricevere le CALLBACK
				serverObject.registerForCallback(loggedAs, clientInterface);
				// System.out.println(" -> Registered for callback");
				multicastAddress = serverObject.getMulticastAddress();
				multicastPort = serverObject.getMulticastPort();

			} catch (RemoteException e) {
				System.out.println("FATAL ERROR Problems trying to use RMI");
				e.printStackTrace();
				System.exit(-1);
			}

			// ------------------- MULTICAST -------------------
			connectToMutlticast();

			// Controllo se la richiesta era una logout ed è andata a buon fine
		} else if (response.getRequestType().equals("logout") && response.getResult() == 1) {

			// ------------------- RMI CALLBACK -------------------
			try {

				// Chiamata al metodo dell'oggetto condiviso per non ricevere le CALLBACK
				serverObject.unregisterForCallback(loggedAs);
				loggedAs = null;
				// System.out.println(" -> Unregistered for callback");
			} catch (RemoteException e) {
				System.out.println("FATAL ERROR Problems trying to use RMI");
				e.printStackTrace();
				System.exit(-1);
			}

			// ------------------- MULTICAST -------------------
			disconnectFromMutlticast();

			// Chiamate per la chiusura della connessione TCP col server
			try {
				client.close();
				client = null;
				// System.out.println(" -> Closed TCP connection");
			} catch (IOException e) {
				System.out.println("FATAL ERROR Unable to close the TCP connection");
				System.exit(-1);
			}

			// Controllo se la richiesta era una login e non è andata a buon fine
		} else if (response.getRequestType().equals("login") && response.getResult() == 0) {

			// Assegnamento per segnare il mancato login e chiusura della connessione tcp
			loggedAs = null;
			try {
				client.close();
				client = null;
				// System.out.println(" -> Closed TCP connection");
			} catch (IOException e) {
				System.out.println("FATAL ERROR Unable to close the TCP connection");
				System.exit(-1);
			}
		}
	}

	// Metodo che chiama loggetto del server per eseguire la registrazione senza TCP
	public static void register(String username, String password, ArrayList<String> tags) {

		try {

			// Chiamata del metodo dell'oggetto condiviso e gestione della sua risposta
			handleResponse(serverObject.register(username, password, tags));
		} catch (RemoteException e) {
			System.out.println("FATAL ERROR Unable to register through RMI");
			System.exit(-1);
		}

	}

	// Metodo per stampare la lista locale dei propri followers
	public static void listFollowers() {

		// Chiamata al metodo dell'oggetto aggiornato dal server
		System.out.println("Followers:\n" + clientRmi.getFollowers().toString());
	}

	// Metodo per connettersi al gruppo multicast e riceverne i messaggi
	private static void connectToMutlticast() {

		// Preparazione del socket multicast e join del gruppo
		try {
			ms = new MulticastSocket(multicastPort);
			group = new InetSocketAddress(InetAddress.getByName(multicastAddress), multicastPort);
			netIf = NetworkInterface.getByName(configuration.getNetinterface());
			ms.joinGroup(group, netIf);
		} catch (IOException e) {
			System.out.println("FATAL ERROR Unable to join the multicast group");
			System.exit(-1);
		}

		// Avvio del thread che riceverà i messagi multicast riguardanti gli wallet
		MulticastReceiver multicastReceiver = new MulticastReceiver(ms);
		threadMulticast = new Thread(multicastReceiver);
		threadMulticast.start();
	}

	// Metodo per uscire dal gruppo multicast e non ricerne più i messaggi
	private static void disconnectFromMutlticast() {

		// Invio della richiesta di interruzione al thread
		threadMulticast.interrupt();
		try {

			// Chiusura della MulticastSocket per far lanciare l'interruzione
			ms.leaveGroup(group, netIf);
			ms.close(); // ?
		} catch (IOException e) {
			System.out.println("FATAL ERROR Unable to disconnect from multicast");
			System.exit(-1);
		}
	}
}