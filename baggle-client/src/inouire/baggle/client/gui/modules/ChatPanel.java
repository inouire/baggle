package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.gui.MainFrame;

/**
 *
 * @author Edouard de Labareyre
 */
public class ChatPanel extends JPanel{

    static int size=16;
    private int offset=0;
    JPanel center = new JPanel();

    int last_author=-1;
    OneChatPane[] list = new OneChatPane[size];
    LinkedList<ChatBlock> blockList = new LinkedList<ChatBlock>();
    int pointeur=0;//pointeur vers le block du bas

    public MainFrame root;

    public ChatPanel(){
        super();
        
        this.setLayout(new GridLayout(0,1));
        this.setBorder(BorderFactory.createTitledBorder(""));
        for(int k=size-1 ; k>=0 ; k--){
            list[k]=new OneChatPane(this);
            this.add(list[k]);
        }
        
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                mouseWheelMovedAction(evt);
            }
        });
        
        setMinimumSize(new Dimension(350,10));
        setPreferredSize(new Dimension(350,10));
    }

    public void scroll(int dist){
        offset+=dist;
        if(offset>=(blockList.size()-size)){
            offset=blockList.size()-size;
        }
        if(offset<0){
            offset=0;
        }
        reAssign();
    }

    public void addServerInfo(String info){
        if(last_author==0){
            blockList.addFirst(ChatBlock.ServerBlock(info));
        }else{
            blockList.addFirst(ChatBlock.ServerBlock(info));
            last_author=0;
        }
        offset=0;
        reAssign();
    }

    public void addMessage(int id,String message){
        String name = Main.connection.players_id_name.get(id);
        String avatar = Main.connection.players_id_avatar.get(id);
        if( name != null && avatar != null){
            if(id == last_author){
                blockList.addFirst(ChatBlock.ClientBlock("", avatar, message));
            }else{
                blockList.addFirst(ChatBlock.ClientBlock(name, avatar, message));
                last_author=id;
            }
            offset=0;
            reAssign();
        }
    }

    public void disableAll(){
        for(OneChatPane p : list){
           p.disableAll();
        }
    }
    
    public void enableAll(){
        for(OneChatPane p : list){
            p.refresh();
        }
    }
    
    public void eraseAll(){
        blockList=new LinkedList<ChatBlock>();
        last_author=-1;
        pointeur=0;
        reAssign();
    }
    
    private void reAssign(){
        int i=offset;
        boolean top=false;
        boolean bottom=false;
        for(int k =0 ; k<size ;k++){
            if(k==0 && i!=0){
                bottom=true;
            }
            if(k==size-1 && offset < blockList.size()-size){
                top =true;
            }
            if(i<blockList.size()){
                list[k].setBlock(blockList.get(i),top,bottom);
            }else{
                list[k].setBlock(null,false,false);
            }
            list[k].erase();
            top=false;
            bottom=false;
            i++;
        }
        repaint();
        for(int k =0 ; k<size ;k++){
            list[k].refresh();
        }
    }

    private void mouseWheelMovedAction(java.awt.event.MouseWheelEvent evt){
        if(blockList.size()<=size){
            offset=0;
            return;
        }
        offset-=2*evt.getWheelRotation();
        if(offset>=(blockList.size()-size)){
            offset=blockList.size()-size;
        }
        if(offset<0){
            offset=0;
        }
        reAssign();
    }
}

class ChatBlock{
    String name="";
    String message="";
    String avatar="";
    boolean is_server=false;

    ChatBlock(String name, String avatar, String message, boolean is_server){
        this.name=name;
        this.avatar=avatar;
        this.message=message;
        this.is_server=is_server;
    }

    //client talk
    static ChatBlock ClientBlock(String name, String avatar, String message){
        return new ChatBlock(name, avatar, message, false);
    }

    //server talk
    static ChatBlock ServerBlock(String message){
        return new ChatBlock("server", "server", message,true);
    }

}

class OneChatPane extends JPanel{

    private ChatBlock block;
    private JTextPane text=new JTextPane();
    private JLabel player=new JLabel();

    private static Font F= new Font("Serial", Font.BOLD, 13);
    private static Font f=new Font("Serial", Font.PLAIN, 12);

    public ImageIcon[] icons_small = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/top.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/bottom.png")),
    };

    OneChatPane(final ChatPanel chatPane){
        this.setLayout(new BorderLayout());

        player.setFont(F);
        player.setOpaque(false);
        player.setForeground(Color.BLACK);
        
        text.setFont(f);
        text.setOpaque(false);
        text.setForeground(Color.BLACK);
        text.setText("");
        text.setEditable(false);

        this.setOpaque(true);
        this.add(text,BorderLayout.CENTER);
        this.add(player,BorderLayout.WEST);
    }

    void setBlock(ChatBlock cb,boolean top, boolean bottom){
        this.block=cb;
    }

    void disableAll(){
         setBackground(Color.LIGHT_GRAY);
    }

    void erase(){
        player.setIcon(null);
        player.setText("");
        text.setText("");
    }

    void refresh(){

        erase();

        if(block==null){
            setBackground(Color.WHITE);
            return;
        }

        //cas particulier du serveur qui parle
        if(block.is_server){
            setBackground(ColorFactory.getAvatarColor("server"));
            text.setText(block.message);          
            return;
        }

        setBackground(ColorFactory.getAvatarColor(block.avatar));
        
        String full_text;
        if (block.name.isEmpty()) {
            full_text = block.message;
        } else {
            full_text = block.name+": "+block.message;
        }
        //player.setIcon(Main.avatarFactory.getSmallAvatar(block.avatar));
        text.setText(full_text);         
    }
}



