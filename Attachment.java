// ------------------- Marchi Davide 602476 -------------------

// Classe che verrà usata come attachment delle chiavi usate all'interno del selector
public class Attachment {

	public String loginUsername; // Username dell'utente che sta utilizzando la connessione
	public Request request; // Struttura per contenere la richiesta che è stata ricevuto
	public Response response; // Struttura nella quale sarà salvata la risposta da inviare al client
	public String message; // Stringa di appoggio usata per inviare e ricevere oggetti come stringhe json

	// Costruttore vuoto
	public Attachment() {
	}

	// Metodo setter standard
	public void setLoginUsername(String loginUsername) {
		this.loginUsername = loginUsername;
	}

	// Metodo setter standard
	public void setRequest(Request request) {
		this.request = request;
	}

	// Metodo setter standard
	public void setResponse(Response response) {
		this.response = response;
	}

	// Metodo setter standard
	public void setMessage(String message) {
		this.message = message;
	}

	// Metodo getter standard
	public String getLoginUsername() {
		return this.loginUsername;
	}

	// Metodo getter standard
	public Request getRequest() {
		return this.request;
	}

	// Metodo getter standard
	public Response getResponse() {
		return this.response;
	}

	// Metodo getter standard
	public String getMessage() {
		return this.message;
	}
}