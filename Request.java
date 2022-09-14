// ------------------- Marchi Davide 602476 -------------------

// Classe per le richieste che il client invierà al server convertite in json
public class Request {

	// Campo per contenere il tipo di richiesta
	private String requestType;

	// Login, logout
	private String username;
	private String password;

	// FollowUser, unfollowUser
	private String idUser;

	// ShowPost, deletePost, rewinPost, ratePost, addComment
	private int idPost;
	private int rating;
	private String comment;

	// CreatePost
	private String title;
	private String content;

	// Costruttore vuoto
	Request() {
	}

	// Costruttore per richieste senza parametri come blog
	Request(String requestType) {

		this.requestType = requestType;
	}

	// Costruttore per richieste di login o per postare un nuovo post
	Request(String requestType, String string1, String string2) {

		this.requestType = requestType;

		// Controllo di quale richiesta si tratti
		if (this.requestType.equals("login")) {
			this.username = string1;
			this.password = string2;
		} else {
			this.title = string1;
			this.content = string2;
		}
	}

	// Copstruttore per richieste di follow, unfollow e logout
	Request(String requestType, String username) {

		this.requestType = requestType;

		// Controllo di quale richiesta si tratti
		if (this.requestType.equals("follow") || this.requestType.equals("unfollow")) {
			this.idUser = username;
		} else {
			this.username = username;
		}
	}

	// Costruttore per richieste come delete o show post
	Request(String requestType, int int1) {

		this.requestType = requestType;
		this.idPost = int1;
	}

	// Csotruttore per richieste di tipo rate
	Request(String requestType, int idPost, int rating) {

		this.requestType = requestType;
		this.idPost = idPost;
		this.rating = rating;
	}

	// Costruttore per richieste di comment
	Request(String requestType, int idPost, String comment) {

		this.requestType = requestType;
		this.idPost = idPost;
		this.comment = comment;
	}

	// Metodo setter standard
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	// Metodo setter standard
	public void setUsername(String username) {
		this.username = username;
	}

	// Metodo setter standard
	public void setPassword(String password) {
		this.password = password;
	}

	// Metodo setter standard
	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}

	// Metodo setter standard
	public void setIdPost(int idPost) {
		this.idPost = idPost;
	}

	// Metodo setter standard
	public void setRating(int rating) {
		this.rating = rating;
	}

	// Metodo setter standard
	public void setComment(String comment) {
		this.comment = comment;
	}

	// Metodo setter standard
	public void setTitle(String title) {
		this.title = title;
	}

	// Metodo setter standard
	public void setContent(String content) {
		this.content = content;
	}

	// Metodo getter standard
	public String getRequestType() {
		return this.requestType;
	}

	// Metodo getter standard
	public String getUsername() {
		return this.username;
	}

	// Metodo getter standard
	public String getPassword() {
		return this.password;
	}

	// Metodo getter standard
	public String getTitle() {
		return this.title;
	}

	// Metodo getter standard
	public String getContent() {
		return this.content;
	}

	// Metodo getter standard
	public String getComment() {
		return this.comment;
	}

	// Metodo getter standard
	public String getIdUser() {
		return this.idUser;
	}

	// Metodo getter standard
	public int getIdPost() {
		return this.idPost;
	}

	// Metodo getter standard
	public int getRating() {
		return this.rating;
	}
}