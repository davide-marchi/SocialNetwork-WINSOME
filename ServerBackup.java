// ------------------- Marchi Davide 602476 -------------------

// Clsse che dopo un intervallo di tempo edegue un backup
public class ServerBackup implements Runnable {

	Winsome winsome; // Oggetto del quale dovranno essere effettuati i backup

	// Costruttore con attributo
	ServerBackup(Winsome winsome) {
		this.winsome = winsome;
	}

	// Metodo run
	public void run() {

		// Assegnamento del nome al thread
		Thread.currentThread().setName("Thread-Backup");

		// Ciclo fino ad un malfunzionamento del server
		while (true) {
			try {

				// Attesa del tempo nel file di configurazione
				Thread.sleep(winsome.config.getBackuptimer());
			} catch (InterruptedException e) {
				System.out.println("Waken up during the sleep");
			}

			// Chiamata alla funzione per salvare lo stato
			winsome.backup();
			System.out.println("\t\t" + Thread.currentThread().getName() + " -> Backup done");
		}
	}
}