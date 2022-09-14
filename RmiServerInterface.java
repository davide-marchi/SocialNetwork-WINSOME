// ------------------- Marchi Davide 602476 -------------------

import java.rmi.*;
import java.util.ArrayList;

//Interfaccia che verrà usata per comunicare al client i metodi chiamabili tramite RMI
public interface RmiServerInterface extends Remote {

	public Response register(String username, String password, ArrayList<String> tags) throws RemoteException;

	public void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException;

	public void unregisterForCallback(String username) throws RemoteException;

	public String getMulticastAddress() throws RemoteException;

	public int getMulticastPort() throws RemoteException;
}
