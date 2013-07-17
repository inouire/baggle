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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.threads.ServerConnection;
import inouire.baggle.datagrams.PINGDatagram;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class ServerListPanel extends JPanel{
    
     ImageIcon[] language_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/fr.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/en.png")),
     };
     
    private JList serverList;
    private DefaultListModel serverListModel;
    
    private final static int REF_WIDTH=400;
    private final static int REF_HEIGHT=140;

    private int heightMax=0;
    private int cellWidth=REF_WIDTH;
    private boolean deadAngle=false;
    
    public ServerListPanel() {
        super();
        
        serverListModel = new DefaultListModel();
        serverList = new JList(serverListModel);
        serverList.setCellRenderer(new OneServerListCellRenderer());
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        serverList.setVisibleRowCount(-1);
        serverList.setFixedCellHeight(REF_HEIGHT);
        serverList.setFixedCellWidth(REF_WIDTH);
                
        // put the JList in a JScrollPane
        JScrollPane resultsListScrollPane = new JScrollPane(serverList);
        resultsListScrollPane.setMinimumSize(new Dimension(150, 50));
        resultsListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        this.setLayout(new BorderLayout());
        this.add(resultsListScrollPane,BorderLayout.CENTER);
        
        addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                notifyResize();
            }

            public void componentMoved(ComponentEvent e) {}

            public void componentShown(ComponentEvent e) {}

            public void componentHidden(ComponentEvent e) {}
        });
        
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                int index = getSelectedIndex(me);
                if(index>=0){
                    OneServerPanel one = (OneServerPanel) serverListModel.get(index);
                    SimpleLog.logger.debug(one.host+":"+one.port+" - "+one.server_name +"has been clicked");
                one.connectToThisServer();
                }
            }

            @Override
             public void mouseExited(MouseEvent arg0) {
                clearHighlight();
                repaint();
            }

        };
         serverList.addMouseListener(mouseListener);
         
         MouseMotionListener mouseMotionListener = new MouseMotionListener(){
            public void mouseDragged(MouseEvent me) {
            }

            public void mouseMoved(MouseEvent me) {
                clearHighlight();
                int index = getSelectedIndex(me);
                if(index>=0){
                    OneServerPanel selectedServer = (OneServerPanel)serverListModel.get(index);
                    selectedServer.highlighted=true;
                    serverList.setToolTipText(selectedServer.players);
                }else{
                    serverList.setToolTipText(null);
                }
                
                repaint();
            }
             
         };
         serverList.addMouseMotionListener(mouseMotionListener);
         
        
    }

    private int getSelectedIndex(MouseEvent me){
        if(me.getY()>heightMax){
             return -1;
        }
        if(me.getX()<cellWidth){
            return serverList.locationToIndex(me.getPoint());
        }
        if(me.getY()<heightMax-REF_HEIGHT){
            return serverList.locationToIndex(me.getPoint());
            
        }
        if(deadAngle){
            return -1;
        }else{
            return serverList.locationToIndex(me.getPoint());
        }
        
    }
    
    private void clearHighlight(){
        for(int k=0;k<serverListModel.getSize();k++){
            ((OneServerPanel)serverListModel.get(k)).highlighted=false;
        }
    }
    
    public void notifyResize() {
        int width=serverList.getSize().width;

        if(width < REF_WIDTH){
            cellWidth=width;
            heightMax = serverListModel.size() * REF_HEIGHT;
        }else{
            cellWidth=width/2;
            if(serverListModel.size()%2==1){
                deadAngle=true;
                heightMax = ((serverListModel.size()/2)+1) * REF_HEIGHT;
            }else{
                deadAngle=false;
                heightMax = serverListModel.size() * REF_HEIGHT / 2;
            }
        }
        serverList.setFixedCellWidth(cellWidth);
    }
     
    public void resetList(){
        serverListModel.removeAllElements();
        repaint();
    }

    public synchronized void addServer(String ip, int port,PINGDatagram pingD){
        OneServerPanel S = new OneServerPanel(pingD.name,ip,port,pingD.mode,pingD.min,pingD.players);
        S.setNbPlayers(pingD.nb,pingD.max);
        S.setTime(pingD.time);
        S.mini_board.setGrid(pingD.grid);
        if(pingD.lang.equals("fr")){
            S.language_icon.setIcon(language_icons[0]);
        }else{
            S.language_icon.setIcon(language_icons[1]);
        }
        if(pingD.priv){
            S.setPrivate();
        }
        serverListModel.addElement(S);
        repaint();
    }
    
}

class OneServerPanel extends JPanel{

    String host=null;
    int port=-1;
    String server_name=null;
    String mode=null;
    int min_word_lenght=3;
    String players=null;
    
    boolean connected=false;//to avoid rebound
        
    JLabel server_name_label = new JLabel();
    JProgressBar players_gauge = new JProgressBar();
    JLabel language_icon=new JLabel();
    JLabel time_label = new JLabel();
    MiniBoardPanel mini_board = new MiniBoardPanel();
    VizHashPanel vizhash = new VizHashPanel();

    boolean highlighted=false;
    
    public OneServerPanel(String server_name,String host,int port,String mode,int min_word_length,String players){
        super();
        
        this.server_name=server_name;
        this.host=host;
        this.port=port;
        this.mode=mode;
        this.players=players;
        
        mini_board.setMode(mode);
        mini_board.setPreferredSize(new Dimension(80,80));
        mini_board.setMinimumSize(new Dimension(80,80));
        
        server_name_label.setHorizontalAlignment(JLabel.CENTER);
        server_name_label.setFont(new Font("", Font.BOLD, 15));
        server_name_label.setText(this.server_name);
        server_name_label.setForeground(Color.DARK_GRAY);
        
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BorderLayout());
        top.add(server_name_label,BorderLayout.CENTER);
        top.add(vizhash,BorderLayout.EAST);
        
        time_label.setHorizontalAlignment(JLabel.CENTER);
        time_label.setForeground(Color.BLACK);
                
        language_icon.setVerticalAlignment(SwingConstants.CENTER);
        language_icon.setHorizontalAlignment(SwingConstants.CENTER);
             
        JLabel min_word_length_label = new JLabel(min_word_length+" lettres min.");
        min_word_length_label.setHorizontalAlignment(SwingConstants.CENTER);
        min_word_length_label.setVerticalAlignment(SwingConstants.CENTER);
        min_word_length_label.setForeground(Color.BLACK);
         
        JPanel infos_panel = new JPanel(new GridLayout(0,1));
        infos_panel.setOpaque(false);
        infos_panel.add(language_icon);
        infos_panel.add(min_word_length_label);
        infos_panel.add(time_label);
        infos_panel.add(players_gauge);

        this.setLayout(new BorderLayout());
        this.add(top,BorderLayout.NORTH);
        this.add(mini_board,BorderLayout.WEST);
        this.add(infos_panel,BorderLayout.CENTER);
        vizhash.setValue("vizhash"+host);
        setBackground(ColorFactory.VERY_LIGHT_GRAY);
        setBorder(BorderFactory.createTitledBorder(""));

    }

    public void setPrivate(){
        setBackground(ColorFactory.LIGHT_BLUE);
        server_name_label.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/lock.png")));
    }
    
    public void setTime(int time){
        int sec=time%60;
        int min=time/60;
        String duree="durée: "+min+" min ";
        if(sec!=0){
            duree+=sec+" sec";
        }
        time_label.setText(duree);
    }
    
    public void setNbPlayers(int nb, int max){
        players_gauge.setMaximum(max);
        players_gauge.setValue(nb);
        players_gauge.setString(nb+"/"+max+Language.getString(39));
        players_gauge.setStringPainted(true);
         if(nb==max){
            setBackground(ColorFactory.LIGHT_RED);
        }
    }
    
    public synchronized void connectToThisServer(){
        if(!connected){
            connected=true;
            Main.connection = new ServerConnection(host,port);
            String player_name = Main.mainFrame.connectionPane.sideConnectionPane.getPlayerName();
            if(player_name.length()==0){
                player_name = (String)JOptionPane.showInputDialog(Main.mainFrame, Language.getString(4)+"\n",
                                            "B@ggle",JOptionPane.PLAIN_MESSAGE,null, null,"");
                if(player_name==null || player_name.trim().length() ==0){
                    return;
                }
                Main.mainFrame.connectionPane.sideConnectionPane.setPlayerName(player_name);
            }
            String avatar = Main.mainFrame.connectionPane.sideConnectionPane.getPlayerAvatar();
            Main.configuration.updateAvatar(avatar);
            Main.configuration.updateName(player_name);
            int result = Main.connection.connect(player_name,avatar);
            if(result==0){
                Main.mainFrame.switchToGamePane(server_name);
            }else{
                Main.mainFrame.switchToConnectionPane();
                connected=false;
                String error_message;
                switch(result){
                    case 3://server full
                        error_message=Language.getString(84);
                        break;
                    default:
                        error_message=Language.getString(51);
                        break;
                }
                JOptionPane.showMessageDialog(Main.mainFrame,
                    error_message,
                    "",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
    }

}


class OneServerListCellRenderer implements ListCellRenderer{
  /**
   * From http://java.sun.com/javase/6/docs/api/javax/swing/ListCellRenderer.html:
   * 
   * Return a component that has been configured to display the specified value. 
   * That component's paint method is then called to "render" the cell. 
   * If it is necessary to compute the dimensions of a list because the list cells do not have a fixed size, 
   * this method is called to generate a component on which getPreferredSize can be invoked. 
   * 
   * jlist - the jlist we're painting
   * value - the value returned by list.getModel().getElementAt(index).
   * cellIndex - the cell index
   * isSelected - true if the specified cell is currently selected
   * cellHasFocus - true if the cell has focus
   */
    @Override
  public Component getListCellRendererComponent(JList jlist, 
                                                Object value, 
                                                int cellIndex, 
                                                boolean isSelected, 
                                                boolean cellHasFocus){
    if (value instanceof JPanel){
      OneServerPanel server = (OneServerPanel) value;
      if(server.highlighted){
          server.setBackground(Color.WHITE);
      }else{
          server.setBackground(ColorFactory.VERY_LIGHT_GRAY);
      }
      return server;
    }
    else{
      // TODO - I get one String here when the JList is first rendered; proper way to deal with this?
      return new JPanel();
    }
  }
}