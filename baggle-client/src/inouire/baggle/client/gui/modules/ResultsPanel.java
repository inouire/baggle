package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
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
        miniboard.updateGrid(grid);
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
