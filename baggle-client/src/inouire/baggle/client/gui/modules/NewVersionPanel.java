 /* Copyright 2009-2013 Edouard Garnier de Labareyre
  *
  * This file is part of B@ggle.
  *
  * B@ggle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * B@ggle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with B@ggle.  If not, see <http://www.gnu.org/licenses/>.
  */
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
            Main.logger.info("No new version available");
            return;
        }
        
        //update panel with information as there is a new version
        info_label.setText("B@ggle "+new_version[0]+" "+Language.getString(75));
        info_label.setToolTipText(new_version[1]);
        
        //display panel as there is a new version
        this.setVisible(true);
    }
    
    public void downloadNewVersion(){
        String url = "http://codingteam.net/project/baggle/download";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.mainFrame,Language.getString(27)+"\n"
                    +Language.getString(77)+" "+url);
        }
    }
}
