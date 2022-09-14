// ------------------- Marchi Davide 602476 -------------------

import java.util.ArrayList;
import java.util.HashMap;

// Classe che contiene tutte le informazioni relative ad un post in winsome
public class Post implements Comparable<Post> {

	private int id; // Numero univoco identificativo del post
	private String title; // Titolo del post di massimo 20 caratteri
	private String content; // Contenuto del post di massimo 500 caratteri
	private String author; // Username dell'utente che lo ha postato
	private String rewinFrom; // Username dell'utente da ci si è fatto il rewin
	private int rewardCicle; // Numero per sapere il ciclo dei pagamanti

	// HashMap per le valutazioni <Username dell'utente, Valutazione lasciata>
	HashMap<String, Integer> unpaidRatings = new HashMap<String, Integer>();
	HashMap<String, Integer> paidRatings = new HashMap<String, Integer>();

	// HashMap per i commenti <Username dell'utente, Lista dei commenti lasciati>
	HashMap<String, ArrayList<String>> paidComments = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> unpaidComments = new HashMap<String, ArrayList<String>>();

	// Costruttore vuoto
	Post() {
	}

	// Costruttore per l'istanziazione di un post
	public Post(String titolo, String contenuto, String autore, int id, int rewardCicle) {
		this.id = id;
		this.title = titolo;
		this.content = contenuto;
		this.author = autore;
		this.rewardCicle = rewardCicle;
	}

	// Costruttore per l'istanziazione dei rewin di un post
	public Post(String titolo, String contenuto, String autore, int id, int rewardCicle, String rewinFrom) {
		this.id = id;
		this.title = titolo;
		this.content = contenuto;
		this.author = autore;
		this.rewardCicle = rewardCicle;
		this.rewinFrom = rewinFrom;
	}

	// Metodo setter standard
	public void setId(int id) {
		this.id = id;
	}

	// Metodo setter standard
	public void setTitle(String title) {
		this.title = title;
	}

	// Metodo setter standard
	public void setContent(String content) {
		this.content = content;
	}

	// Metodo setter standard
	public void setAuthor(String author) {
		this.author = author;
	}

	// Metodo setter standard
	public void setRewinFrom(String rewinFrom) {
		this.rewinFrom = rewinFrom;
	}

	// Metodo setter standard
	public void setRewardCicle(int rewardCicle) {
		this.rewardCicle = rewardCicle;
	}

	// Metodo setter standard
	public void setPaidRatings(HashMap<String, Integer> paidRatings) {
		this.paidRatings = paidRatings;
	}

	// Metodo setter standard
	public void setUnpaidRatings(HashMap<String, Integer> unpaidRatings) {
		this.unpaidRatings = unpaidRatings;
	}

	// Metodo setter standard
	public void setPaidComments(HashMap<String, ArrayList<String>> paidComments) {
		this.paidComments = paidComments;
	}

	// Metodo setter standard
	public void setUnpaidComments(HashMap<String, ArrayList<String>> unpaidComments) {
		this.unpaidComments = unpaidComments;
	}

	// Metodo getter standard
	public int getId() {
		return this.id;
	}

	// Metodo getter standard
	public String getTitle() {
		return this.title;
	}

	// Metodo getter standard
	public String getContent() {
		return this.content;
	}

	// Metodo getter standard
	public String getAuthor() {
		return this.author;
	}

	// Metodo getter standard
	public String getRewinFrom() {
		return this.rewinFrom;
	}

	// Metodo getter standard
	public int getRewardCicle() {
		return this.rewardCicle;
	}

	// Metodo getter standard
	public HashMap<String, Integer> getPaidRatings() {
		return this.paidRatings;
	}

	// Metodo getter standard
	public HashMap<String, Integer> getUnpaidRatings() {
		return this.unpaidRatings;
	}

	// Metodo getter standard
	public HashMap<String, ArrayList<String>> getPaidComments() {
		return this.paidComments;
	}

	// Metodo getter standard
	public HashMap<String, ArrayList<String>> getUnpaidComments() {
		return this.unpaidComments;
	}

	// Metodo per l'aggiunta di like ad un post
	public synchronized int addLike(String likedBy) {

		// Controllo se l'utente abbia già lasciato una valutazione
		if (unpaidRatings.containsKey(likedBy) || paidRatings.containsKey(likedBy))
			return 0;
		else {
			unpaidRatings.put(likedBy, 1);
		}
		return 1;
	}

	// Metodo per l'aggiunta di dilike ad un post
	public synchronized int addDislike(String dislikedBy) {

		// Controllo se l'utente abbia già lasciato una valutazione
		if (unpaidRatings.containsKey(dislikedBy) || paidRatings.containsKey(dislikedBy))
			return 0;
		else {
			unpaidRatings.put(dislikedBy, -1);
		}
		return 1;
	}

	// Metodo per l'aggiunta di un nuovo commento
	public synchronized void addComment(String username, String comment) {

		// Eventuale instanziazione di una nuova lista e aggiunta del commento
		unpaidComments.putIfAbsent(username, new ArrayList<String>());
		unpaidComments.get(username).add(comment);
	}

	// Metodo per sapere il numero complessivo delle valutazioni positive
	public synchronized int takeLikesNumber() {

		int likesNumber = 0;

		// Scorrimento della valutazioni pagate
		for (int rate : this.paidRatings.values()) {
			if (rate == 1) {
				likesNumber++;
			}
		}

		// Scorrimento della valutazioni non pagate
		for (int rate : this.unpaidRatings.values()) {
			if (rate == 1) {
				likesNumber++;
			}
		}
		return likesNumber;
	}

	// Metodo per sapere il numero complessivo delle valutazioni negative
	public synchronized int takeDislikesNumber() {

		int dislikesNumber = 0;

		// Scorrimento della valutazioni pagate
		for (int rate : this.paidRatings.values()) {
			if (rate == -1) {
				dislikesNumber++;
			}
		}

		// Scorrimento della valutazioni non pagate
		for (int rate : this.unpaidRatings.values()) {
			if (rate == -1) {
				dislikesNumber++;
			}
		}
		return dislikesNumber;
	}

	// Metodo per trasformare tutti i commenti in un'unica stringa
	public synchronized String takeCommentsAsString() {

		StringBuilder str = new StringBuilder("Comments: \n");

		// Ciclo per ogni commento pagato e lo aggiungo alla stringa
		for (String username : paidComments.keySet()) {
			for (String comment : paidComments.get(username)) {
				str.append("\t" + username + " \"" + comment + "\"\n");
			}
		}

		// Ciclo per ogni commento non pagato e lo aggiungo alla stringa
		for (String username : unpaidComments.keySet()) {
			for (String comment : unpaidComments.get(username)) {
				str.append("\t" + username + " \"" + comment + "\"\n");
			}
		}
		return str.toString();
	}

	// Metodo per sapere quanti commenti sono stati fatti da uno specifico utente
	public synchronized int takeCommentsAmountFrom(String username) {

		// Controllo se effettivamente ci siano dei commenti
		if (paidComments.isEmpty() && unpaidComments.isEmpty())
			return 0;

		int amount = 0;

		// Controllo dei commenti non pagati
		if (unpaidComments.get(username) != null) {
			amount += unpaidComments.get(username).size();
		}

		// Controllo dei commenti pagati
		if (paidComments.get(username) != null) {
			amount += unpaidComments.get(username).size();
		}
		return amount;
	}

	// Metodo per consentire l'ordinamento dei post basandosi sull'id del post
	public int compareTo(Post p) {

		// Contronto degli id e ritorno del risultato
		if (this.id < p.id) {
			return 1;
		} else {
			return -1;
		}
	}
}