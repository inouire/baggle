package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedList;
import javax.swing.*;


/**
 *
 * @author edouard
 */
public class PlayersResultsPanel extends JPanel{

   
    private JList resultsList;
    private DefaultListModel resultsListModel;
    
    
    public PlayersResultsPanel(boolean all_results){
        super();
        resultsListModel = new DefaultListModel();
        resultsList = new JList(resultsListModel);
        resultsList.setCellRenderer(new ImageListCellRenderer());
        resultsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        resultsList.setVisibleRowCount(-1);
        if(all_results){
            resultsList.setFixedCellHeight(110);
        }else{
            resultsList.setFixedCellHeight(150);
        }
        resultsList.setFixedCellWidth(250);

        // put our JList in a JScrollPane
        JScrollPane resultsListScrollPane = new JScrollPane(resultsList);
        resultsListScrollPane.setMinimumSize(new Dimension(150, 50));
        resultsListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        this.setLayout(new BorderLayout());

        this.add(resultsListScrollPane,BorderLayout.CENTER);
    }

    public void addResult(String title,LinkedList<String> words){
        String list="";
        
        //build list
        for(String a:words){
            list+=a+", ";
        }
        
        //remove last char
        if(list.length()>0){
            list=list.substring(0,list.length()-1);
        }
        
        //create panel with this list
        OnePlayerResultPanel result = new OnePlayerResultPanel(title,list);
        resultsListModel.addElement(result);
        notifyResize();
    }
        
    public void addResult(String name,int score,LinkedList<String> words){
        addResult(name+": "+score,words);
    }
    
    public void clearResults(){
        resultsListModel.removeAllElements();
        repaint();
    }

    void notifyResize() {
        resultsList.setFixedCellWidth((resultsList.getSize().width/2)-1);
    }
    
    
   
}
class OnePlayerResultPanel extends JPanel{

    private JTextPane words_found;

    public OnePlayerResultPanel(String name_and_score,String words){
        super();

        Font f = new Font("Serial", Font.PLAIN, 9);
        words_found = new JTextPane();
        words_found.setFont(f);
        words_found.setEditable(false);
        words_found.setText(words);
        words_found.setOpaque(false);
        this.setBackground(Color.WHITE);
        this.setLayout(new BorderLayout());
        this.add(words_found,BorderLayout.CENTER);

        this.setBorder(BorderFactory.createTitledBorder(name_and_score));
    }
}
