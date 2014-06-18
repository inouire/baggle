package inouire.baggle.client.gui.modules;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.client.threads.BotServerConnection;

/**
 *
 * @author edouard
 */
class BotPanel extends JPanel{

    private JButton start_button;
    private JButton stop_button;
    private JLabel info_label_big=new JLabel("   "+Language.getString(62));
    private JLabel info_label_small=new JLabel("   "+Language.getString(63));

    private RatingPanel starsPane;

    private JPanel bigPanel;
    private JPanel smallPanel;

    private boolean is_started=false;

    private CardLayout baseLayout;

    private static Dimension D_big = new Dimension(1000,45);
    private static Dimension D_small = new Dimension(1000,27);

    public BotPanel(){
        super();

        //mode big
        JLabel image=new JLabel("");
        image.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/border.png")));
        
        JLabel robot=new JLabel();
        robot.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/robot.png")));

        info_label_big.setFont(new Font("Serial", Font.PLAIN, 9));
        
        starsPane = new RatingPanel(5,true);

        start_button=new JButton();
        start_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/add.png")));
        start_button.setMinimumSize(new Dimension(26,26));
        start_button.setPreferredSize(new Dimension(26,26));
        start_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchToStopMode();
            }
        });

        bigPanel = new JPanel();
        bigPanel.setLayout(new GridBagLayout());
        bigPanel.setBackground(ColorFactory.ROBOT_BG);
       
        GridBagConstraints g = new GridBagConstraints();
        
        g.gridx=0;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=GridBagConstraints.REMAINDER;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        bigPanel.add(image,g);
        
        g.gridx=1;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=GridBagConstraints.REMAINDER;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        bigPanel.add(robot,g);
        
        g.gridx=2;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=1;
        g.weighty=1;
        bigPanel.add(info_label_big,g);
        
        g.gridx=2;
        g.gridy=1;
        g.gridwidth=1;
        g.gridheight=GridBagConstraints.REMAINDER;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=0;
        bigPanel.add(starsPane,g);
        
        g.gridx=3;
        g.gridy=0;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=GridBagConstraints.REMAINDER;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.insets=new Insets(3,3,3,3);
        bigPanel.add(start_button,g);
        
        //mode small
        JLabel image_small=new JLabel();
        image_small.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/border.png")));

        info_label_small.setFont(new Font("Serial", Font.PLAIN, 9));
        info_label_small.setPreferredSize(new Dimension(300,20));
        
        stop_button=new JButton();
        stop_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/delete.png")));
        stop_button.setMinimumSize(new Dimension(26,26));
        stop_button.setPreferredSize(new Dimension(26,26));
        stop_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchToStartMode();
            }
        });

        smallPanel = new JPanel();
        smallPanel.setLayout(new GridBagLayout());
        smallPanel.setBackground(ColorFactory.ROBOT_BG);
        GridBagConstraints f = new GridBagConstraints();
        
        f.gridx=0;
        f.gridy=0;
        f.gridwidth=1;
        f.gridheight=GridBagConstraints.REMAINDER;
        f.fill=GridBagConstraints.NONE;
        f.anchor=GridBagConstraints.LINE_START;
        f.weightx=0;
        smallPanel.add(image_small,f);
        
        f.gridx=1;
        f.gridy=0;
        f.gridwidth=1;
        f.gridheight=GridBagConstraints.REMAINDER;
        f.fill=GridBagConstraints.HORIZONTAL;
        f.anchor=GridBagConstraints.CENTER;
        f.weightx=1;
        smallPanel.add(info_label_small,f);
        
        f.gridx=2;
        f.gridy=0;
        f.gridwidth=GridBagConstraints.REMAINDER;
        f.gridheight=GridBagConstraints.REMAINDER;
        f.fill=GridBagConstraints.NONE;
        f.anchor=GridBagConstraints.CENTER;
        f.weightx=0;
        smallPanel.add(stop_button,f);
        
        
        baseLayout=new CardLayout();
        this.setLayout(baseLayout);
        this.add(bigPanel,"big");
        this.add(smallPanel,"small");
        this.setPreferredSize(D_big);
        this.setMaximumSize(D_big);
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        this.setVisible(false);
    }

    void switchToStopMode(){
        baseLayout.show(this, "small");
        this.setPreferredSize(D_small);
        this.setMaximumSize(D_small);
        
        new Thread(){
            @Override
            public void run(){
                Main.bot = new BotServerConnection(starsPane.getRating(),Main.connection.SERVER,Main.connection.PORT,"");
                Main.bot.start();
            }
        }.start();
        
        is_started=true;
        Main.mainFrame.roomPane.giveFocusToWordField();
    }

    void switchToStartMode(){
        baseLayout.show(this, "big");
        this.setPreferredSize(D_big);
        this.setMaximumSize(D_big);
        is_started=false;

        new Thread(){
            @Override
            public void run(){
                if(Main.bot!=null){
                    Main.bot.closeAllConnections();
                }
            }
        }.start();
        
        Main.mainFrame.roomPane.giveFocusToWordField();
    }

    void tryToHide(){
        if(!is_started){
            this.setVisible(false);
        }
    }

    void tryToShow(){
        this.setVisible(true);
    }

    public int getLevel(){
        return this.starsPane.getRating();
    }

}


class RatingPanel extends JPanel{

    private StarPane [] stars;
    private int rating;
    private boolean is_editable;

    /**
     * Create a new star rating panel
     * @param number_of_stars the number of stars that the rating pane contains
     */
    public RatingPanel(int number_of_stars, boolean is_editable) {
        if(number_of_stars<2){
           return;
        }
        this.is_editable=is_editable;
        stars =new StarPane[number_of_stars];

        setLayout(new GridLayout(0,number_of_stars));
        setMaximumSize(new Dimension(30*number_of_stars,25));
        setMinimumSize(new Dimension(22*number_of_stars,22));
        setPreferredSize(new Dimension(25*number_of_stars,22));
        setOpaque(false);

        for(int k=0;k<number_of_stars/2;k++){
            stars[k]=new StarPane(true,k,this);
        }
        rating=number_of_stars/2 -1;
        for(int k=number_of_stars/2;k<number_of_stars;k++){
            stars[k]=new StarPane(false,k,this);
        }
        for (StarPane st : stars){
            add(st);
        }
    }

    void refresh(int id){
        for(int k=0;k<=id;k++){
            stars[k].setSelected(true);
        }
        for(int k=id+1;k<stars.length;k++){
            stars[k].setSelected(false);
        }
        rating=id;
    }

    /**
     * Get current rating
     * @return int the rating
     */
    public int getRating(){
        return rating;
    }

    public void setRating(int n){
        this.rating=n;
        refresh(n);
    }

    /**
     * Change the 'editable' attibute
     * @return
     */
    public void setEditable(boolean is_editable){
        this.is_editable=is_editable;
    }

    /**
     * Get the 'editable' attibute
     * @return boolean the 'editable' attribute
     */
    public boolean isEditable(){
        return this.is_editable;
    }
}

class StarPane extends JPanel{

    public Icon[] icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/star_selected.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/star_selected_hover.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/star_unselected.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/star_unselected_hover.png"))
    };
    boolean selected=true;
    int id;
    JLabel icon=new JLabel(icons[0]);

    StarPane(boolean is_selected,final int id,final RatingPanel parent){
        this.id=id;
        this.selected=is_selected;
        if(this.selected){
            icon.setIcon(icons[0]);
        }else{
            icon.setIcon(icons[2]);
        }
        setLayout(new BorderLayout());
        add(icon,BorderLayout.CENTER);
        setOpaque(false);
        this.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent me) {
                if(parent.isEditable()){
                    parent.refresh(id);
                    icon.setIcon(icons[1]);
                }

            }
            public void mousePressed(MouseEvent me) {
            }
            public void mouseReleased(MouseEvent me) {
            }
            public void mouseEntered(MouseEvent me) {
                if(parent.isEditable()){
                    if(selected){
                        icon.setIcon(icons[1]);
                    }else{
                        icon.setIcon(icons[3]);
                    }
                }
            }
            public void mouseExited(MouseEvent me) {
                if(parent.isEditable()){
                    if(selected){
                        icon.setIcon(icons[0]);
                    }else{
                        icon.setIcon(icons[2]);
                    }
                }
            }
        });
    }

    void setSelected(boolean is_selected){
        selected=is_selected;
        if(selected){
            icon.setIcon(icons[0]);
        }else{
            icon.setIcon(icons[2]);
        }
    }
}