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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import inouire.baggle.client.Main;
import inouire.baggle.datagrams.RESULTDatagram;

/**
 *
 * @author edouard
 */
public class ResultsPanel extends JPanel{

    private MiniBoardPanel miniboard = new MiniBoardPanel();
    private PodiumPanel podium = new PodiumPanel();
    private PlayersResultsPanel all_results = new PlayersResultsPanel(false);

    private String grid;
    
    public ResultsPanel(StatusActionPanel sap){
        this.setLayout(new BorderLayout());

        JButton display_all_button = new JButton("Toutes les solutions");
        display_all_button.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/star_small.png")));
        
        JPanel grid_panel = new JPanel(new BorderLayout());
        grid_panel.add(miniboard,BorderLayout.CENTER);
        grid_panel.add(display_all_button,BorderLayout.NORTH);
        
        JPanel top = new JPanel(new BorderLayout());
        top.add(podium,BorderLayout.CENTER);
        top.add(grid_panel,BorderLayout.EAST);

        this.add(top,BorderLayout.NORTH);
        this.add(all_results,BorderLayout.CENTER);
        this.add(sap,BorderLayout.SOUTH);
        
        all_results.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                all_results.notifyResize();
            }
            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        
        display_all_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                allSolutionsClicked();
            }
        });

    }

    public void clearResults(){
        all_results.clearResults();
        all_results.notifyResize();
        podium.resetPodium();
    }
    
    public void addResult(RESULTDatagram resultD){
        int score=resultD.score;
        String name=Main.connection.players_id_name.get(resultD.id);
        
        //add the player to the podium if needed
        if(resultD.rank!=null){
            podium.setPlayer(resultD.rank,name,score);
        }
        
        //add the player to the overall table
        all_results.addResult(name,score, resultD.words);
    }
    
   
    public void setGrid(String grid){
        this.grid=grid;
        miniboard.setGrid(grid);
    }
    
    public void setBoardColor(Color board_color){
        miniboard.boardColor= board_color;
        repaint();
    }
    
    public void allSolutionsClicked() {
        try{
            SolutionsFrame solutions = new SolutionsFrame(grid);
            solutions.setVisible(true);
        }catch(Exception ex){
            //display error message: impossible to load dictionnary
        }
    }

}
