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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;

/**
 *
 * @author Edouard de Labareyre
 */
public class SideConnectionPanel extends JPanel{
 
    private JComboBox player_avatar_combo;
    private JTextField player_name_field;

    public NewVersionPanel newVersionPane;
        
    public ScoresPanel scoresPane;
    
    public SideConnectionPanel(){
                
        newVersionPane = new NewVersionPanel();
        
        player_avatar_combo = new JComboBox(Main.avatarFactory.getPlayerAvatarList());
        Dimension d = new Dimension(100,50);
        player_avatar_combo.setMaximumSize(d);
        player_avatar_combo.setMinimumSize(d);
        player_avatar_combo.setPreferredSize(d);
        

        player_name_field = new JTextField(8);
        player_name_field.setMaximumSize(new Dimension(500,50));
        player_name_field.setFont(new Font("Serial", Font.BOLD, 20));
        player_name_field.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
                new Thread(){
                    @Override
                    public void run(){
                        try {sleep(200);
                        } catch (InterruptedException ex) {}
                        scoresPane.updateScores();
                    }
                }.start();
            }
            public void keyPressed(KeyEvent ke) {
            }
            public void keyReleased(KeyEvent ke) {
            }
        });

        JLabel avatar_label = new JLabel(Language.getString(10));
        JLabel pseudo_label = new JLabel(Language.getString(11));
        
        JPanel p1 = new JPanel();
        GroupLayout layout = new GroupLayout(p1);
        p1.setBorder(BorderFactory.createTitledBorder(""));
        p1.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(avatar_label)
                        .addComponent(player_avatar_combo))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(pseudo_label)
                        .addComponent(player_name_field))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(avatar_label)
                            .addComponent(pseudo_label))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(player_avatar_combo)
                            .addComponent(player_name_field))
        );
        
        scoresPane = new ScoresPanel();
       
        //this.setPreferredSize(d);
        this.setLayout(new BorderLayout());
        this.add(newVersionPane,BorderLayout.NORTH);
        this.add(p1,BorderLayout.CENTER);
        this.add(scoresPane,BorderLayout.SOUTH);
    }
    
    public String getPlayerName(){
        return player_name_field.getText().trim();
    }
    
    public void setPlayerName(String name){
        player_name_field.setText(name.trim());
    }
    
    public void setPlayerAvatar(String avatar){
        player_avatar_combo.setSelectedIndex(Main.avatarFactory.getAvatarIdByName(avatar));
    }
    
    public String getPlayerAvatar(){
        return Main.avatarFactory.getAvatarByIdInAvatarList(player_avatar_combo.getSelectedIndex());
    }
}
