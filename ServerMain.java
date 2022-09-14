// ------------------- Marchi Davide 602476 -------------------

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Classe per avviare un server winsome
public class ServerMain {

	public static int numberActiveConnections = 0; // Numero di utenti connessi attualmente
	public static int terminationDelay = 60000; // keepAliveTime dei thread
	public static int dimCoda = 100; // Dimensione della coda di attesa
	public static int bufsize = 1024; // Dimensione del buffer di lettura
	static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(dimCoda); // Coda per i task
	static ExecutorService pool; // Threadpool de eseguirà i task
	static Winsome winsome; // Social network di riferimento

	// Metodo main
	public static void main(String[] args) {

		// Preparazione per RMI e lettura del file di configurazione
		RmiServerImplementation rmi_server = null;
		ServerConfiguration config = readConfiguration("ConfigServer.json");

		// Preparazione del threadpool che eseguirà i task
		pool = new ThreadPoolExecutor(config.getNworker(), config.getNworker(), terminationDelay, TimeUnit.MILLISECONDS,
				queue, new ThreadPoolExecutor.AbortPolicy());

		// Istanziazione di winsome a partire dai parametri di configurazione letti
		winsome = new Winsome(config);

		// Apertura del canale di comunicazione
		try (ServerSocketChannel s_channel = ServerSocketChannel.open()) {

			// Bind del socketchannel e settaggio in modalità non bloccante
			s_channel.socket().bind(new InetSocketAddress(config.getTcpport()));
			s_channel.configureBlocking(false);

			// Apertura del seletor
			Selector sel = Selector.open();
			s_channel.register(sel, SelectionKey.OP_ACCEPT);
			System.out.println("------------------- SERVER READY -------------------");

			// ------------------- RMI -------------------
			try {
				// Istanziazione ed esposrtazione dell'oggetto
				rmi_server = new RmiServerImplementation(winsome);
				RmiServerInterface stub = (RmiServerInterface) UnicastRemoteObject.exportObject(rmi_server, 0);
				LocateRegistry.createRegistry(config.getRegport());
				Registry r = LocateRegistry.getRegistry(config.getRegport());

				// Pubblicazione dello stub nel registry
				r.rebind(config.getRmiobjname(), stub);
			} catch (RemoteException e) {
				System.out.println("FATAL ERROR Dealing with RMI");
				System.exit(-1);
			}

			// ------------------- BACKUP -------------------

			// Lettura del file di backup e avvio del thread per eseguirne altri
			readBackupFromFiles();
			ServerBackup serverBackup = new ServerBackup(winsome);
			Thread threadBackup = new Thread(serverBackup);
			threadBackup.start();

			// ------------------- MULTICAST -------------------

			// Avvio del thrad per inviare i messaggi multicast ed effettura i pagamenti
			MulticastSender multicastSender = new MulticastSender(winsome);
			Thread threadMulticast = new Thread(multicastSender);
			threadMulticast.start();

			// Ciclo fino ad un'interruzione esterna o mlfunzionamento del server
			while (true) {

				// Attesa sulla select che può interrotta tramite una wakeup
				if (sel.select() == 0)
					continue;

				// Insieme della chiavi pronte ad eseguire azioni
				Set<SelectionKey> selectedKeys = sel.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();

				// Ciclo fino a che ho chiavi
				while (iter.hasNext()) {

					// Prendo e rimuovo la chiave
					SelectionKey key = iter.next();
					iter.remove();
					try {

						// Se la chiave permette un'operazione di accettazione
						if (key.isAcceptable()) {

							keyIsAcceptable(key, sel);

							// Se la chiave permette lettura
						} else if (key.isReadable()) {

							keyIsReadable(key, rmi_server);

							// Se la chiave permette scrittura
						} else if (key.isWritable()) {

							keyIsWritable(key);
						}

					} catch (IOException e) {

						// In caso di terminazione improvvisa di un client
						Attachment att = (Attachment) key.attachment();

						// Deregistro per la CALLBACK
						if (att.getLoginUsername() != null) {
							rmi_server.unregisterForCallback(att.getLoginUsername());
						}

						// Eseguo il logout e rimuovo la chiave dal selector
						winsome.logout(att.loginUsername);
						key.channel().close();
						key.cancel();
						System.out.println("Opened connections: " + --numberActiveConnections);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("FATAL ERROR Exception in server main");
			System.exit(-1);
		}
	}

	// Metodo per la lettura del file di configurazione
	public static ServerConfiguration readConfiguration(String fileName) {

		ObjectMapper mapper = new ObjectMapper();
		try {

			// Lettura dell'oggetto per contenere le configurazioni
			return mapper.readValue(Paths.get(fileName).toFile(), ServerConfiguration.class);
		} catch (IOException e1) {
			System.out.println("FATAL ERROR Unable to parse the configuration file");
			e1.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	// Metodo per la lettura dei backup
	public static void readBackupFromFiles() {

		ObjectMapper objectMapper = new ObjectMapper();

		try {

			// Lettura dell'id del prossimo post che sarà pubblicato
			File fIdPost = new File("BackupIdPost.json");
			if (fIdPost.exists() && !fIdPost.isDirectory()) {
				winsome.setIdPost(objectMapper.readValue(fIdPost, int.class));
			}

			// Lettura della struttura contenente gli utenti
			File fUsers = new File("BackupUsers.json");
			if (fUsers.exists() && !fUsers.isDirectory()) {
				winsome.setUsers(objectMapper.readValue(fUsers, new TypeReference<ConcurrentHashMap<String, User>>() {
				}));
			}

			// Lettura della struttura contenente i post
			File fPosts = new File("BackupPosts.json");
			if (fPosts.exists() && !fPosts.isDirectory()) {
				winsome.setPosts(
						objectMapper.readValue(fPosts, new TypeReference<ConcurrentHashMap<String, ArrayList<Post>>>() {
						}));
			}

			// Lettura della struttura contenente gli wallets
			File fWallets = new File("BackupWallets.json");
			if (fWallets.exists() && !fWallets.isDirectory()) {
				winsome.setWallets(
						objectMapper.readValue(fWallets, new TypeReference<ConcurrentHashMap<String, Wallet>>() {
						}));
			}

		} catch (IOException e) {
			System.out.println("ERROR Problmes readng backups");
		}
	}

	// Caso in cui posso accettare da un channel
	public static void keyIsAcceptable(SelectionKey key, Selector sel) throws IOException {

		// Preparazione del socket e accettazione della connesione
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel c_channel = server.accept();
		c_channel.configureBlocking(false);
		System.out.println("Opened connections: " + ++numberActiveConnections);

		// Preparazione della chiave e registrazione per la lettura
		Attachment att = new Attachment();
		c_channel.register(sel, SelectionKey.OP_READ, att);
	}

	// Caso in cui posso leggere da un channel
	public static void keyIsReadable(SelectionKey key, RmiServerImplementation rmi_server) throws IOException {

		// Peparazione del channel, della key e del buffer dove leggere
		SocketChannel c_channel = (SocketChannel) key.channel();
		Attachment att = (Attachment) key.attachment();
		ByteBuffer buffer = ByteBuffer.allocate(bufsize);
		buffer.clear();
		int byteRead = c_channel.read(buffer);
		buffer.flip();

		// Guardo se è la prima lettura da quel channel
		if (att.message == null) {

			// Inizializzo il messaggio con una nuova stringa
			att.message = new String(buffer.array()).trim();
		} else {

			// Aggiungo alla parte del messaggio che avevo già letto
			att.message = att.message + new String(buffer.array()).trim();
		}

		// Controllo se con la lettura ho riempito tutto i buffer
		if (byteRead == bufsize) {

			key.attach(att);

		} else if (byteRead == -1) {

			// Rimuovo la chiave se ho avuto problmei con la lettura
			key.cancel();
			key.channel().close();
			System.out.println("Opened connections: " + --numberActiveConnections);

		} else if (byteRead < bufsize) {

			// Ho finito la lettura del messaggio, quindi procedo alla conversione
			ObjectMapper objectMapper = new ObjectMapper();
			att.request = objectMapper.readValue(att.message, Request.class);

			// Segno che quella connessione non deve fare operazioni al momento
			key.interestOps(0);
			pool.execute(new RequestHandler(key, winsome, rmi_server));
			buffer.clear();
		}
	}

	// Caso in cui posso scrivere in un channel
	public static void keyIsWritable(SelectionKey key) throws IOException {

		// Prendo il channel e preparo l'objectMapper
		SocketChannel c_channel = (SocketChannel) key.channel();
		c_channel.configureBlocking(false);
		ObjectMapper objectMapper = new ObjectMapper();
		Attachment att = (Attachment) key.attachment();
		att.message = objectMapper.writeValueAsString(att.response);

		// Controllo se non ho nulla da inviare
		if (att.message == null) {
			System.err.println("ERROR PRoblems with the client. Closing the connection");
			key.cancel();
			c_channel.close();
		}

		// Preparo il buffer per la scrittura sul cannel e scrivo
		ByteBuffer buffer = ByteBuffer.wrap(att.message.getBytes());
		int byteWrote = c_channel.write(buffer);

		// Controllo se ho scritto tutto il messaggio
		if (byteWrote == att.message.getBytes().length) {

			att.message = null;
			att.request = null;
			att.response = null;
			key.attach(att);

			// Ri-registro la chiave in lettura per le richieste successive
			key.interestOps(SelectionKey.OP_READ);
		}
	}
}