// ------------------- Marchi Davide 602476 -------------------

import java.util.ArrayList;

// Classe che rappresenta il singolo utente che usa winsome
public class User {

	private String id; // Username dell'utente, essendo univoco è anche il suo identificativo
	private int hashedPassword; // Funzione hash (hashCode()) della password dell'utente
	private ArrayList<String> tags; // Lista dei tag dell'utente
	private ArrayList<String> followers; // Lista dei seguaci dell'utente
	private ArrayList<String> following; // Lista dei seguiti dall'utente

	// Costruttore vuoto
	public User() {
	}

	// Costruttore con parametri usato per la registrazione
	public User(String id, int hashedPassword, ArrayList<String> tags) {
		this.id = id;
		this.hashedPassword = hashedPassword;
		this.tags = tags;
		this.followers = new ArrayList<String>();
		this.following = new ArrayList<String>();
	}

	// Metodo setter standard
	public void setId(String id) {
		this.id = id;
	}

	// Metodo setter standard
	public void setHashedPassword(int hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	// Metodo getter standard
	public String getId() {
		return this.id;
	}

	// Metodo getter standard
	public int getHashedPassword() {
		return this.hashedPassword;
	}

	// Metodo getter standard
	public ArrayList<String> getTags() {
		return this.tags;
	}

	// Metodo getter per ottenere una nuova lista con i seguaci
	public ArrayList<String> getFollowers() {
		return new ArrayList<>(followers);
	}

	// Metodo getter per ottenere una nuova lista con i seguiti
	public ArrayList<String> getFollowing() {
		return new ArrayList<>(following);
	}

	// Metodo per l'aggiunta di un reguace
	public int addFollower(String follower) {

		// Controllo se è già seguito da quell'utente
		if (followers.contains(follower))
			return 0;
		else
			followers.add(follower);
		return 1;
	}

	// Metodo per la rimozione di un reguace
	public int removeFollower(String follower) {

		// Controllo se non è seguito da quell'utente
		if (!followers.contains(follower))
			return 0;
		else
			followers.remove(follower);
		return 1;
	}

	// Metodo per l'aggiunta di un seguito
	public synchronized int addFollowed(String followed) {

		// Controllo se segue già quell'utente
		if (this.following.contains(followed))
			return 0;
		else
			this.following.add(followed);
		return 1;
	}

	// Metodo per la rimozione di un seguito
	public synchronized int removeFollowed(String followed) {

		// Controllo se non segue quell'utente
		if (!this.following.contains(followed))
			return 0;
		else
			this.following.remove(followed);
		return 1;
	}
}