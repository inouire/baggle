package inouire.baggle.client.gui.modules;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.threads.MasterServerHTTPConnection;

/**
 *
 * @author edouard
 */
public class ScoresPanel extends JPanel{
    
    public JPanel content;
    
    private JButton title;
    
    public JLabel general_label=new JLabel();
    public JLabel brute_label=new JLabel();
    public JLabel hunter_label=new JLabel();
    public JLabel star_label=new JLabel();
    public JLabel winner_label=new JLabel();
    
    private CardLayout baseLayout;
    private JPanel center;
    
    private boolean showed=true;
    private static Dimension D_big = new Dimension(50,140);
    private static Dimension D_small = new Dimension(50,0);
    
    private Icon icon_hide=new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/delete.png"));
    private Icon icon_show=new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/add.png"));
    
    public ScoresPanel(){
        
        JPanel top=new JPanel();
        JLabel icon = new JLabel();
        icon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/podium.png")));
        top.add(icon);
        title = new JButton();
        title.setText(Language.getString(85));

        title.setHorizontalTextPosition(AbstractButton.LEADING);
        title.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                togglePanelView();
            }
        });
        top.add(title);
        top.add(new JLabel("        "));
        
        general_label.setFont(new Font(null,Font.BOLD,14));
        
        general_label.setHorizontalAlignment(JLabel.CENTER);
        brute_label.setHorizontalAlignment(JLabel.CENTER);
        hunter_label.setHorizontalAlignment(JLabel.CENTER);
        star_label.setHorizontalAlignment(JLabel.CENTER);
        winner_label.setHorizontalAlignment(JLabel.CENTER);
                
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        
        Insets marge = new Insets(5,5,5,5);
        GridBagConstraints g = new GridBagConstraints();
        
        g.gridx=0;
        g.gridy=0;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        content.add(general_label,g);
        
        g.gridy=1;
        content.add(hunter_label,g);
        
        g.gridy=2;
        content.add(brute_label,g);
        
        g.gridy=3;
        content.add(winner_label,g);
       
        g.gridy=4;
        content.add(star_label,g);
        
        
        baseLayout=new CardLayout();
        center = new JPanel();
        center.setLayout(baseLayout);
        center.add(content,"content");
        center.add(new JPanel(),"blank");
        
        this.setLayout(new BorderLayout());
        this.add(top,BorderLayout.NORTH);
        this.add(center,BorderLayout.CENTER);
        this.setBorder(BorderFactory.createTitledBorder(""));

    }
    
    public void setShowed(boolean showed){
        this.showed=!showed;
        togglePanelView();
    }
    
    public final void togglePanelView(){
        if(showed){
            baseLayout.show(center, "blank");
            content.setPreferredSize(D_small);
            title.setIcon(icon_show);
            Main.configuration.SHOW_SCORES=false;
        }else{
            baseLayout.show(center, "content");
            content.setPreferredSize(D_big);
            title.setIcon(icon_hide);
            Main.configuration.SHOW_SCORES=true;
        }
        showed=!showed;
    }
    
    public void updateScores(){
        
        String name = Main.mainFrame.connectionPane.sideConnectionPane.getPlayerName();
        
        int[] scores = MasterServerHTTPConnection.getScores(name);
        
        if(scores!=null){
            if(scores.length==1){
                general_label.setText("Aucun score pour "+name+".");
                hunter_label.setText("Jouez pour entrer dans le tableau des scores !");
                brute_label.setText("");
                winner_label.setText("");
                star_label.setText("");
            }else{
                general_label.setText(scores[0]+" / "+scores[1]);
                hunter_label.setText(scores[2]+" points gagnés au total");
                brute_label.setText(scores[3]+" points maxi en une partie");
                winner_label.setText(scores[4]+" adversaires battus");
                star_label.setText(scores[5]+" mots de six lettres et plus");
            }
        }else{
            general_label.setText("Erreur à la récupération des scores");
            hunter_label.setText("");
            brute_label.setText("");
            winner_label.setText("");
            star_label.setText("");
        }
        
    }
    
}
