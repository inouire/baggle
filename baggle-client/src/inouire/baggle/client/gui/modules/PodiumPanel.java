package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author edouard
 */
public class PodiumPanel extends JPanel{

    private JLabel[] players_label;
    private JLabel[] scores_label;

    public PodiumPanel(){
        super();
        Icon[] medal = {new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/dice_gold.png")),
                        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/dice_silver.png")),
                        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/dice_bronze.png"))};

        players_label = new JLabel[3];
        scores_label = new JLabel[3];

        JPanel wrapper = new JPanel();
        //wrapper.setMaximumSize(new Dimension(200,1000));
        //wrapper.setPreferredSize(new Dimension(200,120));
        
        wrapper.setLayout(new GridLayout(0,1));
        
        Font f = new Font("", Font.BOLD, 14);
        Dimension d = new Dimension(215,45);
        
        for(int i=0;i<3;i++){
            players_label[i]=new JLabel();
            players_label[i].setFont(f);
            players_label[i].setIcon(medal[i]);
            scores_label[i]=new JLabel();
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setPreferredSize(d);
            p.add(players_label[i],BorderLayout.CENTER);
            p.add(scores_label[i],BorderLayout.EAST);
            wrapper.add(p);
        }
        this.add(wrapper);
    }

    public void setPlayer(int rank,String name,int score){
        if(rank > 0 && rank <=3){
            if(name.length()>0){
                players_label[rank-1].setText(name);
                scores_label[rank-1].setText(score+"");
                players_label[rank-1].setVisible(true);
                scores_label[rank-1].setVisible(true);
            }else{
                players_label[rank-1].setVisible(false);
                scores_label[rank-1].setVisible(false);
            }
        }
    }
    
    public void resetPodium(){
        for(int k=0;k<3;k++){
            players_label[k].setVisible(false);
            scores_label[k].setVisible(false);
        }
    }
}
