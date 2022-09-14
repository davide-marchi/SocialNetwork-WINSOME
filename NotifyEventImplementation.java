// ------------------- Marchi Davide 602476 -------------------

import java.rmi.server.RemoteObject;
import java.util.ArrayList;

// Classe che implementa l'oggetto remoto che il client renderà raggiungibile tramite RMI
public class NotifyEventImplementation extends RemoteObject implements NotifyEventInterface {

	private ArrayList<String> followers; // Lista contenente gli username dei propri seguaci

	// Costruttore vuoto
	NotifyEventImplementation() {
		this.followers = new ArrayList<String>();
	}

	// Metodo per aggiungere un nuovo follower
	public synchronized void notifyFollow(String newFollower) {
		if (!followers.contains(newFollower)) {
			followers.add(newFollower);
		}
	}

	// Metodo per rimuovere un follower
	public synchronized void notifyUnfollow(String newFollower) {
		if (followers.contains(newFollower)) {
			followers.remove(newFollower);
		}
	}

	// Metodo per inserire all'inizio gli utenti che già ci seguono
	public synchronized void setFollowers(ArrayList<String> followers) {
		this.followers = followers;
	}

	// Metodo per recuperare la lista dei followers
	public synchronized ArrayList<String> getFollowers() {
		return new ArrayList<String>(followers);
	}
}