package pgdiagram;

/**
 * Created by sdv on 22/07/15.
 */
public class DBEntry {
    String description;
    String url;
    String user;
    String password_encrypt;


    public DBEntry(String description, String url, String user, String password_encrypt) {
        this.description = description;
        this.url = url;
        this.user = user;
        this.password_encrypt = password_encrypt;
    }
}

