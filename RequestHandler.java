// ------------------- Marchi Davide 602476 -------------------

import java.nio.channels.SelectionKey;

// Task che verrà lanciato dal server per gestire le richieste parallelamente
public class RequestHandler implements Runnable {

	SelectionKey key; // Chiave del selector da cui è arrivata la richiesta
	Winsome winsome; // Social network del quale chiamare le funzioni
	RmiServerImplementation rmi_server; // Oggeto RMI per comunicare col client

	// Costruttore invocato dal ServerMain quando deve gestire una richiesta
	public RequestHandler(SelectionKey key, Winsome winsome, RmiServerImplementation rmi_server) {

		this.key = key;
		this.winsome = winsome;
		this.rmi_server = rmi_server;
	}

	// Metodo run
	public void run() {

		// Assegnamenti e stampa preliminare
		Attachment att = (Attachment) key.attachment();
		Request req = att.request;
		if (req.getRequestType() != null)
			System.out.println("\t" + Thread.currentThread().getName() + " -> " + req.getRequestType());

		// Controllo se l'utente non è loggato e non sta richiedendo la login
		if (att.getLoginUsername() == null && !req.getRequestType().equals("login")) {

			att.response = new Response(0, "You are not logged in yet", req.getRequestType());

		} else {

			// ObjectMapper per le conversioni in json
			switch (req.getRequestType()) {

			case "login":

				// Controllo se l'utente è già loggato o meno
				if (att.getLoginUsername() != null) {
					att.response = new Response(0, "You are already logged in", "login");
					break;
				}
				att.response = winsome.login(req.getUsername(), req.getPassword());
				if (att.response.getResult() == 1) {
					att.loginUsername = req.getUsername();
				}
				break;

			case "logout":

				// Invocazione del metodo di winsome
				att.response = winsome.logout(att.loginUsername);
				att.loginUsername = null;
				break;

			case "listUsers":

				// Invocazione del metodo di winsome
				att.response = winsome.listUsers(att.loginUsername);
				break;

			case "listFollowing":

				// Invocazione del metodo di winsome
				att.response = winsome.listFollowing(att.loginUsername);
				break;

			case "follow":

				// Invocazione del metodo di winsome ed eventuale chiamata RMI CALLBACK
				att.response = winsome.follow(att.loginUsername, req.getIdUser());
				if (att.response.getResult() == 1) {
					rmi_server.followCallback(req.getIdUser(), att.loginUsername);
				}
				break;

			case "unfollow":

				// Invocazione del metodo di winsome ed evntuale chiamata RMI CALLBACK
				att.response = winsome.unfollow(att.loginUsername, req.getIdUser());
				if (att.response.getResult() == 1) {
					rmi_server.unfollowCallback(req.getIdUser(), att.loginUsername);
				}
				break;

			case "post":

				// Invocazione del metodo di winsome
				att.response = winsome.createPost(att.loginUsername, req.getTitle(), req.getContent());
				break;

			case "viewBlog":

				// Invocazione del metodo di winsome
				att.response = winsome.viewBlog(att.loginUsername);
				break;

			case "showFeed":

				// Invocazione del metodo di winsome
				att.response = winsome.showFeed(att.loginUsername);
				break;

			case "showPost":

				// Invocazione del metodo di winsome
				att.response = winsome.showPost(req.getIdPost());
				break;

			case "rewin":

				// Invocazione del metodo di winsome
				att.response = winsome.rewinPost(att.loginUsername, req.getIdPost());
				break;

			case "delete":

				// Invocazione del metodo di winsome
				att.response = winsome.deletePost(att.loginUsername, req.getIdPost());
				break;

			case "ratePost":

				// Invocazione del metodo di winsome
				att.response = winsome.ratePost(att.loginUsername, req.getIdPost(), req.getRating());
				break;

			case "getWalletInBitcoin":

				// Invocazione del metodo di winsome
				att.response = winsome.getWalletInBitcoin(att.loginUsername);
				break;

			case "getWallet":

				// Invocazione del metodo di winsome
				att.response = winsome.getWallet(att.loginUsername);
				break;

			case "addComment":

				// Invocazione del metodo di winsome
				att.response = winsome.addComment(att.loginUsername, req.getIdPost(), req.getComment());
				break;

			default:

				// Invocazione del metodo di winsome
				att.response = new Response(0, "Unrecognised command", "unknown");
				break;
			}
		}

		// Svuotamento del campo contenente la richiesta
		att.request = null;
		try {

			// Registrazione della chiave per fare la scrittura e wakeup del selector
			key.channel().register(key.selector(), SelectionKey.OP_WRITE, att);
			key.selector().wakeup();

		} catch (Exception e) {
			System.out.println("FATAL ERROR Problems re-registering the key");
			System.exit(-1);
		}
	}
}