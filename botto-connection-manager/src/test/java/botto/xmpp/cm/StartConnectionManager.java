package botto.xmpp.cm;

public class StartConnectionManager {

    public static void main(String[] args) {

        ConnectionManagerClient client = new ConnectionManagerClient("caprazzi.net", "localhost", 5262, "password");

        client.start();
    }
}
