import com.jayway.jsonpath.JsonPath;
import org.jasonjson.core.JsonArray;
import org.jasonjson.core.JsonObject;
import org.jasonjson.core.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FabrickBankAccount {

    //metodi
    //metodo per la lettura del saldo
    public void getBalance(String accountID) throws IOException {
        //indirizzo dal quale saranno prelevati i dati
        String address = "https://sandbox.platfr.io/api/gbs/banking/v4.0/accounts/" + accountID +
                "/balance";
        //creazione dell URL che identifica l'indirizzo dal quale verranno prelevati i dati
        URL url = new URL(address);
        //creazione di una connessione all URL e setting dei pararametri
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Api-Key", "FXOVVXXHVCPVPBZXIJOBGUGSKHDNFRRQJP");
        connection.setRequestProperty("Auth-Schema", "S2S");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException(connection.getResponseMessage());
        }

        //Apertura di uno stream per la lettura dei dati
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        BufferedReader input = new BufferedReader(reader);
        String response = "";

        String output;
        //finchè ci sono ancora dati, legge dallo stream
        while ((output = input.readLine()) != null) {
            response += output;
        }
        //parsing documento Json in Java e prelievo dei valori che ci interessano
        JsonObject data = new JsonParser().parse(response).getAsJsonObject();
        JsonObject object = (JsonObject) data.get("payload");
        System.out.println("Saldo conto: " + object.get("balance"));

        connection.disconnect();
    }

    //metodo per la stampa della lista movimenti
    public void getTransactionList(String accountID, String fromAccountingDate, String toAccountingDate) throws IOException {
        //indirizzo dal quale saranno prelevati i dati
        String address = "https://sandbox.platfr.io/api/gbs/banking/v4.0/accounts/" + accountID +
                "/transactions?fromAccountingDate=" +
                fromAccountingDate +
                "&toAccountingDate=" +
                toAccountingDate;
        //creazione dell URL che identifica l'indirizzo dal quale verranno prelevati i dati e che verrà utilizzato per aprire una connessione
        URL url = new URL(address);
        //creazione di una connessione all URL e setting dei pararametri
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Api-Key", "FXOVVXXHVCPVPBZXIJOBGUGSKHDNFRRQJP");
        connection.setRequestProperty("Auth-Schema", "S2S");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException(connection.getResponseMessage());
        }
        //Apertura di uno stream per la lettura dei dati
        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        BufferedReader input = new BufferedReader(reader);
        String response = "";

        String output;
        //finchè ci sono ancora dati, legge dallo stream
        while ((output = input.readLine()) != null) {
            response += output;
        }
        //parsing documento Json e prelievo dei valori che ci interessano
        JsonObject data = new JsonParser().parse(response).getAsJsonObject();
        JsonObject object = (JsonObject) data.get("payload");
        //System.out.println(object);
        JsonArray list = (JsonArray) object.get("list");
        //scansione della lista dei valori
        for (int index = 0; index < list.size(); index++) {
            Object o = list.get(index);
            if (o instanceof JsonObject) {
                System.out.println("Operazione del giorno " + ((JsonObject) o).get("valueDate") + ": " + ((JsonObject) o).get("amount") + " " + ((JsonObject) o).get("currency"));
            }
        }
        connection.disconnect();
    }

    //metodo per effettuare un bonifico
    public void makeTransfer(String accountID, String receiverName, String description, String currency, String amount, String executionDate) throws IOException {
        try{
            //indirizzo dal quale saranno prelevati i dati
            String address = "https://sandbox.platfr.io/api/gbs/banking/v4.0/accounts/" + accountID + "/payments/money-transfers";
            //creazione dell URL che identifica l'indirizzo dal quale verranno prelevati i dati che verrà utilizzato per aprire una connessione
            URL url = new URL(address);
            //creazione di una connessione all >URL e setting dei parametri
            HttpURLConnection connection = (HttpURLConnection)  url.openConnection();
            //per le richieste POST bisogna abilitare anche la scrittura sullo stream di connessioni
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Auth-Schema", "S2S");
            connection.setRequestProperty("Api-Key", "FXOVVXXHVCPVPBZXIJOBGUGSKHDNFRRQJP");
            connection.setRequestProperty("X-Time-Zone", "Europe/Rome");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String accountCode = "IT23A0336844430152923804660";
            String bitCode = "SELBIT2BXXX";

            // POST prevede la creazione di un body di request
            String body = "{\r\n" + "\"creditor\":{\r\n" + "\"name\":\"" + receiverName +"\",\r\n" +
                    "\"account\":{\r\n" + "\"accountCode\":\""+accountCode+"\",\r\n" +
                    "\"bicCode\": \""+bitCode+"\"\r\n" + "}\r\n" + "},\r\n" +
                    "\"executionDate\":\"" + executionDate + "\",\r\n" +
                    "\"description\":\"" + description + "\",\r\n" +
                    "\"amount\":"+ amount +",\r\n" + "\"currency\":\""+ currency + "\"\r\n" + "}";
            //Scrittura di body su OutputStream
            OutputStream out = connection.getOutputStream();
            out.write(body.getBytes());
            out.close();

            if(connection.getResponseCode() == 400) {
                InputStream error = connection.getErrorStream();
                String response = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(error));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                //parsing documento Json e prelievo dei valori che ci interessano
                JsonObject data = new JsonParser().parse(response).getAsJsonObject();
                //System.out.println(data);
                JsonArray list = (JsonArray) data.get("errors");
                for (int index = 0; index < list.size(); index++) {
                    Object o = list.get(index);
                    if (o instanceof JsonObject) {
                        System.out.println("Errore : " + ((JsonObject) o).get("code") + " " + ((JsonObject)o).get("description"));
                    }
                }
            } else if (connection.getResponseCode() != 200 && connection.getResponseCode() != 400) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + connection.getResponseCode());
            } else {
                //Apertura di uno stream per la lettura dei dati
                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                BufferedReader buffer = new BufferedReader(in);
                String output;
                while ((output = buffer.readLine()) != null) {
                    System.out.println(output);
                }
            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
        }
    }
}




