package fungsi;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

/** Pemilihan satu pemeriksaan pengganti; data disimpan oleh layanan transaksi PA. */
public final class LabPAUbahDialog extends JDialog {
    private static final long serialVersionUID=1L;
    private final List<LabPAUbahPemeriksaan.Pilihan> items;
    private final LabPAUbahPemeriksaan.Data data;
    private final JTable table;
    private static final int SUDUT=10;
    private final JTextArea reason=new JTextArea(1,30);
    private LabPAUbahPemeriksaan.Pilihan pilihan;
    private final Color blue=new Color(32,102,205),line=new Color(208,218,232);
    private final NumberFormat number=NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));
    public LabPAUbahDialog(Window owner,LabPAUbahPemeriksaan.Data data,List<LabPAUbahPemeriksaan.Pilihan> items){
        super(owner,"Ubah Periksa Lab PA",ModalityType.APPLICATION_MODAL);this.items=items;this.data=data;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(0,12));root.setBackground(Color.WHITE);root.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));setContentPane(root);
        JPanel header=new JPanel(new GridLayout(0,1,0,5));header.setOpaque(false);
        header.add(label("Ubah Periksa Lab PA",18,true));
        JPanel identitas=new JPanel();
        identitas.setLayout(new BoxLayout(identitas,BoxLayout.X_AXIS));
        identitas.setOpaque(false);
        JLabel namaPasien=label(data.pasien,12,true);
        namaPasien.setToolTipText(data.pasien);
        namaPasien.setMinimumSize(new Dimension(0,namaPasien.getPreferredSize().height));
        JLabel infoPemeriksaan=label("•  No. Rawat "+data.kunci.noRawat+"  •  "+data.kunci.tanggal
                +"  •  "+data.kunci.jam+"  •  "+LabPAData.namaJenis(data.jenis),12,false);
        infoPemeriksaan.setMinimumSize(infoPemeriksaan.getPreferredSize());
        identitas.add(namaPasien);
        identitas.add(Box.createHorizontalStrut(8));
        identitas.add(infoPemeriksaan);
        identitas.add(Box.createHorizontalGlue());
        header.add(identitas);
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
        JTextField search=new JTextField();
        search.setUI(new BasicTextFieldUI());
        search.setFont(font(13,false));
        search.setToolTipText("Cari kode atau nama pemeriksaan pengganti");
        styleInput(search,search);
        search.getDocument().addDocumentListener(new DocumentListener(){
            private void filter(){String s=search.getText().trim();sorter.setRowFilter(s.isEmpty()?null:RowFilter.regexFilter("(?i)"+Pattern.quote(s),0,1));}
            @Override public void insertUpdate(DocumentEvent e){filter();}@Override public void removeUpdate(DocumentEvent e){filter();}@Override public void changedUpdate(DocumentEvent e){filter();}
        });
        JPanel find=new JPanel(new BorderLayout(8,0));find.setOpaque(false);find.add(label("Cari pengganti",12,true),BorderLayout.WEST);find.add(search,BorderLayout.CENTER);
        center.add(find,BorderLayout.NORTH);JScrollPane scroll=new JScrollPane(table);scroll.setBorder(BorderFactory.createLineBorder(line));center.add(scroll,BorderLayout.CENTER);
        root.add(center,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(0,8));bottom.setOpaque(false);
        JPanel reasonPanel=new JPanel(new BorderLayout(0,5));reasonPanel.setOpaque(false);reasonPanel.add(label("Alasan perubahan",12,true),BorderLayout.NORTH);
        reasonPanel.add(buatKolomAlasan(reasonPanel),BorderLayout.CENTER);
        bottom.add(reasonPanel,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        actions.setOpaque(false);
        JButton save=button("Simpan Perubahan",blue);
        save.addActionListener(e->simpan());
        JButton cancel=button("Batal",new Color(190,45,58));
        cancel.addActionListener(e->dispose());
        actions.add(save);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(cancel);
        bottom.add(actions,BorderLayout.SOUTH);
        root.add(bottom,BorderLayout.SOUTH);
        setMinimumSize(new Dimension(680,540));setSize(850,650);setLocationRelativeTo(owner);
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){search.requestFocusInWindow();}});
    }
    private Font font(int size,boolean bold){return new Font(bold?"Segoe UI Semibold":"Segoe UI",bold?Font.BOLD:Font.PLAIN,size);}
    private JLabel label(String text,int size,boolean bold){JLabel l=new JLabel(text);l.setFont(font(size,bold));l.setForeground(new Color(37,52,74));return l;}
    private void styleInput(JComponent bingkai,JTextComponent input){
        bingkai.setOpaque(false);
        input.setOpaque(false);
        bingkai.setBorder(new AbstractBorder(){
            private static final long serialVersionUID=1L;
            @Override public Insets getBorderInsets(Component c,Insets insets){
                insets.set(6,9,6,9);
                return insets;
            }
            @Override public void paintBorder(Component c,Graphics graphics,int x,int y,int width,int height){
                Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(input.isFocusOwner()?blue:line);
                g.setStroke(new BasicStroke(input.isFocusOwner()?1.5f:1f));
                g.drawRoundRect(x+1,y+1,width-3,height-3,SUDUT,SUDUT);
                g.dispose();
            }
        });
        input.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){bingkai.repaint();}
            @Override public void focusLost(FocusEvent e){bingkai.repaint();}
        });
    }
    private JScrollPane buatKolomAlasan(JPanel reasonPanel){
        reason.setUI(new BasicTextAreaUI());
        reason.setFont(font(13,false));
        reason.setLineWrap(true);
        reason.setWrapStyleWord(true);
        reason.setBorder(BorderFactory.createEmptyBorder());
        JScrollPane scroll=new JScrollPane(reason,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER){
            private static final long serialVersionUID=1L;
            @Override public Dimension getPreferredSize(){
                Dimension size=super.getPreferredSize();
                Insets insets=getInsets();
                int lebar=Math.max(1,(getWidth()>0?getWidth():size.width)-insets.left-insets.right);
                View view=reason.getUI().getRootView(reason);
                view.setSize(lebar,Integer.MAX_VALUE);
                int satuBaris=reason.getFontMetrics(reason.getFont()).getHeight();
                int tinggiTeks=(int)Math.ceil(view.getPreferredSpan(View.Y_AXIS));
                // Sisakan ruang untuk tabel saat teks yang sangat panjang ditempel.
                int tinggi=Math.max(satuBaris,Math.min(tinggiTeks,8*satuBaris));
                return new Dimension(size.width,tinggi+insets.top+insets.bottom);
            }
        };
        scroll.setViewportBorder(null);
        scroll.getViewport().setOpaque(false);
        styleInput(scroll,reason);
        // Perbarui tata letak footer, karena JScrollPane punya batas validasi sendiri.
        reason.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){reasonPanel.revalidate();}
            @Override public void removeUpdate(DocumentEvent e){reasonPanel.revalidate();}
            @Override public void changedUpdate(DocumentEvent e){reasonPanel.revalidate();}
        });
        scroll.addComponentListener(new ComponentAdapter(){
            @Override public void componentResized(ComponentEvent e){reasonPanel.revalidate();}
        });
        return scroll;
    }
    private JButton button(String text,Color warna){
        JButton b=new JButton(text){
            private static final long serialVersionUID=1L;
            @Override protected void paintComponent(Graphics graphics){
                Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getModel().isPressed()||getModel().isRollover()?warna.darker():warna);
                g.fillRoundRect(0,0,getWidth(),getHeight(),SUDUT,SUDUT);
                if(isFocusOwner()){
                    g.setColor(Color.WHITE);
                    g.drawRoundRect(2,2,getWidth()-5,getHeight()-5,SUDUT-2,SUDUT-2);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        b.setUI(new BasicButtonUI());
        b.setFont(font(12,true));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setBorder(BorderFactory.createEmptyBorder(9,16,9,16));
        return b;
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
