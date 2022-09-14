// ------------------- Marchi Davide 602476 -------------------

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// Classe che rappresenta il wallet di un utente iscritto a winsome
public class Wallet {

	private String owner; // Username identificativo dell'utente
	private double wincoin; // Quantità di wincoin posseduti
	private ArrayList<String> transactions; // Storico della transazioni

	// Costruttore vuoto
	public Wallet() {
	}

	// Costruttore con parametro
	public Wallet(String owner) {
		this.owner = owner;
		this.wincoin = 0;
		this.transactions = new ArrayList<String>();
	}

	// Metodo setter standard
	public synchronized void setOwner(String owner) {
		this.owner = owner;
	}

	// Metodo setter standard
	public synchronized void setWincoin(double wincoin) {
		this.wincoin = wincoin;
	}

	// Metodo setter standard
	public synchronized void setTransactions(ArrayList<String> transactions) {
		this.transactions = transactions;
	}

	// Metodo getter standard
	public synchronized String getOwner() {
		return this.owner;
	}

	// Metodo getter standard
	public synchronized double getWincoin() {
		return this.wincoin;
	}

	// Metodo getter standard
	public synchronized ArrayList<String> getTransactions() {
		return this.transactions;
	}

	// Metodo per l'aggiunta di wincoin al wallet
	public synchronized void addWincoin(double amount) {

		// Aggiunta della cifra e inserimento della transazione
		this.wincoin += amount;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		this.transactions.add(dtf.format(now) + " +" + amount);
	}
}