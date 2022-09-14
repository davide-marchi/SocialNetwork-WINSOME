// ------------------- Marchi Davide 602476 -------------------

import java.io.*;
import java.net.*;

// Task per i pagamenti periodici e l'invio di messaggi multicast per notificarli
public class MulticastSender implements Runnable {

	private Winsome winsome; // Oggetto di cui verrà chiamato il metodo per pagamenti

	// Costruttore al quale deve essere passato il social network di riferimento
	MulticastSender(Winsome winsome) {
		this.winsome = winsome;
	}

	// Metodo run eseguito dal thread
	public void run() {

		// Scrittura del messaggio e rinominazione del thread
		byte[] data = "Wallets updated".getBytes();
		Thread.currentThread().setName("Thread-Payments");

		// Lettura dell'indirizzo da i file di configurazione
		InetAddress ia = null;
		try {
			ia = InetAddress.getByName(winsome.config.getMulticast());
		} catch (UnknownHostException e1) {
			System.out.println("FATAL ERROR Unable to retrive multicast address");
			System.exit(-1);
		}

		// Istanziazione del datagramma da inviare
		DatagramPacket dp = new DatagramPacket(data, data.length, ia, winsome.config.getMcastport());

		// Apertura del canale di comunicazione
		try (DatagramSocket ms = new DatagramSocket(winsome.config.getUdpport());) {

			// Ciclo fino all'interruzione manuale del server
			while (true) {

				// Attesa del tempo che deve trascorrere tra un aggiornamento e l'altro
				try {
					Thread.sleep(winsome.config.getRewardtimer());
				} catch (InterruptedException e) {
					System.out.println("ERROR Waken up during thread.sleep()");
				}

				// Chiamata alla funzione per ricompensare gli utenti e stampa
				winsome.pay();
				System.out.println("\t\t" + Thread.currentThread().getName() + " -> Wallets updated");

				// Invio del datagramma in multicast per notificare i client in ascolto
				try {
					ms.send(dp);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		} catch (SocketException e1) {
			System.out.println("FATAL ERROR Problems with the multicast");
			System.exit(-1);
		}
	}
}