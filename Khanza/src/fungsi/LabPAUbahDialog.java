package fungsi;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.*;

/** Pemilihan satu pemeriksaan pengganti; data disimpan oleh layanan transaksi PA. */
public final class LabPAUbahDialog extends JDialog {
    private static final long serialVersionUID=1L;
    private final List<LabPAUbahPemeriksaan.Pilihan> items;
    private final LabPAUbahPemeriksaan.Data data;
    private final JTable table;
    private final JTextArea reason=new JTextArea(3,30);
    private LabPAUbahPemeriksaan.Pilihan pilihan;
    private final Color blue=new Color(32,102,205),line=new Color(208,218,232);
    private final NumberFormat number=NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));
    public LabPAUbahDialog(Window owner,LabPAUbahPemeriksaan.Data data,List<LabPAUbahPemeriksaan.Pilihan> items){
        super(owner,"Ubah Periksa Lab PA",ModalityType.APPLICATION_MODAL);this.items=items;this.data=data;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(0,12));root.setBackground(Color.WHITE);root.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));setContentPane(root);
        JPanel header=new JPanel(new GridLayout(0,1,0,5));header.setOpaque(false);
        header.add(label("Ubah Periksa Lab PA",18,true));header.add(label(data.pasien+"  •  "+data.kunci.noRawat,12,true));
        header.add(label(data.kunci.tanggal+"  "+data.kunci.jam+"  |  "+LabPAData.namaJenis(data.jenis),12,false));
        header.add(label("Lama: "+data.kunci.kode+" — "+data.nama+"  |  Rp "+number.format(data.biaya),12,false));
        header.add(label("No. PA: "+(data.noPA.isEmpty()?"Belum diterbitkan":data.noPA)+"  •  Penggantian hanya sesama jenis PA",12,false));
        root.add(header,BorderLayout.NORTH);
        DefaultTableModel model=new DefaultTableModel(new Object[]{"Kode","Pemeriksaan pengganti","Jenis","Tarif"},0){
            private static final long serialVersionUID=1L;
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        for(LabPAUbahPemeriksaan.Pilihan item:items)model.addRow(new Object[]{item.kode,item.nama,item.jenis,"Rp "+number.format(item.biaya)});
        table=new JTable(model);table.setFont(font(12,false));table.setRowHeight(34);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(228,239,255));table.setSelectionForeground(blue);table.setShowGrid(false);
        table.getTableHeader().setFont(font(12,true));table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);table.getColumnModel().getColumn(3).setPreferredWidth(130);
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(model);table.setRowSorter(sorter);
        JPanel center=new JPanel(new BorderLayout(0,8));center.setOpaque(false);
        JTextField search=new JTextField();search.setFont(font(13,false));search.setToolTipText("Cari kode atau nama pemeriksaan pengganti");style(search);
        search.getDocument().addDocumentListener(new DocumentListener(){
            private void filter(){String s=search.getText().trim();sorter.setRowFilter(s.isEmpty()?null:RowFilter.regexFilter("(?i)"+Pattern.quote(s),0,1));}
            @Override public void insertUpdate(DocumentEvent e){filter();}@Override public void removeUpdate(DocumentEvent e){filter();}@Override public void changedUpdate(DocumentEvent e){filter();}
        });
        JPanel find=new JPanel(new BorderLayout(8,0));find.setOpaque(false);find.add(label("Cari pengganti",12,true),BorderLayout.WEST);find.add(search,BorderLayout.CENTER);
        center.add(find,BorderLayout.NORTH);JScrollPane scroll=new JScrollPane(table);scroll.setBorder(BorderFactory.createLineBorder(line));center.add(scroll,BorderLayout.CENTER);
        root.add(center,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(0,8));bottom.setOpaque(false);
        JPanel reasonPanel=new JPanel(new BorderLayout(0,5));reasonPanel.setOpaque(false);reasonPanel.add(label("Alasan perubahan",12,true),BorderLayout.NORTH);
        reason.setFont(font(13,false));reason.setLineWrap(true);reason.setWrapStyleWord(true);style(reason);reasonPanel.add(new JScrollPane(reason),BorderLayout.CENTER);
        bottom.add(reasonPanel,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
        JButton cancel=button("Batal",false);cancel.addActionListener(e->dispose());
        JButton save=button("Simpan Perubahan",true);save.addActionListener(e->simpan());
        actions.add(cancel);actions.add(save);bottom.add(actions,BorderLayout.SOUTH);root.add(bottom,BorderLayout.SOUTH);
        setMinimumSize(new Dimension(680,540));setSize(850,650);setLocationRelativeTo(owner);
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){search.requestFocusInWindow();}});
    }
    private Font font(int size,boolean bold){return new Font(bold?"Segoe UI Semibold":"Segoe UI",bold?Font.BOLD:Font.PLAIN,size);}
    private JLabel label(String text,int size,boolean bold){JLabel l=new JLabel(text);l.setFont(font(size,bold));l.setForeground(new Color(37,52,74));return l;}
    private void style(JComponent c){
        Border normal=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(line),BorderFactory.createEmptyBorder(7,9,7,9));
        Border active=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(blue),BorderFactory.createEmptyBorder(7,9,7,9));
        c.setBorder(normal);c.addFocusListener(new FocusAdapter(){@Override public void focusGained(FocusEvent e){c.setBorder(active);}@Override public void focusLost(FocusEvent e){c.setBorder(normal);}});
    }
    private JButton button(String text,boolean primary){
        JButton b=new JButton(text);b.setFont(font(12,true));b.setBackground(primary?blue:Color.WHITE);b.setForeground(primary?Color.WHITE:blue);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary?blue:line),BorderFactory.createEmptyBorder(9,16,9,16)));return b;
    }
    private void simpan(){
        if(table.getSelectedRow()<0){JOptionPane.showMessageDialog(this,"Pilih pemeriksaan pengganti terlebih dahulu.");return;}
        String alasan=reason.getText().trim();
        if(alasan.isEmpty()||alasan.length()>500){JOptionPane.showMessageDialog(this,"Isi alasan perubahan, maksimal 500 karakter.");reason.requestFocusInWindow();return;}
        LabPAUbahPemeriksaan.Pilihan item=items.get(table.convertRowIndexToModel(table.getSelectedRow()));
        String message="Pemeriksaan lama: "+data.kunci.kode+" — "+data.nama+"\nPengganti: "+item.kode+" — "+item.nama
                +"\nTarif: Rp "+number.format(data.biaya)+" → Rp "+number.format(item.biaya)
                +"\nNo. PA tetap: "+(data.noPA.isEmpty()?"belum diterbitkan":data.noPA)
                +"\n\nSimpan perubahan pemeriksaan, order terkait, dan jurnal selisih tarif?";
        if(JOptionPane.showConfirmDialog(this,message,"Konfirmasi Ubah Periksa PA",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        pilihan=item;dispose();
    }
    public LabPAUbahPemeriksaan.Pilihan getPilihan(){return pilihan;}
    public String getAlasan(){return reason.getText().trim();}
}
