// ------------------- Marchi Davide 602476 -------------------

// Classe per contenere i vari parametri di configurazione da leggere all'avvio del server
public class ServerConfiguration {

	private int udpport; // Indirizzo udp del server winsome
	private int tcpport; // Porta su cui si mette ad ascoltare listen socket
	private String multicast; // Indirizzo del gruppo multicast a cui i client devono iscriversi
	private int mcastport; // Porta di multicast su cui i client devono mettersi in ascolto
	private int regport; // Porta su cui è in ascolto il registry
	private String rmiobjname; // Nome di binding per l'oggetto RMI che serve ai client per registrarsi
	private int rewardtimer; // Ogni quanti millisecondi calcolare le ricompense per gli utenti
	private int backuptimer; // Ogni quanti millisecondi eseguire il backup automaticobackup
	private int nworker; // Numero di worker per la gestione delle richieste dei client
	private double authorpercentage; // Percentuale della ricompensa che spetta all'autore

	// Costruttore vuoto
	public ServerConfiguration() {
	}

	// Costruttore con parametri
	public ServerConfiguration(int udpport, int tcpport, String multicast, int mcastport, int regport,
			String rmiobjname, int rewardtimer, int backuptimer, int nworker, double authorpercentage) {
		this.udpport = udpport;
		this.tcpport = tcpport;
		this.multicast = multicast;
		this.mcastport = mcastport;
		this.regport = regport;
		this.rmiobjname = rmiobjname;
		this.rewardtimer = rewardtimer;
		this.backuptimer = backuptimer;
		this.nworker = nworker;
		this.authorpercentage = authorpercentage;
	}

	// Metodo setter standard
	public void setUdpport(int udpport) {
		this.udpport = udpport;
	}

	// Metodo setter standard
	public void setTcpport(int tcpport) {
		this.tcpport = tcpport;
	}

	// Metodo setter standard
	public void setMulticast(String multicast) {
		this.multicast = multicast;
	}

	// Metodo setter standard
	public void setMcastport(int mcastport) {
		this.mcastport = mcastport;
	}

	// Metodo setter standard
	public void setRegport(int regport) {
		this.regport = regport;
	}

	// Metodo setter standard
	public void setRmiobjname(String rmiobjname) {
		this.rmiobjname = rmiobjname;
	}

	// Metodo setter standard
	public void setRewardtimer(int rewardtimer) {
		this.rewardtimer = rewardtimer;
	}

	// Metodo setter standard
	public void setBackuptimer(int backuptimer) {
		this.backuptimer = backuptimer;
	}

	// Metodo setter standard
	public void setNworker(int nworker) {
		this.nworker = nworker;
	}

	// Metodo setter standard
	public void setAuthorpercentage(double authorpercentage) {
		this.authorpercentage = authorpercentage;
	}

	// Metodo getter standard
	public int getUdpport() {
		return udpport;
	}

	// Metodo getter standard
	public String getMulticast() {
		return multicast;
	}

	// Metodo getter standard
	public int getTcpport() {
		return tcpport;
	}

	// Metodo getter standard
	public int getMcastport() {
		return mcastport;
	}

	// Metodo getter standard
	public int getRegport() {
		return regport;
	}

	// Metodo getter standard
	public String getRmiobjname() {
		return rmiobjname;
	}

	// Metodo getter standard
	public int getRewardtimer() {
		return rewardtimer;
	}

	// Metodo getter standard
	public int getBackuptimer() {
		return backuptimer;
	}

	// Metodo getter standard
	public int getNworker() {
		return nworker;
	}

	// Metodo getter standard
	public double getAuthorpercentage() {
		return authorpercentage;
	}
}