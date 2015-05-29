
package ie.teamchile.smartapp.retrofit;

/**
 * Created by user on 5/26/15.
 */

public class PostingData {
   public Creds login;

    public PostingData(String username, String password) {
        this.login = new Creds(username, password);
    }

    private class Creds {
        private String username;
        private String password;

        public Creds(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}