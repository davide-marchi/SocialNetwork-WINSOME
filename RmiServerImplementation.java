// ------------------- Marchi Davide 602476 -------------------

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;

//Classe che implementa l'oggetto remoto che il server renderà raggiungibile tramite RMI
public class RmiServerImplementation extends RemoteServer implements RmiServerInterface {

	private Winsome winsome; // Oggetto su cui poter "inoltrare" varie chiamate
	private HashMap<String, NotifyEventInterface> clients; // HashMap per sapere come contattare i client

	// Costruttore a cui passo il social network
	RmiServerImplementation(Winsome winsome) {
		this.winsome = winsome;
		this.clients = new HashMap<String, NotifyEventInterface>();
	}

	// Metodo chiamato dai client per registrare un utente a winsome
	public Response register(String username, String password, ArrayList<String> tags) throws RemoteException {

		// Chiamata a winsome per la registrazione
		return winsome.register(username, password, tags);
	}

	// Metodo chiamato dai client per richiedere di registrarsi per la CALLBACK
	public synchronized void registerForCallback(String username, NotifyEventInterface ClientInterface)
			throws RemoteException {

		// Controllo che l'utente non sia già registrato
		if (!clients.containsKey(username)) {

			// Aggiunta dell'utente nella HashMap
			clients.put(username, ClientInterface);
			ClientInterface.setFollowers(winsome.getFollowers(username));
			System.out.println("-> " + username + " registered for callbacks");
		}

	}

	// Metodo chiamato dai client per richiedere di deregistrarsi per la CALLBACK
	public synchronized void unregisterForCallback(String username) throws RemoteException {

		// Controllo che l'utente fosse già registrato elo toglio della HashMap
		if (clients.containsKey(username)) {
			clients.remove(username);
			System.out.println("<- " + username + " unregistered for callbacks");
		}
	}

	// Metodo chiamato lato server per notificare un nuovo seguace ad un clietn
	public synchronized void followCallback(String username, String newFollower) {

		// Comtrollo se l'utente interessato fosse registrato per la callback
		if (clients.containsKey(username)) {
			try {

				// Chiamata all'oggetto remoto del cliente interessato
				clients.get(username).notifyFollow(newFollower);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	// Metodo chiamato dal server per notificare la perdita di un seguace
	public synchronized void unfollowCallback(String username, String newUnfollower) {

		// Comtrollo se l'utente interessato fosse registrato per la callback
		if (clients.containsKey(username)) {
			try {

				// Chiamata all'oggetto remoto del cliente interessato
				clients.get(username).notifyUnfollow(newUnfollower);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	// Metodo chiamato lato client per sapere l'indirizzo IP del gruppo multicast
	public String getMulticastAddress() {
		return winsome.getMulticastAddress();
	}

	// Metodo chiamato lato client per sapere la porta del gruppo multicast
	public int getMulticastPort() {
		return winsome.getMulticastPort();
	}
}