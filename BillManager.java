import java.io.*;
import java.util.ArrayList;

/**
 * BillManager with serialization to bills.dat
 * - Loads in constructor
 * - Saves automatically on modifying operations
 * - Reassigns IDs to be consecutive after delete/load
 * - Exports human-readable report
 */
public class BillManager {
    private ArrayList<Bill> bills;
    private final File dataFile;

    public BillManager() {
        this.dataFile = new File("bills.dat");
        this.bills = new ArrayList<>();
        loadFromFile();
    }

    public ArrayList<Bill> getBills() {
        return bills;
    }

    public void addBill(Bill bill) {
        // assign id as size+1 to keep consecutive
        bill.setId(bills.size() + 1);
        bills.add(bill);
        saveToFile();
    }

    public void editBill(int index, Bill newBill) {
        if (index >= 0 && index < bills.size()) {
            int existingId = bills.get(index).getId();
            newBill.setId(existingId);
            bills.set(index, newBill);
            saveToFile();
        }
    }

    public void deleteBill(int index) {
        if (index >= 0 && index < bills.size()) {
            bills.remove(index);
            reassignIds();
            saveToFile();
        }
    }

    public void markPaid(int index) {
        if (index >= 0 && index < bills.size()) {
            bills.get(index).setPaid(true);
            saveToFile();
        }
    }

    private void reassignIds() {
        for (int i = 0; i < bills.size(); i++) {
            bills.get(i).setId(i + 1);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!dataFile.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                bills = (ArrayList<Bill>) obj;
                reassignIds();
            }
        } catch (Exception e) {
            System.err.println("Warning: could not load bills.dat (" + e.getMessage() + "). Starting with empty list.");
            bills = new ArrayList<>();
        }
    }

    private void saveToFile() {
        File tmp = new File(dataFile.getAbsolutePath() + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp))) {
            oos.writeObject(bills);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
            return;
        }

        if (dataFile.exists()) dataFile.delete();
        tmp.renameTo(dataFile);
    }

    // Export to a simple text report (human readable)
    public boolean exportToText(String filename) {
        File out = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
            pw.println("ID\tName\tAmount\tDue Date\tStatus\tDescription");
            for (Bill b : bills) {
                pw.printf("%d\t%s\t%.2f\t%s\t%s\t%s%n",
                        b.getId(),
                        b.getCustomerName(),
                        b.getAmount(),
                        b.getDueDate(),
                        b.isPaid() ? "Paid" : "Unpaid",
                        b.getDescription().replace("\n", " "));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Export failed: " + e.getMessage());
            return false;
        }
    }
}


