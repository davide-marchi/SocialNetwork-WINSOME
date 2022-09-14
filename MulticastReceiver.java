// ------------------- Marchi Davide 602476 -------------------

import java.io.*;
import java.net.*;

// Task laciato dal client per ricevere i messaggi multicast
public class MulticastReceiver implements Runnable {

	private MulticastSocket ms; // Socket multicast per la ricezione dei messaggi

	// Costruttore con passaggio della socket
	MulticastReceiver(MulticastSocket ms) {
		this.ms = ms;
	}

	// Metodo run eseguito dal thread
	public void run() {

		// Allocamento del buffer dove poer scrivere i messaggi ricevuti
		byte[] buffer = new byte[64];

		// Ciclo fino a che non ho ricevuto la richiesta di interruzione
		while (!Thread.interrupted()) {
			try {

				// Ricezione del datagramma e stampa del messaggio ricevuto
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				ms.receive(dp);
				String message = new String(dp.getData());
				System.out.println("\t" + message.trim());
			} catch (IOException e) {
			}
		}
	}
}