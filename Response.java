// ------------------- Marchi Davide 602476 -------------------

// Classe che conterrà le risposte di winsome per il client
public class Response implements java.io.Serializable {

	private int result; // Risultato della richiesta: 1 riuscita, 0 fallita
	private String risposta; // Stringa contenente la risposta effettiva
	private String requestType; // Tipo della richiesta a cui si riferisce la risposta

	// Costruttore vuoto
	public Response() {
	}

	// Costruttore con parametri
	public Response(int result, String risposta, String requestType) {
		this.result = result;
		this.risposta = risposta;
		this.requestType = requestType;
	}

	// Metodo setter standard
	public void setResult(int result) {
		this.result = result;
	}

	// Metodo setter standard
	public void setRisposta(String risposta) {
		this.risposta = risposta;
	}

	// Metodo setter standard
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	// Metodo getter standard
	public int getResult() {
		return this.result;
	}

	// Metodo getter standard
	public String getRisposta() {
		return this.risposta;
	}

	// Metodo getter standard
	public String getRequestType() {
		return this.requestType;
	}
}