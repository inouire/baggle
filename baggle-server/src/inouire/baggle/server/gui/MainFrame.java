 /* Copyright 2009-2014 Edouard Garnier de Labareyre
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

import inouire.baggle.server.Main;
import inouire.baggle.server.ServerConfiguration;
import inouire.baggle.server.core.BaggleServer;
import inouire.utils.Utils;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author edouard
 */
public class MainFrame extends JFrame{

    private final JPanel basePane;
    private final CardLayout baseLayout;
    private final ChoicePanel choicePane;
    private final TestPanel testPane;
    SetupPanel setupPane;
    private final MonitoringPanel monitorPane;
    
    public ServerConfiguration configuration;
    

    public MainFrame( ServerConfiguration configuration){
        super();
        
        Utils.setBestLookAndFeelAvailable();
        
        this.configuration=configuration;
        Main.server = new BaggleServer(configuration);
        
        this.setSize(500, 400);
        this.setTitle("Interface de lancement de serveur b@ggle");

        choicePane=new ChoicePanel();
        testPane=new TestPanel();
        setupPane=new SetupPanel();
        monitorPane=new MonitoringPanel();

        basePane=new JPanel();
        baseLayout=new CardLayout();
        basePane.setLayout(baseLayout);
        basePane.add(choicePane,"choice");
        basePane.add(testPane,"test");
        basePane.add(setupPane,"setup");
        basePane.add(monitorPane,"monitor");
        
        getContentPane().add(basePane);
        
        baseLayout.show(basePane, "choice");
        
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt){
        if(monitorPane.players.getValue()>0){
            JOptionPane.showMessageDialog(this,"Il reste des joueurs dans le salon !");
        }
        if(Main.server!=null){
            Main.server.stopServer();
        }
    }

    public void switchToChoicePane(){
        setTitle("Interface de lancement de serveur b@ggle");
        baseLayout.show(basePane, "choice");
    }

    public void switchToTestPane(){
        setTitle("Interface de lancement de serveur b@ggle");
        baseLayout.show(basePane, "test");
        testPane.launchTest();
    }

    public void switchToSetup() {
        //load configuration into setupPane TODO
        setupPane.loadConfiguration(configuration);
        setTitle("Paramètre du serveur b@ggle");
        baseLayout.show(basePane, "setup");
    }

    public void switchToMonitor(){
        setTitle("Serveur b@ggle");
        baseLayout.show(basePane, "monitor");
        monitorPane.resetComponent();
    }
}
