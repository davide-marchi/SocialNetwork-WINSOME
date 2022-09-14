// ------------------- Marchi Davide 602476 -------------------

// Classe per contenere i vari parametri di configurazione da leggere all'avvio del client
public class ClientConfiguration {

	private String server; // Indirizzo IP del server Winsome : "localhost"
	private int tcpport; // Porta TCP su cui si mette ad ascoltare il listen socket
	private int regport; // Porta su cui è in ascolto il registry (per RMI)
	private String rmiobjname; // Nome di binding per l'oggetto RMI che serve ai client per registrarsi
	private String netinterface; // Interfaccia network

	// Costruttore vuoto
	public ClientConfiguration() {
	}

	// Costruttore con parametri
	public ClientConfiguration(String server, int tcpport, int regport, String rmiobjname, String netinterface) {
		this.server = server;
		this.tcpport = tcpport;
		this.regport = regport;
		this.rmiobjname = rmiobjname;
		this.netinterface = netinterface;
	}

	// Metodo setter standard
	public void setServer(String server) {
		this.server = server;
	}

	// Metodo setter standard
	public void setTcpport(int tcpport) {
		this.tcpport = tcpport;
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
	public void setNetinterface(String netinterface) {
		this.netinterface = netinterface;
	}

	// Metodo getter standard
	public String getServer() {
		return server;
	}

	// Metodo getter standard
	public int getTcpport() {
		return tcpport;
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
	public String getNetinterface() {
		return netinterface;
	}
}