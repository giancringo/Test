import java.io.IOException;

public class FabrickBankAccountTest {
    public static void main (String[] args) throws IOException {

        FabrickBankAccount account = new FabrickBankAccount();

        account.getBalance("14537780");
        account.getTransactionList("14537780", "2019-01-01", "2019-12-01");
        account.makeTransfer("14537780", "Gianmarco", "monthly rent", "EUR", "500", "2019-05-02");
    }
}
