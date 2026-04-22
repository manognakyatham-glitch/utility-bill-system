import java.io.Serializable;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String customerName;
    private double amount;
    private String dueDate;    // stored as "DD/MM/YYYY" text
    private boolean paid;
    private String description; // notes

    public Bill(int id, String customerName, double amount, String dueDate, boolean paid, String description) {
        this.id = id;
        this.customerName = customerName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paid = paid;
        this.description = description;
    }

    // Convenience constructor (keeps backwards compatibility)
    public Bill(int id, String customerName, double amount, String dueDate, boolean paid) {
        this(id, customerName, amount, dueDate, paid, "");
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void display() {
        System.out.println("ID: " + id);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Amount: " + amount);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Paid: " + paid);
        System.out.println("Description: " + description);
        System.out.println("---------------------------");
    }

    @Override
    public String toString() {
        return id + " | " + customerName + " | " + amount + " | " + dueDate + " | " + (paid ? "Paid" : "Unpaid") + " | " + description;
    }
}

