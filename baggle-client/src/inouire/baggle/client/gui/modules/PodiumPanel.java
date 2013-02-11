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
