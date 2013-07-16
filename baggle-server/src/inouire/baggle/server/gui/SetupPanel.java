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

package inouire.baggle.server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import inouire.baggle.server.Main;
import inouire.baggle.server.bean.ServerConfigXML;
/**
 *
 * @author edouard
 */
public class SetupPanel extends JPanel{

    JLabel server_icon;
    JTextField server_name_field;

    JCheckBox game_mode;

    JSlider max_players_slider;
    JCheckBox parental_filter_check;
    JCheckBox disable_chat_check;
    JCheckBox ghost_check;

    JTextField password_field;
    JCheckBox password_check;
    JTextField port_field;

    JButton start_server;

    JButton reset;
    JButton quitter;

    public SetupPanel(){

        this.setLayout(new BorderLayout());
        
        //top panel
        JPanel top=new JPanel();
        top.setLayout(new BorderLayout());
        server_icon=new JLabel();
        server_icon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/mode_0.png")));
        server_name_field = new JTextField();
        server_name_field.setMaximumSize(new Dimension(1000,8));
        server_name_field.setText("le salon de "+System.getProperty("user.name"));
        server_name_field.setFont(new Font("Serial", Font.BOLD, 20));
        start_server=new JButton();
        start_server.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ok.png")));
        top.add(server_icon,BorderLayout.WEST);
        top.add(server_name_field,BorderLayout.CENTER);
        top.add(start_server,BorderLayout.EAST);
        top.add(new JLabel(" "),BorderLayout.NORTH);

        JPanel game_settings=new JPanel();
        game_settings.setBorder(BorderFactory.createTitledBorder("Options de jeu"));
        final JLabel max_players_label=new JLabel("8 joueurs maximum dans le salon");
        max_players_slider=new JSlider();
        max_players_slider.setMinimum(1);
        max_players_slider.setMaximum(10);
        max_players_slider.setMajorTickSpacing(1);
        max_players_slider.setSnapToTicks(true);
        max_players_slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                max_players_label.setText(max_players_slider.getValue()+"  joueurs maximum dans le salon");
            }
        });
        parental_filter_check=new JCheckBox("Activer le filtre parental (pas de mots grossiers)");
        parental_filter_check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                refreshServerIcon();
            }
        });
        
        //max_players_slider.setPaintTicks(true);
        max_players_slider.setValue(8);
        game_mode=new JCheckBox("Compter dans le score les mots trouvés par plusieurs joueurs");
        GroupLayout layout0 = new GroupLayout(game_settings);
        game_settings.setLayout(layout0);
        layout0.setAutoCreateGaps(true);
        layout0.setAutoCreateContainerGaps(true);
        layout0.setHorizontalGroup(
                layout0.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(max_players_label)
                    .addComponent(max_players_slider)
                    .addComponent(game_mode)
                    .addComponent(parental_filter_check)
        );
        layout0.setVerticalGroup(
                layout0.createSequentialGroup()
                    .addComponent(max_players_label)
                    .addComponent(max_players_slider)
                    .addComponent(game_mode)
                    .addComponent(parental_filter_check)
        );
        

        JPanel server_settings=new JPanel();
        server_settings.setBorder(BorderFactory.createTitledBorder("Options du serveur"));
        
        disable_chat_check=new JCheckBox("Désactiver le chat");
        disable_chat_check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                refreshServerIcon();
            }
        });
        ghost_check=new JCheckBox("Rendre le serveur invisible");
        ghost_check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                refreshServerIcon();
            }
        });
        password_check=new JCheckBox("Mot de passe");
        password_check.setSelected(false);
        password_check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if(password_check.isSelected()){
                    password_field.setEnabled(true);
                    refreshServerIcon();
                }else{
                    password_field.setEnabled(false);
                    refreshServerIcon();
                }
            }
        });
        password_check.setEnabled(false);
        password_field=new JTextField(10);
        password_field.setEnabled(false);
        password_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent arg0) {
                if(!password_field.isEnabled()){
                    password_field.setEnabled(true);
                    password_check.setSelected(true);
                    password_field.grabFocus();
                    refreshServerIcon();
                }
            }
            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }
        });
        password_field.setEnabled(false);
        JLabel port_label=new JLabel("Port d'écoute");
        port_field=new JTextField(10);
        port_field.setEnabled(false);
        port_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent arg0) {
                if(!port_field.isEnabled()){
                    port_field.setEnabled(true);
                    port_field.grabFocus();
                }
            }
            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }
        });
        GroupLayout layout = new GroupLayout(server_settings);
        server_settings.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(disable_chat_check)
                        .addComponent(ghost_check)
                        .addComponent(password_check)
                        .addComponent(port_label))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(password_field)
                        .addComponent(port_field))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(disable_chat_check))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(ghost_check))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(password_check)
                            .addComponent(password_field))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(port_label)
                            .addComponent(port_field))
        );
        start_server.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //get new config
                updateConfigurationFromUserInput(Main.server.configuration);
                
                //change the displayed panel
                Main.mainFrame.switchToMonitor();
                
                //start server core
                Main.server.startServer();
                
                //store this new config
                ServerConfigXML.writeToFile(Main.server.configuration,Main.DEFAULT_CONFIG_FILE);
            }
        });
        
        JPanel settings=new JPanel();
        settings.setLayout(new GridLayout(0,1));
        settings.add(game_settings);
        settings.add(server_settings);

        this.add(top,BorderLayout.NORTH);
        this.add(settings,BorderLayout.CENTER);
    }

    public void refreshServerIcon(){
        if(ghost_check.isSelected()){
            server_icon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/ghost.png")));
            return;
        }
        if(password_check.isSelected()){
            server_icon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/mode_1.png")));
        }else{
            server_icon.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/mode_0.png")));
        }
    }
    
    public void loadConfiguration(ServerConfigXML config){
        server_name_field.setText(config.getRoomName());
        game_mode.setSelected(config.isAllWordsCount());
        max_players_slider.setValue(config.getMaxPlayers());
        parental_filter_check.setSelected(config.isParentalFilter());
        disable_chat_check.setSelected(config.isBlockChat());
        ghost_check.setSelected(!config.isRegisterToMasterServer());
        password_check.setSelected(config.isIsPrivate());
        password_field.setText(config.getPassword());
        port_field.setText(config.getLanListenningPort()+"");
    }
    
    public void updateConfigurationFromUserInput(ServerConfigXML config){
        config.setRoomName(server_name_field.getText().trim());
        config.setAllWordsCount(game_mode.isSelected());
        config.setMaxPlayers(max_players_slider.getValue());
        config.setParentalFilter(parental_filter_check.isSelected());
        config.setBlockChat(disable_chat_check.isSelected());
        config.setRegisterToMasterServer(!ghost_check.isSelected());

        try{
            int port = Integer.parseInt(port_field.getText());
            config.setListenningPort(port);
        }catch(NumberFormatException nfe){
            config.setListenningPort(42705);
        }
    }
    
}
