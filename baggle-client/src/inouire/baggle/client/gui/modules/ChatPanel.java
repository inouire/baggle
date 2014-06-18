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

    static int size=14;
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
        
        setMinimumSize(new Dimension(260,10));
        setPreferredSize(new Dimension(260,10));
        
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
            blockList.addFirst(ChatBlock.ServerHeaderBlock());
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
                blockList.addFirst(ChatBlock.ClientBlock(avatar,message));
            }else{
                blockList.addFirst(ChatBlock.ClientHeaderBlock(avatar,name));
                blockList.addFirst(ChatBlock.ClientBlock(avatar,message));
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
    String avatar="unknown";
    String name="";
    String talk="";
    boolean display_player=true;
    boolean is_server=false;

    ChatBlock(String avatar,boolean display,String name,String talk, boolean is_server){
        this.avatar=avatar;
        this.display_player=display;
        this.name=name;
        this.talk=talk;
        this.is_server=is_server;
    }

    //client header
    static ChatBlock ClientHeaderBlock(String avatar , String name){
        return new ChatBlock(avatar,true,name,"",false);
    }

    //client talk
    static ChatBlock ClientBlock(String avatar , String talk){
        return new ChatBlock(avatar,false,"",talk,false);
    }

    //server header
    static ChatBlock ServerHeaderBlock(){
        return new ChatBlock("server",true,"","",true);
    }

    //server talk
    static ChatBlock ServerBlock(String talk){
        return new ChatBlock("server",false,"",talk,true);
    }

}

class OneChatPane extends JPanel{

    private ChatBlock block;
    private JTextPane text=new JTextPane();
    private JLabel player=new JLabel();
    private JLabel server=new JLabel();
    private boolean top=false;
    private boolean bottom=false;

    private static Font F= new Font("Serial", Font.BOLD, 12);
    private static Font f=new Font("Serial", Font.PLAIN, 10);

    public ImageIcon[] icons_small = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/top.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/bottom.png")),
    };

    OneChatPane(final ChatPanel chatPane){
        this.setLayout(new BorderLayout());

        player.setFont(F);
        player.setOpaque(false);
        player.setForeground(Color.BLACK);

        server.setFont(F);
        server.setOpaque(false);
        server.setForeground(Color.BLACK);
        
        text.setFont(f);
        text.setOpaque(false);
        text.setForeground(Color.BLACK);
        text.setText("");
        text.setEditable(false);

        this.setOpaque(true);
        this.add(text,BorderLayout.CENTER);
        this.add(player,BorderLayout.WEST);
        this.add(server,BorderLayout.EAST);
        server.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(top){
                    chatPane.scroll(2);
                }else if(bottom){
                    chatPane.scroll(-2);
                }
            }
        });
    }

    void setBlock(ChatBlock cb,boolean top, boolean bottom){
        this.block=cb;
        this.top=top;
        this.bottom=bottom;
    }

    void disableAll(){
         setBackground(Color.LIGHT_GRAY);
    }

    void erase(){
        server.setIcon(null);
        player.setIcon(null);
        server.setText("");
        player.setText("");
        text.setText("");
    }

    void refresh(){

        erase();

        if(block==null){
            setBackground(Color.WHITE);
            return;
        }

        if(top){
            server.setIcon(icons_small[0]);
        }else if(bottom){
            server.setIcon(icons_small[1]);
        }

        //cas particulier du serveur qui parle
        if(block.is_server){
            setBackground(ColorFactory.getAvatarColor("server"));
            if(block.display_player){
                server.setIcon(Main.avatarFactory.getSmallAvatar("server"));
                text.setText("");
            }else{
                text.setText(block.talk);
            }
            return;
        }


        setBackground(ColorFactory.getAvatarColor(block.avatar));

        if(block.display_player){
            player.setIcon(Main.avatarFactory.getSmallAvatar(block.avatar));
            player.setText(block.name);
            text.setText("");
        }else{
            player.setText("");
            text.setText(block.talk);
        }
    }
}



