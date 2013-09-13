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

package inouire.baggle.client.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.modules.LANDiscoverPanel;
import inouire.baggle.client.gui.modules.OfficialServersPanel;
import inouire.baggle.client.gui.modules.SideConnectionPanel;
import inouire.baggle.client.threads.LANDiscoverThread;


/**
 *
 * @author Edouard de Labareyre
 */
public class ConnectionPanel extends JPanel{


    public SideConnectionPanel sideConnectionPane;
    public OfficialServersPanel officialServersPane;
    public LANDiscoverPanel lanDiscoverPane;
    
    private NetworkTypeSelectionPanel networkTypePane;
    
    private JButton about_button;    
    
    private JPanel networksPane;
    
    private CardLayout networksLayout;
    
    public static GridBagConstraints CENTERED = new GridBagConstraints (0, 0, 1, 1, 0, 0,
                                                       GridBagConstraints.CENTER,
                                                       GridBagConstraints.CENTER,
                                                       new Insets (0,0,0,0), 0, 0);

    private boolean local_showed=false;
    
    public ConnectionPanel(){
        super();
        
        //selection buttons
        networkTypePane = new NetworkTypeSelectionPanel();
        JPanel network_type_wrapper = new JPanel(new GridBagLayout ());
        network_type_wrapper.add(networkTypePane,CENTERED);
        network_type_wrapper.setOpaque(false);
        
        //top banner
        JLabel brand_label = new JLabel();
        brand_label.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/banner.png")));
        brand_label.setText(Main.VERSION);
        brand_label.setFont(new Font("Serial", Font.BOLD, 16));
        brand_label.setForeground(Color.WHITE);
        
        about_button = new JButton();
        about_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/info.png")));
        about_button.setToolTipText(Language.getString(28));
        about_button.setPreferredSize(new Dimension(42,42));
        about_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                aboutPressed();
            }
        });
        JPanel about_button_wrapper = new JPanel(new GridBagLayout ());
        about_button_wrapper.add (about_button,CENTERED); 
        about_button_wrapper.setOpaque(false);
        about_button_wrapper.setPreferredSize(new Dimension(58,50));
        
        JPanel top_panel = new JPanel(new BorderLayout());
        top_panel.add(brand_label,BorderLayout.WEST);
        top_panel.add(about_button_wrapper,BorderLayout.EAST);
        top_panel.add(network_type_wrapper,BorderLayout.CENTER);
        
        top_panel.setBackground(ColorFactory.GREEN_BOARD);
        
        JLabel non_dispo = new JLabel("Non disponible pour le moment");
        non_dispo.setHorizontalAlignment(SwingConstants.CENTER);
        
        sideConnectionPane = new SideConnectionPanel();

        officialServersPane = new OfficialServersPanel();
        lanDiscoverPane = new LANDiscoverPanel();

        networksPane=new JPanel();
        networksLayout=new CardLayout();
        networksPane.setLayout(networksLayout);
        networksPane.add(officialServersPane,"official");
        
        networksPane.add(lanDiscoverPane,"local");
        networksLayout.show(networksPane, "official");
        
        this.setLayout(new BorderLayout());
        this.add(top_panel,BorderLayout.NORTH);
        this.add(sideConnectionPane,BorderLayout.WEST);
        this.add(networksPane,BorderLayout.CENTER);

    }
    
    public void showOfficial(){
        networksLayout.show(networksPane, "official");
        local_showed = false;
    }
    public void showLocal(){
        networksLayout.show(networksPane, "local");
        local_showed = true;
        new LANDiscoverThread(lanDiscoverPane).start();
    }
    
    public boolean isLocalShowed(){
        return local_showed;
    }
    
    public void aboutPressed(){
        JOptionPane.showMessageDialog(Main.mainFrame,
                Language.getAboutMessage(),
                Language.getString(28),
                JOptionPane.INFORMATION_MESSAGE);
    }
}


class NetworkTypeSelectionPanel extends JPanel{
    
    private Icon[] type_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/network_official.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/network_local.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/network_official_nb.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/network_local_nb.png")),
    };
    
    private JButton officialNetwork_button;
    private JButton localNetwork_button;
       
    public NetworkTypeSelectionPanel(){
        
        officialNetwork_button=new JButton(Language.getString(82));
        officialNetwork_button.setIcon(type_icons[0]);
        officialNetwork_button.setToolTipText(Language.getToolTip(0));
        officialNetwork_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                OfficialButton_clicked();
            }
        });
        
        localNetwork_button = new JButton("");
        localNetwork_button.setIcon(type_icons[3]);
        localNetwork_button.setToolTipText(Language.getToolTip(1));
        localNetwork_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                LocalButton_clicked();
            }
        });
        
        this.setOpaque(false);
        this.add(officialNetwork_button);
        this.add(localNetwork_button);
    }
    
    private void OfficialButton_clicked(){
        officialNetwork_button.setIcon(type_icons[0]);
        officialNetwork_button.setText(Language.getString(82));
        
        localNetwork_button.setIcon(type_icons[3]);
        localNetwork_button.setText("");
        
        Main.mainFrame.connectionPane.showOfficial();
                
    }
    
    private void LocalButton_clicked(){
        officialNetwork_button.setIcon(type_icons[2]);
        officialNetwork_button.setText("");
        
        localNetwork_button.setIcon(type_icons[1]);
        localNetwork_button.setText(Language.getString(83));
        
        Main.mainFrame.connectionPane.showLocal();
    }
    
}




