package simrskhanza;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import static simrskhanza.frmUtama.versionlocal;

public class main extends javax.swing.JFrame {
    private static final int MAX_PROGRESS = 100;
    
    public main() {
        initComponents();
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/picture/yaski24.png")).getImage());
        new RealLoadingWorker().execute();
    }
    
    private class RealLoadingWorker extends SwingWorker<Void, Integer> {
        private int progress = 0;

        @Override
        protected Void doInBackground() throws Exception {
            while (progress < MAX_PROGRESS) {
                // Simulasikan pekerjaan nyata
                performRealTask(); 
                publish(progress);
                progress++;
            }
            return null;
        }

        private void performRealTask() throws InterruptedException {
            // Simulasi proses aktual (bisa diganti koneksi DB atau load file)
            Thread.sleep(5); 
        }
        
        @Override
        protected void process(List<Integer> chunks) {
            int latestProgress = chunks.get(chunks.size() - 1);
            progressBar.setValue(latestProgress);
            loadingnum.setText(latestProgress + "%");
        }

        @Override
        protected void done() {
            frmUtama utama = frmUtama.getInstance();
            utama.isWall();
            utama.setVisible(true);
            dispose();
        }
    }
    
    
    
//package simrskhanza;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.List;
//import javax.swing.ImageIcon;
//import javax.swing.SwingWorker;
//import javax.swing.Timer;
//import usu.widget.util.WidgetUtilities;
//
//public class main extends javax.swing.JFrame {
// Timer timer;
// ActionListener action;
// int a = 0;
//    
//    public main() {
//        initComponents();
//        setLocationRelativeTo(this);
//        setIconImage(new ImageIcon(super.getClass().getResource("/picture/yaski24.png")).getImage());
//        BackgroundWorker bw =new BackgroundWorker();
//        bw.execute();
//        
//    }
//    
//    private class BackgroundWorker extends SwingWorker<String, Integer>{
//        @Override
//        protected String doInBackground() throws Exception {
//            frmUtama utama=new frmUtama();
//            while( a <100){
//                    try{
//                   Thread.sleep(20);
//                    publish(a);
//                   a+=6;
//                }catch(Exception e){}
//                }
//                    utama.isWall();
//                    utama.setVisible(true);
//                    dispose();
//            return "finished";
//        }
//        @Override
//        protected void process(List<Integer> chunks) {
//            progressBar.setValue(chunks.get(chunks.size()-1));
//        }
// }
//
// public void main(String args[]) {
//        new SwingWorker<Void, Integer>(){
//                @Override
//                public Void doInBackground(){
//                    frmUtama utama=frmUtama.getInstance();
//                    utama.isWall();
//                    utama.setVisible(true);
//                    dispose();
//                    return null;
//                }
//                @Override
//                public void done(){
//                }
//                @Override
//                protected void process(List<Integer> ints){
//                    progressBar.setValue(progressBar.getValue() + 3);
//                }
//
//
//            }.execute();
//    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        loadingnum1 = new javax.swing.JLabel();
        loadingnum = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 350));
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(600, 350));
        setType(java.awt.Window.Type.POPUP);
        getContentPane().setLayout(null);

        jPanel1.setBackground(new java.awt.Color(0, 156, 215));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("RS. Akademis Jaury Jusuf Putera");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel1.add(jLabel4);
        jLabel4.setBounds(70, 130, 470, 50);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("Sistem Informasi Managemen");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel1.add(jLabel5);
        jLabel5.setBounds(70, 110, 200, 30);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("Selamat datang di");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel1.add(jLabel6);
        jLabel6.setBounds(70, 50, 200, 30);

        jLabel3.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Powered by SIMKES Khanza");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel1.add(jLabel3);
        jLabel3.setBounds(330, 170, 170, 20);

        loadingnum1.setBackground(new java.awt.Color(0, 156, 215));
        loadingnum1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        loadingnum1.setForeground(new java.awt.Color(255, 255, 255));
        loadingnum1.setText("Sedang memuat data ...");
        jPanel1.add(loadingnum1);
        loadingnum1.setBounds(50, 270, 150, 30);

        loadingnum.setBackground(new java.awt.Color(0, 156, 215));
        loadingnum.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        loadingnum.setForeground(new java.awt.Color(255, 255, 255));
        loadingnum.setText("100%");
        jPanel1.add(loadingnum);
        loadingnum.setBounds(190, 270, 50, 30);

        progressBar.setBackground(new java.awt.Color(204, 204, 204));
        progressBar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        progressBar.setForeground(new java.awt.Color(51, 204, 0));
        progressBar.setToolTipText("Loading");
        progressBar.setBorderPainted(false);
        progressBar.setFocusable(false);
        progressBar.setName("Loading"); // NOI18N
        progressBar.setOpaque(true);
        progressBar.setPreferredSize(new java.awt.Dimension(146, 10));
        jPanel1.add(progressBar);
        progressBar.setBounds(50, 297, 500, 10);
        progressBar.getAccessibleContext().setAccessibleName("Loading");
        progressBar.getAccessibleContext().setAccessibleParent(progressBar);

        getContentPane().add(jPanel1);
        jPanel1.setBounds(0, 0, 600, 350);

        getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JLabel loadingnum;
    public javax.swing.JLabel loadingnum1;
    public javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
