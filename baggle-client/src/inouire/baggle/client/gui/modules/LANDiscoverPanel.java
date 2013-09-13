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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.gui.ConnectionPanel;
import inouire.baggle.client.threads.LANDiscoverThread;
import inouire.baggle.datagrams.PINGDatagram;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JProgressBar;


/**
 *
 * @author Edouard de Labareyre
 */
public class LANDiscoverPanel extends JPanel{

   
    private ServerListPanel serverListPanel;
    private ListStatusPanel listStatusPanel;
    
    private int refresh_id=1;
    
    public LANDiscoverPanel(){
        super();
        
        //status panel
        listStatusPanel=new ListStatusPanel(this);
        
        //server list
        serverListPanel = new ServerListPanel();
        
        this.setLayout(new BorderLayout());
        this.add(listStatusPanel,BorderLayout.NORTH);
        this.add(serverListPanel,BorderLayout.CENTER);

    }
    
    public void setStartOfPingProcess(){
        listStatusPanel.setStartOfPingProcess();
    }
    
    public int newRefreshId(){
        refresh_id++;
        serverListPanel.resetList();
        return refresh_id;
    }
        
    public void addValidServer(int id,String ip, int port,PINGDatagram pingD){
        if(refresh_id==id){
            serverListPanel.addServer(ip,port,pingD);
            this.listStatusPanel.setOkMessage();
            serverListPanel.notifyResize();
            Main.mainFrame.connectionPane.lanDiscoverPane.listStatusPanel.setEndOfPingProcess();
        }//else ignore
    }

}



 class ListStatusPanel extends JPanel{
    
    private JButton refresh_button = new JButton();
    private JLabel status_label = new JLabel();
    private JProgressBar scanProgress;
    
    private LANDiscoverPanel parent;

    private Icon[] status_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_wait.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_down.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_error.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_empty.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_refresh.png")),
    };
        
    public ListStatusPanel(LANDiscoverPanel parent){
        super();
        
        this.parent=parent;
        
        status_label.setText(Language.getString(13));
        status_label.setIcon(status_icons[0]);
        status_label.setForeground(Color.BLACK);
        status_label.setHorizontalAlignment(SwingConstants.CENTER);
        
        refresh_button.setIcon(status_icons[4]);
        refresh_button.setEnabled(true);
        refresh_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                refresh_button_clicked();
            }
        });
        
        //progress bar
        scanProgress = new JProgressBar();
        scanProgress.setPreferredSize(new Dimension(70,10));
        JPanel scan_wrapper = new JPanel(new GridBagLayout());
        scan_wrapper.add(scanProgress,ConnectionPanel.CENTERED);
        scan_wrapper.setOpaque(false);
        scan_wrapper.setPreferredSize(new Dimension(80,10));
        
        this.setLayout(new BorderLayout());
        this.add(scan_wrapper, BorderLayout.WEST);
        this.add(status_label, BorderLayout.CENTER);
        this.add(refresh_button, BorderLayout.EAST);
        this.setBackground(ColorFactory.YELLOW_NOTIF);
    }
    
    public void setErrorMessage(){
        status_label.setText("Error");
        status_label.setIcon(status_icons[2]);
        refresh_button.setEnabled(false);
    }
    
    public void setEmpty(){
        status_label.setText("Empty");
        status_label.setIcon(status_icons[3]);
        refresh_button.setEnabled(true);
    }
    
    private void refresh_button_clicked(){
        new LANDiscoverThread(parent).start();
    }
    
    public void setStartOfPingProcess(){
        status_label.setText(Language.getString(12));
        status_label.setIcon(status_icons[0]);
        scanProgress.setVisible(true);
        scanProgress.setIndeterminate(true);
        refresh_button.setEnabled(false);
    }
    
    
    public void setOkMessage(){
        status_label.setText(Language.getString(44));
        status_label.setIcon(status_icons[1]);
        refresh_button.setEnabled(true);
    }
        
    public void setNbServersPinged(int nb){
        if(nb>0){
            refresh_button.setEnabled(true);
        }
    }
    
    public void setEndOfPingProcess(){
        refresh_button.setEnabled(true);
        scanProgress.setIndeterminate(false);
        scanProgress.setValue(100);
    }
}


