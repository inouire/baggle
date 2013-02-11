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

import inouire.baggle.client.gui.ColorFactory;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.datagrams.ACCEPTDatagram;

/**
 *
 * @author edouard
 */
public class RulesPane extends JPanel{

    private JTextPane specificRulesPane;
    private JButton exitButton;
    
    public RulesPane(){
        
        String[] rules = Language.getRules();
                
        Font title_font = new Font("Serial", Font.BOLD, 14);
        
        Insets marge = new Insets(2,2,2,2);
        
        //prepare gui elements
        
        JLabel general_rules_title = new JLabel(rules[0]);
        general_rules_title.setFont(title_font);
        general_rules_title.setForeground(ColorFactory.RED_BOARD);
        
        JTextPane general_rules_text = new JTextPane();
        general_rules_text.setText(rules[1]);
        general_rules_text.setEditable(false);
        general_rules_text.setOpaque(false);
        
        JLabel example_1_image = new JLabel();
        if(Main.LOCALE.equals("fr")){
            example_1_image.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/regle_exemple1.png")));
        }else{
            example_1_image.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/rule_example1.png")));
        }
        
        
        JTextPane example_1_text =  new JTextPane();
        example_1_text.setText(rules[2]);
        example_1_text.setEditable(false);
        example_1_text.setOpaque(false);
        
        JTextPane dice_rules_text = new JTextPane();
        dice_rules_text.setText(rules[3]);
        dice_rules_text.setEditable(false);
        dice_rules_text.setOpaque(false);
        
        JLabel example_2_image = new JLabel();
        if(Main.LOCALE.equals("fr")){
            example_2_image.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/regle_exemple2.png")));
        }else{
            example_2_image.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/rule_example2.png")));
        }
        
        JTextPane example_2_text = new JTextPane();
        example_2_text.setText(rules[4]);
        example_2_text.setEditable(false);
        example_2_text.setOpaque(false);
        
        JLabel specific_rules_title = new JLabel(rules[5]);
        specific_rules_title.setFont(title_font);
        specific_rules_title.setForeground(ColorFactory.RED_BOARD);
        
        specificRulesPane  = new JTextPane();
        specificRulesPane.setEditable(false);
        specificRulesPane.setOpaque(false);
        
        exitButton=new JButton();
        exitButton.setText("Revenir au jeu");
        exitButton.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_ready_small.png")));
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Main.mainFrame.roomPane.showPrevious();
            }
        });
        
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints g = new GridBagConstraints();
        
        g.gridx=0;
        g.gridy=0;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(general_rules_title,g);
        
        g.gridx=0;
        g.gridy=1;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.BOTH;
        g.anchor=GridBagConstraints.NORTH;
        g.weightx=1;
        g.weighty=0;
        g.insets=marge;
        this.add(general_rules_text,g);
        
        g.gridx=0;
        g.gridy=2;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(example_1_image,g);
        g.gridx=1;
        g.gridy=2;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.WEST;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(example_1_text,g);
        
        g.gridx=0;
        g.gridy=3;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(dice_rules_text,g);
        
        g.gridx=0;
        g.gridy=4;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(example_2_image,g);
        g.gridx=1;
        g.gridy=4;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(example_2_text,g);
        
        g.gridx=0;
        g.gridy=5;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(specific_rules_title,g);
        
        g.gridx=0;
        g.gridy=6;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.NORTH;
        g.weightx=0;
        g.weighty=0;
        g.insets=marge;
        this.add(specificRulesPane,g);

        g.gridx=0;
        g.gridy=7;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=0;
        g.weighty=1;
        g.insets=marge;
        this.add(exitButton,g);
                
    }
    
    public void updateRules(ACCEPTDatagram acceptD){
        
        String specific="";
        String[] rules = Language.getRules();
        
        //mode de jeu
        if(acceptD.mode.equals("trad")){
            specific+=rules[6];
        }else{
            specific+=rules[7];
        }
        
        //nombre de lettres
        specific+=rules[8]+acceptD.min+rules[9];
        
        specificRulesPane.setText(specific);
    }

}
