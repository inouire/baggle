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

package inouire.baggle.client.gui;

import inouire.baggle.client.gui.modules.StatusActionPanel;
import inouire.baggle.client.gui.modules.TimePanel;
import inouire.baggle.client.gui.modules.WordEntryPanel;
import inouire.baggle.client.gui.modules.WordsFoundPanel;
import inouire.baggle.client.gui.modules.ChatPanel;
import inouire.baggle.client.gui.modules.PlayersPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import inouire.baggle.client.Language;
import inouire.baggle.client.gui.modules.BoardPanel;
import inouire.baggle.client.gui.modules.ResultsPanel;
import inouire.baggle.client.gui.modules.RulesPane;
import inouire.baggle.client.gui.modules.TopBarPanel;
import inouire.baggle.client.gui.modules.WordStatusPanel;

/**
 *
 * @author Edouard de Labareyre
 */
public class RoomPanel extends JPanel{

    public TimePanel timePane;

    public ChatPanel chatPane;
    public PlayersPanel playersPane;
    public WordEntryPanel wordEntryPane;
    public WordsFoundPanel wordsFoundPane;
    public BoardPanel boardPane;
    public WordStatusPanel wordStatusPane;
    
    public TopBarPanel topBarPane;

    public ResultsPanel resultsPane;
    GamePanel gamePane;
    NoGamePanel noGamePane;
    public RulesPane rulesPane;
    
    public StatusActionPanel readyActionPane;
    public StatusActionPanel resetActionPane;
    
    private JPanel right,center,left;

    private JPanel actionPane;
    private CardLayout actionPaneLayout;
    
    private String previousCard="nogame";
    private String currentCard="nogame";
    
    
    public RoomPanel(){

        super();

        //left
        playersPane  = new PlayersPanel();
        wordsFoundPane = new WordsFoundPanel();
        left = new JPanel();
        left.setLayout(new BorderLayout());
        left.add(playersPane,BorderLayout.CENTER);
        left.add(wordsFoundPane,BorderLayout.SOUTH);

        //south
        wordEntryPane = new WordEntryPanel();

        //center
        resetActionPane = new StatusActionPanel(false);
        readyActionPane = new StatusActionPanel(true);
        boardPane = new BoardPanel();
        wordStatusPane = new WordStatusPanel();
        timePane = new TimePanel();

        //north
        topBarPane = new TopBarPanel(timePane);
        
        resultsPane = new ResultsPanel(readyActionPane);
        gamePane = new GamePanel(resetActionPane,boardPane,wordStatusPane);
        noGamePane = new NoGamePanel();
        rulesPane = new RulesPane();
      
        actionPane =new JPanel();
        actionPaneLayout=new CardLayout();
        actionPane.setLayout(actionPaneLayout);
        actionPane.add(gamePane,"game");
        actionPane.add(resultsPane,"results");
        actionPane.add(noGamePane,"nogame");
        actionPane.add(rulesPane,"rules");
        actionPane.add(new JPanel(),"nothing");
        actionPaneLayout.show(actionPane, "nothing");

        center = new JPanel(new BorderLayout());
        center.add(actionPane,BorderLayout.CENTER);
        center.add(wordEntryPane,BorderLayout.SOUTH);

        //right
        chatPane = new ChatPanel();
        right = new JPanel(new BorderLayout());
        right.add(chatPane,BorderLayout.CENTER);
        
        this.setLayout(new BorderLayout());
        this.add(topBarPane,BorderLayout.NORTH);
        this.add(left,BorderLayout.WEST);
        this.add(center,BorderLayout.CENTER);
        this.add(right,BorderLayout.EAST);

    }

    /**
     * Give focus to word field
     */
    public void giveFocusToWordField(){
        this.wordEntryPane.giveFocus();
    }
    
    private void showCurrent(){
        actionPaneLayout.show(actionPane, currentCard);
    }
    
    public void showPrevious(){
        String current = currentCard;
        String previous = previousCard;
        currentCard=previous;
        previousCard=current;
        actionPaneLayout.show(actionPane, previous);
    }
    
    public void showGame(){
        previousCard=currentCard;
        currentCard="game";
        showCurrent();
    }

    public void showResults(){
        previousCard=currentCard;
        currentCard="results";
        showCurrent();
    }
    
    public void showNoGame(){
        previousCard=currentCard;
        currentCard="nogame";
        showCurrent();
    }
    
    public void toggleRules(){
        if(currentCard.equals("rules")){
            showPrevious();
        }else{
            previousCard=currentCard;
            currentCard="rules";
            showCurrent();
        }
        
    }
    
    
    public void showNothing(){
        previousCard=currentCard;
        currentCard="nothing";
        showCurrent();
    }
    
    public void disableAll(){
        //TODO
    }

    public void enableAll(){
        //TODO
    }


    /**
     * Put the focus on the word field
     */
    public void putFocusOnWordField(){
        wordEntryPane.giveFocus();
    }

    /**
     * Refresh only the right side of the screen
     */
    public void refreshRight(){
        chatPane.repaint();
    }

    /**
     * Refresh only the center side of the screen
     */
    public void refreshCenter(){
        center.repaint();
    }

    /**
     * Refresh only the left side of the screen
     */
    public void refreshLeft(){
        playersPane.repaint();
    }

    public int getBotLevel(){
        return this.playersPane.getBotLevel();
    }

    public void setGameMode(String mode){
        Color background = ColorFactory.BROWN_BOARD;
        if(mode.equalsIgnoreCase("all")){
            background=ColorFactory.BLUE_BOARD;
        }else if(mode.equalsIgnoreCase("trad")){
            background=ColorFactory.GREEN_BOARD;
        }
        
        this.topBarPane.setBackground(ColorFactory.BLUE_BOARD);
        this.boardPane.setBoardColor(background);
        this.resultsPane.setBoardColor(background);
        
        repaint();
    }
}


class GamePanel extends JPanel{


    public GamePanel(StatusActionPanel sap , BoardPanel bp, WordStatusPanel wsp){
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(0,1));
        top.add(sap);
        add(top,BorderLayout.NORTH);
        add(bp,BorderLayout.CENTER);
        add(wsp,BorderLayout.SOUTH);
    }
}

class NoGamePanel extends JPanel{
    
    StatusActionPanel sap = new StatusActionPanel(true);
    
    public NoGamePanel(){
        
        Font F=new Font("Serial", Font.BOLD, 20);
         
        JLabel no_game = new JLabel();
        no_game.setFont(F);
        no_game.setHorizontalAlignment(SwingConstants.CENTER);
        no_game.setVerticalAlignment(SwingConstants.BOTTOM);
        no_game.setText(Language.getString(78));
        
        JLabel click_on_ready = new JLabel();
        click_on_ready.setHorizontalAlignment(SwingConstants.CENTER);
        click_on_ready.setVerticalAlignment(SwingConstants.BOTTOM);
        click_on_ready.setFont(F);
        click_on_ready.setText(Language.getString(79));
        
        JLabel down_arrow = new JLabel();
        down_arrow.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/down_arrow.png")));       
        down_arrow.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        down_arrow.setVerticalAlignment(SwingConstants.TOP);
        
        JPanel center = new JPanel(new GridLayout(0,1));
        center.add(no_game);
        center.add(click_on_ready);
        center.add(down_arrow);
        
        this.setLayout(new BorderLayout());
        this.add(center,BorderLayout.CENTER);
        this.add(sap,BorderLayout.SOUTH);
    }
    
    void resetPanel(){
        
    }
}