// ------------------- Marchi Davide 602476 -------------------

import java.rmi.*;
import java.util.ArrayList;

// Interfaccia che verrà usata per comunicare al server i metodi chiamabili tramite RMI
public interface NotifyEventInterface extends Remote {

	public void notifyFollow(String newFollower) throws RemoteException;

	public void notifyUnfollow(String newFollower) throws RemoteException;

	public void setFollowers(ArrayList<String> followers) throws RemoteException;
}