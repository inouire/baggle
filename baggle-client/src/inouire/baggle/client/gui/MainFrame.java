package inouire.baggle.client.gui;

import java.awt.CardLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import inouire.baggle.client.Main;
import inouire.baggle.client.threads.LANDiscoverThread;
import inouire.baggle.client.threads.MasterServerHTTPConnection;
/**
 *
 * @author Edouard de Labareyre
 */
public class MainFrame extends JFrame {

    public RoomPanel roomPane;
    public ConnectionPanel connectionPane;

    private JPanel basePane;
    private CardLayout baseLayout;

    //boolean to know if we are in a room, or not
    private boolean in_room=false;
    
    public MainFrame(){
        
        this.setSize(900,590);
        this.setLocationRelativeTo(null);
        
        this.setTitle("B@ggle");
        this.setIconImage(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/robot_small.png")).getImage());

        basePane=new JPanel();
        baseLayout=new CardLayout();
        basePane.setLayout(baseLayout);
       
        getContentPane().add(basePane);
        
        setTitle("B@ggle - choix d'un salon");
        

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                windowClosingAction(evt);
            }
        });
        
        
    }
    
    public void initConnectionPanel(){
        connectionPane = new ConnectionPanel();
        basePane.add(connectionPane,"connection");
        baseLayout.show(basePane, "connection");
    }
    
    public void initGamePanel(){
        roomPane =new RoomPanel();
         basePane.add(roomPane,"game");
    }
    
    public void switchToGamePane(String roomName){
        setTitle("B@ggle - "+roomName);
        in_room=true;
        baseLayout.show(basePane, "game");
    }

    public void switchToConnectionPane(){
        setTitle("B@ggle - choix d'un salon");
        in_room=false;
        baseLayout.show(basePane, "connection");
    }

    /**
     * Action when "quit" button is pressed
     * @param evt 
     */
    private void windowClosingAction(java.awt.event.WindowEvent evt){
        if(in_room){
            leaveRoom();
        }else{
            leaveApplication();
        }
    }

    /**
     * Leave the game room
     */
    public void leaveRoom(){
        
        //send disconnect datagram
        Main.connection.disconnect();

        //disconnect robot if any
        roomPane.playersPane.removeBot();
        
        //reset gui elements
        roomPane.playersPane.removeAllPlayers();
        roomPane.wordsFoundPane.resetWordsFound();
        roomPane.readyActionPane.setToMode(true);
        roomPane.resetActionPane.setToMode(true);
        roomPane.noGamePane.sap.setToMode(true);
        roomPane.wordStatusPane.reset();
        
        Main.mainFrame.roomPane.chatPane.eraseAll();
        
        //launch refresh of servers list
        if(connectionPane.isLocalShowed()){
            new LANDiscoverThread(connectionPane.lanDiscoverPane).start();
        }else{
            new MasterServerHTTPConnection(connectionPane.officialServersPane).start();
        }
        
        
        //display connection frame
        Main.mainFrame.switchToConnectionPane();
    }
    
    /**
     * Leave the program
     */
    public void leaveApplication(){
        
        //if connected to a room, send disconnect datagram
        if(in_room){
            Main.connection.disconnect();
            
            //disconnect robot if any
            roomPane.playersPane.removeBot();
        }
        
        //save configuration
        Main.configuration.updateName(connectionPane.sideConnectionPane.getPlayerName());
        Main.configuration.updateAvatar(connectionPane.sideConnectionPane.getPlayerAvatar());
        Main.configuration.updateWindowSetting(getX(), getY(),getWidth(),getHeight());
        Main.configuration.updateBoardSize(roomPane.boardPane.getBoardSize());
        Main.configuration.saveToFile();
        Main.cachedServers.saveList();
        
        System.exit(0);
    }
    
    public boolean inRoom(){
        return in_room;
    }
}
