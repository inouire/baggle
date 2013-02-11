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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.gui.ConnectionPanel;
import inouire.baggle.client.threads.MasterServerHTTPConnection;
import inouire.baggle.datagrams.PINGDatagram;

/**
 *
 * @author Edouard de Labareyre
 */
public class OfficialServersPanel extends JPanel{

   
    private ServerListPanel serverListPanel;
    private ServerListStatusPanel serverListStatusPanel;
    
    private int nb_servers=0;
    private int server_ok_counter=0;
    private int server_total_counter=0;
    private int refresh_id=1;
    
    public OfficialServersPanel(){
        super();
        
        //status panel
        serverListStatusPanel=new ServerListStatusPanel(this);
        
        //server list
        serverListPanel = new ServerListPanel();
        
        this.setLayout(new BorderLayout());
        this.add(serverListStatusPanel,BorderLayout.NORTH);
        this.add(serverListPanel,BorderLayout.CENTER);
        
    }
    
    public void setStartOfPingProcess(){
        serverListStatusPanel.setStartOfPingProcess();
    }
    
    public int newRefreshId(int nb){
        nb_servers=nb;
        serverListStatusPanel.setTotalNbServers(nb);
        serverListPanel.resetList();
        server_total_counter=0;
        server_ok_counter=0;
        refresh_id++;
        return refresh_id;
    }
    
    public synchronized void addValidServer(int id,String ip, int port,PINGDatagram pingD){
        if(refresh_id==id){
            serverListPanel.addServer(ip, port, pingD);
            server_total_counter++;
            server_ok_counter++;
            updateNbAnswers();
            
        }//else ignore
        
    }
    
    public void addFailedServer(int id){
        if(refresh_id==id){
            server_total_counter++;
            updateNbAnswers();
        }
    }
    
    private synchronized void updateNbAnswers(){
        serverListStatusPanel.setNbServersPinged(server_total_counter);
        if(server_total_counter==nb_servers){
            Main.logger.debug("All server ping threads ended");
            serverListStatusPanel.setOkMessage();
            if(server_ok_counter==0){
                Main.mainFrame.connectionPane.officialServersPane.serverListStatusPanel.setErrorMessage();
            }else{
                
            }
            Main.mainFrame.connectionPane.officialServersPane.serverListStatusPanel.setEndOfPingProcess();
        }
        serverListPanel.notifyResize();
    }

    
}


/**
 *
 * @author edouard
 */
class ServerListStatusPanel extends JPanel{
    
    private OfficialServersPanel parent;
    private JButton refresh_button = new JButton();
    private JLabel status_label = new JLabel();
    private JProgressBar scanProgress;

    private Icon[] status_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_wait.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_down.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_error.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_empty.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/status_refresh.png")),
    };
        
    public ServerListStatusPanel(OfficialServersPanel parent){
        super();
        
        this.parent=parent;
        
        status_label.setText(Language.getString(13));
        status_label.setIcon(status_icons[0]);
        status_label.setForeground(Color.BLACK);
        status_label.setHorizontalAlignment(SwingConstants.CENTER);
        
        refresh_button.setIcon(status_icons[4]);
        refresh_button.setEnabled(false);
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
        new MasterServerHTTPConnection(parent).start();
    }
    
    public void setStartOfPingProcess(){
        status_label.setText(Language.getString(13));
        status_label.setIcon(status_icons[0]);
        scanProgress.setVisible(true);
        scanProgress.setIndeterminate(true);
        refresh_button.setEnabled(false);
    }
    
    public void setTotalNbServers(int nb){
        scanProgress.setMinimum(0);
        scanProgress.setMaximum(nb);
        scanProgress.setIndeterminate(false);
        scanProgress.setValue(0);
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
        scanProgress.setValue(nb);
    }
    
    public void setEndOfPingProcess(){
        scanProgress.setVisible(false);
        refresh_button.setEnabled(true);
    }
}

