package inouire.baggle.client.gui.modules;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.gui.MainFrame;
import inouire.baggle.types.Status;


/**
 * Panel containing the player list + the bot panel
 * @author Edouard de Labareyre
 */
public class PlayersPanel extends JPanel {

    private JList playersList;
    private DefaultListModel playersListModel;
    
    
    private BotPanel botPane = new BotPanel();

    private Lock m=new ReentrantLock();

    public MainFrame root;

    public PlayersPanel(){
        
        playersListModel = new DefaultListModel();
        playersList = new JList(playersListModel);
        playersList.setCellRenderer(new ImageListCellRenderer());
        playersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playersList.setLayoutOrientation(JList.VERTICAL);
        playersList.setFixedCellHeight(40);
        playersList.setBackground(Color.WHITE);

        // put our JList in a JScrollPane
        JScrollPane playersListScrollPane = new JScrollPane(playersList);
        playersListScrollPane.setMinimumSize(new Dimension(150, 50));
        
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(""));
        this.setMinimumSize(new Dimension(240,2000));
        this.setPreferredSize(new Dimension(240,2000));

        JPanel listPlusBot=new JPanel(new BorderLayout());
        listPlusBot.setBackground(Color.WHITE);
        listPlusBot.add(playersListScrollPane,BorderLayout.CENTER);
        listPlusBot.add(botPane,BorderLayout.NORTH);
        this.add(listPlusBot,BorderLayout.CENTER);
    }

    /**
     * Add a player in the list
     * @param id
     * @param name
     * @param avatar
     */
    public void addPlayer(int id, String name, String avatar){
        OnePlayerPane player = new OnePlayerPane(id,name,avatar);
        m.lock();
        try{
            playersListModel.addElement(player);
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
            m.unlock();
        }
        updateBotPaneStatus();
    }

    /**
     * Remove a player from the list
     * @param id the id of the player to remove
     */
    public void removePlayer(int id){
        OnePlayerPane toRemove = getPlayer(id);
        if(toRemove != null){
            m.lock();
            try{
                playersListModel.removeElement(toRemove);
            }catch (Exception ex){
                ex.printStackTrace();
            }finally{
                m.unlock();
            }
        }
        updateBotPaneStatus();
    }

    /**
     * Remove all the players from the list
     */
    public void removeAllPlayers(){
        m.lock();
        try{
            playersListModel.removeAllElements();
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
            m.unlock();
        }
        repaint();
    }


    /**
     * Get the status of a given player
     * @param id
     * @return
     */
    public Status getPlayerStatus(int id){
        OnePlayerPane p = getPlayer(id);
        if(p!=null){
            return p.status;
        }else{
            return Status.IDLE;
        }
    }

    /**
     * Update the status of bot panel from the status of all the players of the list
     */
    public void updateBotPaneStatus(){
        int na=0;
        int nt=0;
        m.lock();
        try{
            nt=playersListModel.getSize();
            for(Enumeration e=playersListModel.elements(); e.hasMoreElements();){
                OnePlayerPane opp = (OnePlayerPane) e.nextElement();
                if(opp.status!=Status.PAUSE){
                    na++;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
           m.unlock();
        }
        if(nt<=1){
            botPane.tryToShow();
        }else if(na<=1){
            OnePlayerPane me = getPlayer(Main.connection.my_id);
            if(me!=null){
                if(me.status != Status.PAUSE){
                    botPane.tryToShow();
                }else{
                    botPane.tryToHide();
                }
            }
        }else{
            botPane.tryToHide();
        }
        repaint();
    }

    /**
     * Uupdate the status of a given player
     * @param id the id of the player
     * @param st the status of the player
     */
    public void updateStatus(int id, Status st){
        OnePlayerPane p = getPlayer(id);
        if(p!=null){
            p.updateStatus(st);
        }
        updateBotPaneStatus();
        repaint();
    }

    /**
     * Reset all the gauges of the players
     */
    public void resetAllGauges() {
        m.lock();
        try{
            for(Enumeration e=playersListModel.elements(); e.hasMoreElements() ;){
                OnePlayerPane opp = (OnePlayerPane) e.nextElement();
                opp.progress.setValue(0);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
            m.unlock();
        }
        repaint();
    }

    /**
     * Set the gauge of a given player to the specified value
     * @param id the id of the player
     * @param value the value of the gauge (0-100)
     */
    public void setGauge(int id,int value) {
        OnePlayerPane p = getPlayer(id);
        if(p!=null){
            p.setProgress(value);
        }
        repaint();
    }

    /**
     * Set the total score of a given player to the specified value
     * @param id the id of the player
     * @param score the total score of the player
     */
    public void setTotalScore(int id,int score) {
        OnePlayerPane p = getPlayer(id);
        if(p!=null){
            p.setTotalScore(score);
        }
        repaint();
    }

    /**
     * Get the pane corresponding to a given player id
     * @param id the id of the player
     * @return the player pane
     */
    private OnePlayerPane getPlayer(int id){
        OnePlayerPane r=null;
        m.lock();
        try{
            for(Enumeration e=playersListModel.elements(); e.hasMoreElements();){
                OnePlayerPane opp = (OnePlayerPane) e.nextElement();
                if(opp.id==id){
                    r=opp;
                    break;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
            m.unlock();
        }
        return r;
    }

    /**
     * Enable all the components of the players pane
     */
    public void enableAll(){
    }

    /**
     * Disable all the components of the players pane
     */
    public void disableAll(){
        removeAllPlayers();
    }

    /**
     * Give the the number of connected players
     * @return the number of players connected
     */
    public int getNumberOfPlayers(){
        return playersListModel.size();
    }

    public int getBotLevel(){
        return botPane.getLevel();
    }
    
    public void removeBot(){
        this.botPane.switchToStartMode();
    }
}

class OnePlayerPane extends JPanel{

    int id;

    Status status=Status.IDLE;
    
    public Icon[] status_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_reset.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_ready.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_pause.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_blank.png"))
    };

    private JLabel label;
    private JLabel icon;
    private JLabel status_label;
    private JLabel score_label;
    JProgressBar progress;

    OnePlayerPane(int id, String name, String avatar){
        this.id=id;

        this.setPreferredSize(new Dimension(20,40));
        setBackground(Color.WHITE);
        setOpaque(true);

        icon = new JLabel();
        icon.setIcon(Main.avatarFactory.getAvatar(avatar));

        label = new JLabel(name);
        label.setForeground(Color.BLACK);
        
        status_label = new JLabel();
        status_label.setForeground(Color.BLACK);
        status_label.setIcon(status_icons[3]);
        
        progress = new JProgressBar();
        progress.setBorderPainted(false);
        progress.setMaximum(100);
        progress.setOrientation(JProgressBar.HORIZONTAL);
        progress.setSize(10, 13);
        progress.setPreferredSize(new Dimension(10,13));
        progress.setMaximumSize(new Dimension(1000,13));
        progress.setToolTipText(Language.getString(37)+name+ Language.getString(38));

        score_label=new JLabel();

        JPanel group = new JPanel(new BorderLayout());
        group.add(label,BorderLayout.CENTER);
        group.add(progress,BorderLayout.SOUTH);
        group.add(score_label,BorderLayout.EAST);
        group.setOpaque(false);
        
        setLayout(new BorderLayout());
        add(icon,BorderLayout.WEST);
        add(status_label,BorderLayout.EAST);
        add(group,BorderLayout.CENTER);
    }

    void setProgress(int number){
        progress.setValue(number);
    }

    void updateStatus(Status status){
        this.status=status;
        switch(status){
            case IDLE:
                setBackground(Color.WHITE);
                status_label.setIcon(status_icons[3]);
                break;
            case READY:
                setBackground(ColorFactory.LIGHT_GREEN);
                status_label.setIcon(status_icons[1]);
                break;
            case PAUSE:
                setBackground(ColorFactory.LIGHT_GRAY);
                status_label.setIcon(status_icons[2]);
                break;
            case RESET:
                setBackground(ColorFactory.LIGHT_RED);
                status_label.setIcon(status_icons[0]);
                break;
        }
    }

    void setTotalScore(int total_score) {
        score_label.setText(total_score+"");
    }


}

class ImageListCellRenderer implements ListCellRenderer{
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
                                                boolean cellHasFocus)
  {
    if (value instanceof JPanel){
      Component component = (Component) value;
      return component;
    }
    else{
      // TODO - I get one String here when the JList is first rendered; proper way to deal with this?
      return new JPanel();
    }
  }
}


