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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.ColorFactory;


public class TopBarPanel extends JPanel {
    
    private JButton logout_button = new JButton();
    private JButton rules_button = new JButton();
    
    private JButton scores_button = new JButton();
    
    private JTextField search_field = new JTextField();
    public TimePanel time_pane = new TimePanel();
    
   
    public TopBarPanel(TimePanel time_pane) {
        super();
                       
        search_field.setOpaque(true);
        search_field.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
                if(ke.getKeyChar()=='\n'){
                    searchWordAction();
                }
            }
            public void keyPressed(KeyEvent ke) {}
            public void keyReleased(KeyEvent ke) {}
        });
        
        JLabel search_label = new JLabel();
        search_label.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/search.png")));
        search_label.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent me) {
                searchWordAction();
            }
            public void mousePressed(MouseEvent me) {}

            public void mouseReleased(MouseEvent me) {}

            public void mouseEntered(MouseEvent me) {}

            public void mouseExited(MouseEvent me) {}
        });
        JPanel search_panel = new JPanel(new BorderLayout());
        search_panel.setOpaque(false);
        search_panel.setPreferredSize(new Dimension(120,32));
        search_panel.setMinimumSize(new Dimension(80,32));
        search_panel.add(search_label,BorderLayout.WEST);
        search_panel.add(search_field,BorderLayout.CENTER);
        
        this.time_pane=time_pane;
        time_pane.setPreferredSize(new Dimension(300,32));
        time_pane.setMinimumSize(new Dimension(200,32));
        
        logout_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/back.png")));
        logout_button.setToolTipText(Language.getString(29));
        logout_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logoutAction();
            }
        });
        
        rules_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/info_small.png")));
        rules_button.setText(Language.getString(81));
        rules_button.setToolTipText(Language.getString(80));
        rules_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rulesAction();
            }
        });
        
        scores_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/medal.png")));
        scores_button.setToolTipText(Language.getString(55));
        scores_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoresAction();
            }
        });
        
        this.setLayout(new GridBagLayout());
        this.setBackground(ColorFactory.BLUE_BOARD);
        
        GridBagConstraints g = new GridBagConstraints();
        
        g.gridx=0;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.insets=new Insets(8, 5,8,5);
        this.add(logout_button,g);
        
        g.gridx=1;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.insets=new Insets(8, 5,8,5);
        this.add(rules_button,g);
        
        g.gridx=2;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.CENTER;
        g.weightx=1;
        g.insets=new Insets(8, 5,8,5);
        this.add(time_pane,g);
        
        g.gridx=3;
        g.gridy=0;
        g.gridwidth=1;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_END;
        g.weightx=0;
        g.insets=new Insets(8, 5,8,5);
        this.add(search_panel,g);
        
        g.gridx=4;
        g.gridy=0;
        g.gridwidth=GridBagConstraints.REMAINDER;
        g.gridheight=1;
        g.fill=GridBagConstraints.NONE;
        g.anchor=GridBagConstraints.LINE_START;
        g.weightx=0;
        g.insets=new Insets(8, 5,8,5);
        this.add(scores_button,g);
        
    }
    
    private void logoutAction(){
        Main.mainFrame.leaveRoom();
    }

    private void rulesAction(){
        Main.mainFrame.roomPane.toggleRules();
    }
        
    private void searchWordAction(){
        String word = search_field.getText().trim().toLowerCase();
        if(word!=null && word.length() !=0){
            try {
                Desktop.getDesktop().browse(new URI("http://"+Main.connection.LANG+".wiktionary.org/wiki/"+word.toLowerCase()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Main.mainFrame,Language.getString(27));
            }
        }
        Main.mainFrame.roomPane.wordEntryPane.giveFocus();
    }
    
    private void scoresAction(){
        try {
            Desktop.getDesktop().browse(new URI(Main.OFFICIAL_WEBSITE+"/scores.php"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.mainFrame,Language.getString(27));
        }
    }
}
