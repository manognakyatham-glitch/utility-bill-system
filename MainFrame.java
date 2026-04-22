import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private BillManager manager;
    private JTable table;
    private DefaultTableModel model;

    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JComboBox<String> sortCombo;

    private JLabel totalLabel, paidLabel, unpaidLabel;

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MainFrame() {
        manager = new BillManager();
        setTitle("Utility Bill Management System");
        setSize(980, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        refreshTable();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        setContentPane(root);

        // ---------- TOP ----------
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5));
        top.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        top.add(searchField);

        JButton searchBtn = new JButton("Search");
        top.add(searchBtn);

        top.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All","Paid","Unpaid"});
        top.add(filterCombo);

        top.add(new JLabel("Sort:"));
        sortCombo = new JComboBox<>(new String[]{
                "None","Amount ↑","Amount ↓","Due Date ↑","Due Date ↓"
        });
        top.add(sortCombo);

        JButton clearBtn = new JButton("Clear");
        top.add(clearBtn);
        root.add(top, BorderLayout.NORTH);

        // ---------- TABLE ----------
        String[] cols = {"ID","Name","Amount","Due Date","Status","Description"};
        model = new DefaultTableModel(cols,0){
            public boolean isCellEditable(int r,int c){ return false; }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setDefaultRenderer(Object.class,new BillRenderer());

        JTableHeader h = table.getTableHeader();
        h.setBackground(new Color(40,110,220));
        h.setForeground(Color.WHITE);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---------- BOTTOM ----------
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel btns = new JPanel();
        JButton addBtn = new JButton("Add Bill");
        JButton editBtn = new JButton("Edit Bill");
        JButton delBtn = new JButton("Delete Bill");
        JButton toggleBtn = new JButton("Toggle Paid / Unpaid");

        btns.add(addBtn);
        btns.add(editBtn);
        btns.add(delBtn);
        btns.add(toggleBtn);

        bottom.add(btns, BorderLayout.WEST);

        JPanel totals = new JPanel();
        unpaidLabel = new JLabel("Unpaid: ₹0");
        paidLabel = new JLabel("Paid: ₹0");
        totalLabel = new JLabel("Total: ₹0");
        totals.add(unpaidLabel);
        totals.add(paidLabel);
        totals.add(totalLabel);

        bottom.add(totals, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        // ---------- ACTIONS ----------
        searchBtn.addActionListener(e -> refreshTable());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedIndex(0);
            sortCombo.setSelectedIndex(0);
            refreshTable();
        });

        addBtn.addActionListener(e -> addBill());
        editBtn.addActionListener(e -> editBill());
        delBtn.addActionListener(e -> deleteBill());
        toggleBtn.addActionListener(e -> togglePaid());
    }

    // ---------- HELPERS ----------
    private LocalDate parseDate(String d){
        try { return LocalDate.parse(d.trim(), dtf); }
        catch(Exception e){ return null; }
    }

    private List<Bill> buildViewList(){
        String s = searchField.getText().toLowerCase();
        String f = filterCombo.getSelectedItem().toString();
        String sort = sortCombo.getSelectedItem().toString();

        List<Bill> view = new ArrayList<>();

        for(Bill b: manager.getBills()){
            if(f.equals("Paid") && !b.isPaid()) continue;
            if(f.equals("Unpaid") && b.isPaid()) continue;

            String status = b.isPaid() ? "paid" : "unpaid";

            if(!s.isEmpty()){
                if(!(b.getCustomerName().toLowerCase().contains(s) ||
                     b.getDescription().toLowerCase().contains(s) ||
                     b.getDueDate().toLowerCase().contains(s) ||
                     status.contains(s))) continue;
            }
            view.add(b);
        }

        Comparator<Bill> c = null;
        switch(sort){
            case "Amount ↑": c = Comparator.comparingDouble(Bill::getAmount); break;
            case "Amount ↓": c = Comparator.comparingDouble(Bill::getAmount).reversed(); break;
            case "Due Date ↑": c = Comparator.comparing(b -> parseDate(b.getDueDate()),
                    Comparator.nullsLast(Comparator.naturalOrder())); break;
            case "Due Date ↓": c = Comparator.comparing(
                    (Bill b)->parseDate(b.getDueDate()),
                    Comparator.nullsLast(Comparator.reverseOrder())); break;
        }
        if(c!=null) Collections.sort(view,c);
        return view;
    }

    private int getMainIndex(int viewRow){
        int id = buildViewList().get(viewRow).getId();
        for(int i=0;i<manager.getBills().size();i++)
            if(manager.getBills().get(i).getId()==id) return i;
        return -1;
    }

    private void refreshTable(){
        model.setRowCount(0);

        double total=0, paid=0, unpaid=0;
        for(Bill b: manager.getBills()){
            total+=b.getAmount();
            if(b.isPaid()) paid+=b.getAmount();
            else unpaid+=b.getAmount();
        }

        for(Bill b: buildViewList()){
            model.addRow(new Object[]{
                    b.getId(), b.getCustomerName(), b.getAmount(),
                    b.getDueDate(), b.isPaid()?"Paid":"Unpaid", b.getDescription()
            });
        }

        totalLabel.setText("Total: ₹"+total);
        paidLabel.setText("Paid: ₹"+paid);
        unpaidLabel.setText("Unpaid: ₹"+unpaid);
    }

    // ---------- BUTTON LOGIC ----------
    private void addBill(){
        JTextField n=new JTextField(), a=new JTextField(), d=new JTextField();
        JTextArea ds=new JTextArea(3,20);

        Object[] f={"Name:",n,"Amount:",a,"Due Date (DD/MM/YYYY):",d,
                "Description:",new JScrollPane(ds)};
        if(JOptionPane.showConfirmDialog(this,f,"Add Bill",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;

        manager.addBill(new Bill(0,n.getText(),
                Double.parseDouble(a.getText()),d.getText(),false,ds.getText()));
        refreshTable();
    }

    private void editBill(){
        int r=table.getSelectedRow();
        if(r==-1){ JOptionPane.showMessageDialog(this,"Select a bill to edit"); return; }
        int i=getMainIndex(r);
        Bill b=manager.getBills().get(i);

        JTextField n=new JTextField(b.getCustomerName());
        JTextField a=new JTextField(String.valueOf(b.getAmount()));
        JTextField d=new JTextField(b.getDueDate());
        JTextArea ds=new JTextArea(b.getDescription(),3,20);

        Object[] f={"Name:",n,"Amount:",a,"Due Date (DD/MM/YYYY):",d,
                "Description:",new JScrollPane(ds)};
        if(JOptionPane.showConfirmDialog(this,f,"Edit Bill",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;

        manager.editBill(i,new Bill(b.getId(),n.getText(),
                Double.parseDouble(a.getText()),d.getText(),b.isPaid(),ds.getText()));
        refreshTable();
    }

    private void deleteBill(){
        int r=table.getSelectedRow();
        if(r==-1){ JOptionPane.showMessageDialog(this,"Select a bill to delete"); return; }
        manager.deleteBill(getMainIndex(r));
        refreshTable();
    }

    private void togglePaid(){
        int r=table.getSelectedRow();
        if(r==-1){ JOptionPane.showMessageDialog(this,"Select a bill"); return; }
        int i=getMainIndex(r);
        Bill b=manager.getBills().get(i);
        b.setPaid(!b.isPaid());
        refreshTable();
    }

    // ---------- RENDERER ----------
    private class BillRenderer extends DefaultTableCellRenderer{
        public Component getTableCellRendererComponent(
                JTable t,Object v,boolean sel,boolean foc,int r,int c){
            Component comp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            Bill b=buildViewList().get(r);
            LocalDate due=parseDate(b.getDueDate());

            comp.setBackground(Color.WHITE);
            comp.setForeground(Color.BLACK);

            if(!b.isPaid() && due!=null && due.isBefore(LocalDate.now()))
                comp.setBackground(new Color(255,220,220)); // 🔴 overdue

            if(b.isPaid())
                comp.setForeground(new Color(0,130,0)); // 🟢 paid

            if(sel){
                comp.setBackground(new Color(60,130,230));
                comp.setForeground(Color.WHITE);
            }
            return comp;
        }
    }

    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        }catch(Exception e){}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
