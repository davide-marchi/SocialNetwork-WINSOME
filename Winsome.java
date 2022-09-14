// ------------------- Marchi Davide 602476 -------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// Classe che rappresenta il social network winsome
public class Winsome {

	// Oggetto che conterrà la configurazione di winsome
	ServerConfiguration config = new ServerConfiguration();

	// Intero pre assegnare identificativi univoci ai post
	private AtomicInteger idPost = new AtomicInteger(0);

	// HashMap per gli utenti registrati contente coppie <username, User>
	private ConcurrentHashMap<String, User> Users = new ConcurrentHashMap<String, User>();

	// HashMap contenente le coppie <username autore, Post pubblicati>
	private ConcurrentHashMap<String, ArrayList<Post>> Posts = new ConcurrentHashMap<String, ArrayList<Post>>();

	// HashMap contente le coppie <username proprietario, Wallet>
	private ConcurrentHashMap<String, Wallet> Wallets = new ConcurrentHashMap<String, Wallet>();

	// Set contenente gli username degli utenti che sono attualmente loggati
	private Set<String> loggedUsers = new HashSet<>();

	// Ordine dell sincronizzazioni per evitare deadlock
	// - Users -> loggedUsers
	// - Posts -> Utenti
	// - Users -> Wallets
	// - Posts -> Wallets
	// Ordine complessivo: Posts -> Users -> Wallets -> loggedUsers

	// Costruttore per partire dalla configurazione
	public Winsome(ServerConfiguration config) {
		this.config = config;
	}

	// Metodo gestire la richiesta di login da parte di un client
	public Response login(String username, String password) {

		synchronized (Users) {

			// Controllo se l'username è valido
			if (!Users.containsKey(username)) {
				return new Response(0, username + " does not exist", "login");
			}

			// Controllo se la password è quella giusta
			if (password.hashCode() == Users.get(username).getHashedPassword()) {

				synchronized (loggedUsers) {

					// Controllo se l'utente è già loggato
					if (!loggedUsers.contains(username)) {
						loggedUsers.add(username);
						System.out.println("-> " + username + " logged in");
						return new Response(1, username + " logged in", "login");
					} else {
						return new Response(0, username + " is already logged in", "login");
					}
				}
			} else {
				return new Response(0, "Wrong password", "login");
			}
		}
	}

	// Metodo per gestire una richiesta di logout
	public Response logout(String username) {

		synchronized (loggedUsers) {

			// Controllo se l'username non è valido
			if (!loggedUsers.contains(username)) {
				return new Response(0, username + " is not logged in", "logout");
			} else {
				loggedUsers.remove(username);
				System.out.println("<- " + username + " logged out");
				return new Response(1, username + " logged out", "logout");
			}
		}
	}

	// Metodo per gestire una richiesta showFeed
	public Response showFeed(String username) {

		synchronized (Posts) {

			synchronized (Users) {

				// Controllo se l'utente segue qualcuno
				if (Users.get(username).getFollowing().isEmpty()) {
					return new Response(0, "Empty feed, try following someone", "showFeed");
				} else {

					// Preparazione della risposta
					StringBuilder risposta = new StringBuilder(
							" Id\t| Author\t| Title\n-------------------------------\n");
					ArrayList<Post> feed = new ArrayList<>();
					for (String followed : Users.get(username).getFollowing()) {

						if (Posts.containsKey(followed))
							feed.addAll(Posts.get(followed));
					}
					Collections.sort(feed);

					// Ciclo per ogni post dopo averli ordinati e li aggiungo alla risposta
					for (Post post : feed) {
						if (post.getRewinFrom() == null) {
							risposta.append(
									" " + post.getId() + "\t| " + post.getAuthor() + "\t| " + post.getTitle() + "\n");
						} else {
							risposta.append(" " + post.getId() + "\t| " + post.getAuthor() + " (rw: "
									+ post.getRewinFrom() + ")\t| " + post.getTitle() + "\n");
						}
					}
					return new Response(1, risposta.toString(), "showFeed");
				}
			}
		}
	}

	// Metodo per gestire una richiesta listUsers
	public Response listUsers(String username) {

		StringBuilder response = new StringBuilder(" Username\t| Tags\n-------------------------------");

		synchronized (Users) {

			// Recupero l'utente
			User user = Users.get(username);
			Set<String> kset = Users.keySet();

			// Ciclo per ogni utente registrato
			for (String key : kset) {

				if (key.equals(username))
					continue;
				ArrayList<String> tags = Users.get(key).getTags();
				int firstCommonTag = 1;

				// Ciclo per ogni tag dell'utente selezionato
				for (String t : tags) {

					// Controllo se è un tag in comune
					if (user.getTags().contains(t)) {
						if (firstCommonTag == 1) {
							response.append("\n " + key + "\t| ");
							firstCommonTag = 0;
						}
						response.append(t + " ");
					}
				}
			}
		}
		return new Response(1, response.toString(), "listUsers");
	}

	// Metodo per gestire una richiesta listFollowing
	public Response listFollowing(String username) {

		StringBuilder response = new StringBuilder("Followed users:\n");

		synchronized (Users) {

			ArrayList<String> following = Users.get(username).getFollowing();

			// Controllo se non segue nessuno
			if (following.isEmpty()) {
				return new Response(0, "You dont't follow anyone", "listFollowing");
			}

			// Ciclo per ogni utente seguito
			response.append(following.toString());
		}
		return new Response(1, response.toString(), "listFollowing");
	}

	// Metodo per gestire una richiesta register
	public Response register(String username, String password, ArrayList<String> tags) {

		StringBuilder risposta = new StringBuilder();
		int status = 1;

		// Esecuzione di controlli per verificare la validità dei dati forniti
		if (username.isBlank() || username.isEmpty()) {
			risposta.append("Invalid username\n");
			status = 0;
		}
		if (password.isBlank() || password.isEmpty()) {
			risposta.append("Invalid password\n");
			status = 0;
		}
		if (tags.isEmpty() || tags.size() > 5) {
			risposta.append("Invalid number of tags\n");
			status = 0;
		}

		// Controllo il numero dei tag
		Set<String> set = new HashSet<String>(tags);
		if (set.size() < tags.size()) {
			risposta.append("Duplicated tags are not ammitted\n");
			status = 0;
		}

		// Se i controlli sono andatio a buon fine
		if (status == 1) {

			synchronized (Users) {

				// Controllo se l'username non è disponibile
				if (Users.containsKey(username)) {
					risposta.append("Username already used\n");
					status = 0;
				} else {

					// Rendo i tags in lettere minuscole
					tags.replaceAll(String::toLowerCase);
					User newuser = new User(username, password.hashCode(), tags);
					Users.put(username, newuser);

					synchronized (Wallets) {

						// Aggiunta del wallet
						Wallets.put(username, new Wallet(username));
						risposta.append("Registered " + username);
					}
				}
			}
		}
		return new Response(status, risposta.toString(), "register");
	}

	// Metodo per gestire una richiesta follow
	public Response follow(String username, String tofollow) {

		// Controllo che non stia provando a seguire se stesso
		if (tofollow.equals(username)) {
			return new Response(0, "You can't follow yourself", "follow");
		}

		synchronized (Users) {

			// Controllo se l'utente esiste o se lo segue già
			if (!Users.containsKey(tofollow)) {
				return new Response(0, tofollow + " does not exist", "follow");
			}
			if (Users.get(tofollow).addFollower(username) == 0) {
				return new Response(0, "You already follow " + tofollow, "follow");
			}
			if (Users.get(username).addFollowed(tofollow) == 0) {
				return new Response(0, "You already follow " + tofollow, "follow");
			}
			return new Response(1, "Following " + tofollow, "follow");
		}
	}

	// Metodo per gestire una richiesta unfollow
	public Response unfollow(String username, String tounfollow) {

		// Controllo che non stia provando a smettere di seguire se stesso
		if (tounfollow.equals(username)) {
			return new Response(0, "non puoi smettere di seguire te stesso !", "unfollow");
		}

		synchronized (Users) {

			// Controllo se l'utente esiste o se non lo segue
			if (!Users.containsKey(tounfollow)) {
				return new Response(0, tounfollow + " does not exist", "unfollow");
			}
			if (Users.get(username).removeFollowed(tounfollow) == 0) {
				return new Response(0, "You don't follow " + tounfollow, "unfollow");
			}
			if (Users.get(tounfollow).removeFollower(username) == 0) {
				return new Response(0, "You don't follow " + tounfollow, "unfollow");
			}
		}
		return new Response(1, "You stopped following " + tounfollow, "unfollow");
	}

	// Metodo per gestire una richiesta createPost
	public Response createPost(String username, String title, String content) {

		// Controllo se titolo e contenuto rispettano le lunghezze richieste
		if (title.length() > 20 || title.isBlank() || title.isEmpty()) {
			return new Response(0, "Title can only have up to 20 characters", "createPost");
		}
		if (content.length() > 500 || content.isBlank() || content.isEmpty()) {
			return new Response(0, "Content can only have up to 500 characters", "createPost");
		}
		Post post = new Post(title, content, username, idPost.getAndIncrement(), 1);

		synchronized (Posts) {

			// Controllo se non è il primo post e aggiungo alla struttura
			if (Posts.containsKey(username)) {
				Posts.get(username).add(post);
			} else {
				ArrayList<Post> list = new ArrayList<Post>();
				list.add(post);
				Posts.put(username, list);
			}
		}
		return new Response(1, "Published post with id " + post.getId(), "createPost");
	}

	// Metodo per gestire una richiesta showPost
	public Response showPost(int id) {

		StringBuilder risposta = new StringBuilder();

		synchronized (Posts) {

			// Ciclo per ogni blog presente
			for (ArrayList<Post> blog : Posts.values()) {

				// Ciclo per ogni post nel blog
				for (Post post : blog) {

					// Controllo se si tratta del post cercato
					if (post.getId() == id) {
						risposta.append("Title: " + post.getTitle() + "\n" + "Content: " + post.getContent() + "\n"
								+ "Ratings: " + post.takeLikesNumber() + " likes, " + post.takeDislikesNumber()
								+ " dislikes\n" + post.takeCommentsAsString());

						return new Response(1, risposta.toString(), "showPost");
					}
				}
			}
			return new Response(0, "Post not present", "showPost");
		}
	}

	// Metodo per gestire una richiesta addComment
	public Response addComment(String username, int postId, String comment) {

		// Controllo se il commento è valido e se l'id del post potrebbe esistere
		if (comment.length() > 100 || comment.isBlank() || comment.isEmpty()) {
			return new Response(0, "Comment can only have up to 100 characters", "addComment");
		}
		if (postId > idPost.get()) {
			return new Response(0, "The post doesn't exit", "addComment");
		}

		synchronized (Posts) {

			// Ciclo per ogni blog presente
			for (ArrayList<Post> blog : Posts.values()) {

				// Ciclo per ogni post
				for (Post post : blog) {

					// Controllo se è il post cercato
					if (post.getId() == postId) {

						// Controllo se sta cercando di commentare un proprio post
						if (post.getAuthor().equals(username)) {
							return new Response(0, "You can't comment one of your posts", "addComment");
						}

						synchronized (Users) {

							// Controllo se non segue la persona che ha pubblicato il post
							if (!(Users.get(username).getFollowing().contains(post.getAuthor()))) {
								return new Response(0, "You can't comment a post wich is not in your feed",
										"addComment");
							}
						}
						post.addComment(username, comment);
						return new Response(1, "Comment added to post " + postId, "addComment");
					}
				}
			}
		}
		return new Response(0, "The post doesn't exit", "addComment");
	}

	// Metodo per gestire una richiesta ratePost
	public Response ratePost(String username, int postId, int rating) {

		// Controllo la validità dei rating e dell'id
		if (rating != -1 && rating != 1) {
			return new Response(0, "Insert only ratings (+1) or (-1)", "addComment");
		}
		if (postId > idPost.get()) {
			return new Response(0, "The post doesn't exits", "addComment");
		}

		synchronized (Posts) {

			// Ciclo per ogni blog
			for (ArrayList<Post> blog : Posts.values()) {

				// Ciclo per ogni post
				for (Post post : blog) {

					// Controllo se è il post cercato
					if (post.getId() == postId) {

						// Controllo se sta provando a valutare un proprio post
						if (post.getAuthor().equals(username)) {
							return new Response(0, "You can't rate one of your posts", "ratePost");
						}

						synchronized (Users) {

							// Controllo se non segue la persona che ha pubblicato il post
							if (!(Users.get(username).getFollowing().contains(post.getAuthor()))) {
								return new Response(0, "You can't comment a post wich is not in your feed",
										"addComment");
							}
						}

						// Aggiunta della valutazione
						if (rating == -1 && post.addDislike(username) == 1) {
							return new Response(1, "Added dislike to post " + postId, "ratePost");
						} else if (rating == 1 && post.addLike(username) == 1) {
							return new Response(1, "Added like to post " + postId, "ratePost");
						} else {
							return new Response(0, "You can't rate a post multiple times", "ratePost");
						}
					}
				}
			}
		}
		return new Response(0, "The post doesn't exist", "ratePost");
	}

	// Metodo per gestire una richiesta deletePost
	public Response deletePost(String username, int postId) {

		// Controllo la vlidità dell'id
		if (postId > idPost.get()) {
			return new Response(0, "The post doesn't exist", "deletePost");
		}

		synchronized (Posts) {

			// Ciclo per ogni blog
			for (ArrayList<Post> blog : Posts.values()) {

				// Ciclo per ogni post nel blog
				for (Post post : blog) {

					// Controllo se è il post cercato
					if (post.getId() == postId) {

						// Controllo se sta provando a cancellare un post non suo
						if (!post.getAuthor().equals(username)) {
							return new Response(0, "You can't delete other users posts", "deletePost");
						} else {
							blog.remove(post);
							if (blog.isEmpty())
								Posts.remove(post.getAuthor());
							return new Response(1, "Deleted post " + postId, "deletePost");
						}
					}
				}
			}
		}
		return new Response(0, "The post doesn't exist", "deletePost");
	}

	// Metodo per gestire una richiesta rewinPost
	public Response rewinPost(String username, int postId) {

		// Controllo la validità dell'id
		if (postId > idPost.get()) {
			return new Response(0, "The post doesn't exist", "rewinPost");
		}

		synchronized (Posts) {

			// Ciclo per ogni blog
			for (ArrayList<Post> blog : Posts.values()) {

				// Ciclo per ogni post nel blog
				for (Post post : blog) {

					// Controllo se è il post cercato
					if (post.getId() == postId) {

						// Controllo se sta provando a rewinnare un post suo
						if (post.getAuthor().equals(username) || post.getRewinFrom() != null) {
							return new Response(0, "You can't rewin this post", "rewinPost");
						}

						synchronized (Users) {

							// Controllo se appartiene al feed
							if (!(Users.get(username).getFollowing().contains(post.getAuthor()))) {
								return new Response(0, "You can't rewin a post wich is not in your feed", "rewinPost");
							} else {

								// Duplicazione del post rewinnato e pubblicazione
								Post newPost = new Post(post.getTitle(), post.getContent(), username,
										idPost.getAndIncrement(), post.getRewardCicle(), post.getAuthor());
								if (Posts.containsKey(username)) {
									Posts.get(username).add(newPost);
								} else {
									ArrayList<Post> list = new ArrayList<Post>();
									list.add(newPost);
									Posts.put(username, list);

								}
								return new Response(1, "Rewinned post " + postId, "rewinPost");
							}
						}
					}
				}
			}
		}
		return new Response(0, "The post doesn't exist", "rewinPost");
	}

	// Metodo per gestire una richiesta viewBlog
	public Response viewBlog(String username) {

		StringBuilder risposta = new StringBuilder(" Id\t| Author\t| Title\n-------------------------------\n");

		synchronized (Posts) {

			// Controllo se l'utente ha pubblicato post
			if (Posts.get(username) == null) {
				return new Response(0, "Your blog is empty", "viewBlog");
			}

			// Ciclo per ogni post pubblicato
			for (Post post : Posts.get(username)) {

				// Controllo se non si tratta di un rewin e provvedo ad aggiungere il post
				if (post.getRewinFrom() == null) {
					risposta.append(" " + post.getId() + "\t| " + post.getAuthor() + "\t| " + post.getTitle() + "\n");
				} else {
					risposta.append(" " + post.getId() + "\t| " + post.getAuthor() + " (rw: " + post.getRewinFrom()
							+ ")\t| " + post.getTitle() + "\n");
				}
			}
			return new Response(1, risposta.toString(), "viewBlog");
		}
	}

	// Metodo per gestire una richiesta getWallet
	public Response getWallet(String username) {

		synchronized (Wallets) {

			// Controllo se il wallet esiste
			if (Wallets.containsKey(username)) {

				// Preparazione della risposta
				StringBuilder risposta = new StringBuilder(
						"Total wincoins: " + Wallets.get(username).getWincoin() + "\nTransactions:");
				for (String t : Wallets.get(username).getTransactions()) {
					risposta.append("\n " + t);
				}
				return new Response(1, risposta.toString(), "getWallet");
			}
			return new Response(0, "Wallet not found", "getWallet");
		}
	}

	// Metodo per gestire una richiesta getWalletInBitcoin
	public Response getWalletInBitcoin(String username) {

		double changeBtc;
		HttpURLConnection http = null;
		try {

			// Richiesta per oteenere un numero casuale n da 1 a 100 (1 bitcoin = n wincoin)
			URL url = new URL(
					"https://www.random.org/integers/?num=1&min=100&max=1000&col=1&base=10&format=plain&rnd=new");
			http = (HttpURLConnection) url.openConnection();

			// Controllo il codice della risposta
			if (http.getResponseCode() == 200) {
				try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {

					// Converto il numero casuale ottenuto
					changeBtc = Double.parseDouble(responseReader.readLine());
				}
			} else {
				return new Response(0, "Incalculable btc change: http response code != 200", "getWalletInBitcoin");
			}
		} catch (IOException e) {
			// problemi con il sistema di cambio
			return new Response(0, "Incalculable btc change: exception catched", "getWalletInBitcoin");
		} finally {
			if (http != null)
				http.disconnect();
		}

		synchronized (Wallets) {

			// Controllo l'esistenza del wallet e ritorno del valore calcolato
			if (Wallets.containsKey(username)) {
				return new Response(1, "Total wincoins in btc: " + (Wallets.get(username).getWincoin() / changeBtc),
						"getWalletInBitcoin");
			}
		}
		return new Response(0, "Wallet not found", "getWalletInBitcoin");
	}

	// Metodo usato dall'oggetto RMI lato server per settare i followers dei client
	public ArrayList<String> getFollowers(String username) {

		// Preparazione e ritorno della lista dei followers
		ArrayList<String> followers = new ArrayList<String>();
		synchronized (Users) {
			followers.addAll(Users.get(username).getFollowers());
		}
		return followers;
	}

	// Metodo usato quando si recupera lo stato a partire da un backup
	public void setIdPost(int idPost) {
		this.idPost.set(idPost);
	}

	// Metodo usato quando si recupera lo stato a partire da un backup
	public void setUsers(ConcurrentHashMap<String, User> Users) {
		this.Users = Users;
	}

	// Metodo usato quando si recupera lo stato a partire da un backup
	public void setPosts(ConcurrentHashMap<String, ArrayList<Post>> Posts) {
		this.Posts = Posts;
	}

	// Metodo usato quando si recupera lo stato a partire da un backup
	public void setWallets(ConcurrentHashMap<String, Wallet> Wallets) {
		this.Wallets = Wallets;
	}

	// Metodo chiamato dal thread apposito per salvare lo stato del server
	public void backup() {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {

			// Salvataggio dell'idPost nell'apposito file
			File fIdPost = new File("BackupIdPost.json");
			objectMapper.writeValue(fIdPost, this.idPost.get());

			// Salvataggio degli users nell'apposito file
			File fUsers = new File("BackupUsers.json");
			synchronized (Users) {
				objectMapper.writeValue(fUsers, this.Users);
			}

			// Salvataggio dei post nell'apposito file
			File fPosts = new File("BackupPosts.json");
			synchronized (Posts) {
				objectMapper.writeValue(fPosts, this.Posts);
			}

			// Salvataggio degli wallets nell'apposito file
			File fWallets = new File("BackupWallets.json");
			synchronized (Wallets) {
				objectMapper.writeValue(fWallets, this.Wallets);
			}
		} catch (IOException e) {
			System.out.println("ERROR Problems during backup phase");
		}
	}

	// Metodo getter chiamato dall'oggetto RMI per sapere l'indirizzo multicast
	public String getMulticastAddress() {
		return config.getMulticast();
	}

	// Metodo getter chiamato dall'oggetto RMI per sapere la porta multicast
	public int getMulticastPort() {
		return config.getMcastport();
	}

	// Metodo per calcolare i vari pagamenti da effetture
	public void pay() {

		synchronized (Posts) {

			// Ciclo per ogni utente nella lista dei post
			for (String user : Posts.keySet()) {

				// Controllo se ha pubblicato qualcosa
				if (Posts.get(user) != null && !Posts.get(user).isEmpty()) {

					// Ciclo per ogni post presente in winsome
					for (Post post : Posts.get(user)) {

						// Lista degli utenti che guadagneranno da questo post
						ArrayList<String> contributors = new ArrayList<String>();

						// ------------------- COMMENTS -------------------

						// Variabile per il valore della sommatoria sui commenti
						int commentsSummation = 0;

						// Ciclo per ogni nuovo commento
						for (String commentator : post.getUnpaidComments().keySet()) {

							// Aggiunta dell'utente alla lista dei contributorie colcolo del contributo
							contributors.add(commentator);
							commentsSummation += 2 / (1 + Math.exp(-(post.takeCommentsAmountFrom(commentator) - 1)));

							// Spostamento del commento tra quelli pagati
							post.getPaidComments().putIfAbsent(commentator, new ArrayList<String>());
							post.getPaidComments().get(commentator).addAll(post.getUnpaidComments().get(commentator));
							post.getUnpaidComments().remove(commentator);
						}

						// Calcolo del contributo complessivo dei commenti
						double commentsGain = Math.log(commentsSummation + 1);

						// ------------------- LIKES -------------------

						// Variabile per il valore della sommatoria sulle valutazioni
						int ratingSummation = 0;

						// Ciclo per ogni valutazione non pagata
						for (String rater : post.getUnpaidRatings().keySet()) {

							// Incremento della sommatoria e aggiunta ai contributori
							ratingSummation += post.getUnpaidRatings().get(rater);
							if (post.getUnpaidRatings().get(rater) == 1 && !contributors.contains(rater)) {
								contributors.add(rater);
							}
						}

						// Spostamento delle valutazioni tra quelle pagate
						post.getPaidRatings().putAll(post.getUnpaidRatings());
						post.setUnpaidRatings(new HashMap<String, Integer>());

						// Calcolo del contributo complessivo delle valutazioni
						double likesGain = Math.log(Math.max(ratingSummation, 0) + 1);

						// ------------------- GAIN -------------------

						int rewardCicle = post.getRewardCicle();
						double totalGain = (likesGain + commentsGain) / rewardCicle;
						post.setRewardCicle(rewardCicle + 1);

						// Controllo se il post ha effettivamente generato guadagno
						if (totalGain != 0) {

							synchronized (Wallets) {

								// Pagamento dei contributori
								for (String contributor : contributors) {
									Wallets.get(contributor).addWincoin(
											totalGain * (1 - config.getAuthorpercentage() / 100) / contributors.size());
								}

								// Pagamento di chi ha creato il post (o il rewin)
								Wallets.get(user).addWincoin(totalGain * (config.getAuthorpercentage() / 100));
							}
						}
					}
				}
			}
		}
	}
}