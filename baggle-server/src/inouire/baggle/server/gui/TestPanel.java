 /* Copyright 2009-2014 Edouard Garnier de Labareyre
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

package inouire.baggle.server.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import javax.swing.*;
import inouire.baggle.server.Main;
import inouire.baggle.server.core.MasterServerHTTPConnection;

/**
 *
 * @author edouard
 */
public class TestPanel extends JPanel{

    private JTextPane text;
    private JLabel uplinkIcon;
    private JLabel downlinkIcon;
    private JButton refresh;
    private JButton link;

    private int PORT=12341;

    private int uplink=0;

    private boolean ping=false;
    private boolean isListenning=false;
    private boolean portBusy=false;

    public TestPanel(){
        this.setLayout(new GridLayout(0,1));

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());

        JPanel left=new JPanel(new BorderLayout());
        JLabel local=new JLabel();
        local.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/lan.png")));
        left.add(local,BorderLayout.CENTER);
        
        JPanel right = new JPanel(new BorderLayout());
        JLabel web = new JLabel();
        web.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/web.png")));
        right.add(web,BorderLayout.CENTER);

        JPanel upP=new JPanel(new FlowLayout());
        uplinkIcon=new JLabel();
        upP.add(uplinkIcon);
        JPanel downP=new JPanel(new FlowLayout());
        downlinkIcon=new JLabel();
        downP.add(downlinkIcon);
        JPanel middleP=new JPanel(new FlowLayout());
        refresh=new JButton();
        refresh.setText("Relancer le test");
        refresh.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/refresh.png")));
        refresh.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               launchTest();
           }
        });
        middleP.add(refresh);

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0,1));
        center.add(upP);
        center.add(middleP);
        center.add(downP);

        top.add(left,BorderLayout.WEST);
        top.add(center,BorderLayout.CENTER);
        top.add(right,BorderLayout.EAST);

        link=new JButton();
        link.setVisible(false);
        

        JPanel bottom=new JPanel();
        bottom.setLayout(new BorderLayout());
        text=new JTextPane();
        text.setEditable(false);
        bottom.setBorder(BorderFactory.createTitledBorder("Diagnostic"));
        bottom.add(text,BorderLayout.CENTER);
        bottom.add(link,BorderLayout.NORTH);

        this.add(top);
        this.add(bottom);

    }
    public void setUplinkArrow(int status){
        switch(status){
            case 0:
                uplinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ltr_ok.png")));
                break;
            case 1:
                uplinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ltr_error.png")));
                break;
            case 2:
                uplinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ltr_wait.png")));
                break;
        }
    }
    public void setDownlinkArrow(int status){
        switch(status){
            case 0:
                downlinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/rtl_ok.png")));
                break;
            case 1:
                downlinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/rtl_error.png")));
                break;
            case 2:
                downlinkIcon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/rtl_wait.png")));
                break;
        }
    }
    public void setOkButton(){
        link.setText("Lancer le serveur");
        link.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ok.png")));
        link.setVisible(true);
        link.setEnabled(true);
        link.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
                Main.mainFrame.switchToSetup();
           }
        });

    }
    public void setHelpButton(){
        link.setText("Lancer le tutoriel de redirection de port");
        link.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/tool.png")));
        link.setVisible(true);
        link.setEnabled(true);
        link.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               try {
                    Desktop.getDesktop().browse(new URI("http://www.inouire.net/blog/index.php?post/2010/02/04/tuto-port-forwarding-baggle"));
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (Exception e2) {
                    addText("Impossible de lancer le navigateur internet.\nRendez vous à l'adresse suivante: http://www.inouire.net/blog/index.php?post/2010/02/04/tuto-port-forwarding-baggle");
                }
           }
        });
    }

    public void addText(String text){
        this.text.setText(this.text.getText()+text);
    }

    public void writeDiag(){
        refresh.setEnabled(true);
        text.setText("");
        if(uplink==0){//no connexion
            addText("Impossible de se connecter à internet.\n\nVérifiez votre connexion.\n");
            setUplinkArrow(1);
            setDownlinkArrow(1);
        }else if(uplink==1){//connexion with proxy
            addText("Impossible de se connecter au serveur principal b@ggle.\n\nVérifiez que votre connexion internet n'est pas filtrée par un proxy.\n");
            setUplinkArrow(1);
            setDownlinkArrow(1);
        }else{//uplink connexion ok
            setUplinkArrow(0);
            if(ping){
                setDownlinkArrow(0);
                setOkButton();
                addText("La connexion est correctement paramétrée pour lancer un serveur.\n");
            }else if(portBusy){
                addText("Un serveur est probablement déjà lancé sur le port "+Main.server.configuration.listeningPort+" de cet ordinateur !\nLa connexion ne peut pas être diagnostiquée correctement.\n\n");
                setDownlinkArrow(1);
            }else{
                setDownlinkArrow(1);
                setHelpButton();
                addText("C'est probablement la première fois que vous lancez un serveur\n\nVous devez paramétrer votre box d'accès internet pour autoriser le serveur b@ggle à recevoir des connexions (des futurs joueurs)\n" +
                        "Pour cela vous pouvez lancer le tutoriel en cliquant sur le bouton ci-dessus. \n");
            }
        }
    }

   

    private  void testLink(){
            
        //http://masterserver.baggle.org/ws/pingme.php?port=42345
        int pingable = MasterServerHTTPConnection.testPing();
        
        switch(pingable){
            case 0:
                setUplinkArrow(0);
                setDownlinkArrow(0);
                break;
            case 1:
                setUplinkArrow(1);
                setDownlinkArrow(1);
                break;
            case 2:
                setUplinkArrow(0);
                setDownlinkArrow(1);
                break;
        }
        refresh.setEnabled(true);
    }

    public void launchTest(){
        link.setVisible(false);
        setUplinkArrow(2);
        setDownlinkArrow(2);
        refresh.setEnabled(false);
        text.setText("Diagnostic de la connexion...\n");

        Thread t = new Thread() {
            @Override
            public void run() {
                testLink();
            }
        };
        t.start();
        
    }


}
