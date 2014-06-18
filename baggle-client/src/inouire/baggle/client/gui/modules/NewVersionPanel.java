package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.threads.MasterServerHTTPConnection;
import inouire.basics.SimpleLog;


public class NewVersionPanel extends JPanel{
    
    private JLabel info_label = new JLabel();
    private JButton download_button = new JButton();
    
    public NewVersionPanel() {
        super();
        
        info_label.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/update.png")));
        
        
        download_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/download.png")));
        download_button.setPreferredSize(new Dimension(25,25));
        download_button.setMaximumSize(new Dimension(25,25));
        download_button.setOpaque(false);
        download_button.setToolTipText(Language.getString(76));
        download_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadNewVersion();
            }
        });

        this.setLayout(new BorderLayout());
        this.setBackground(ColorFactory.YELLOW_NOTIF);
        this.add(download_button,BorderLayout.EAST);
        this.add(info_label,BorderLayout.CENTER);
        
        this.setVisible(false);
    }
    
    public void checkForNewVersion(){
        //use ws client to know if there is a new version
        String[] new_version=MasterServerHTTPConnection.checkNewVersion();
        
        if(new_version==null){
            SimpleLog.logger.info("No new version available");
            return;
        }
        
        //update panel with information as there is a new version
        info_label.setText("B@ggle "+new_version[0]+" "+Language.getString(75));
        info_label.setToolTipText(new_version[1]);
        
        //display panel as there is a new version
        this.setVisible(true);
    }
    
    public void downloadNewVersion(){
        String url = "http://baggle.org/download.php";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.mainFrame,Language.getString(27)+"\n"
                    +Language.getString(77)+" "+url);
        }
    }
}
