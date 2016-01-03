package inouire.baggle.client.gui.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.solver.Solver;


/**
 *
 * @author Edouard de Labareyre
 */
public class WordsFoundPanel extends JPanel{

    private JTextPane already_found = new JTextPane();
    private JProgressBar total_gauge = new JProgressBar();

    private JLabel rules_label=new JLabel();

    private int total_points=0;
    private int max_points=0;
    
    private Icon[] language_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/fr.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/en.png")),
    };
   
    public WordsFoundPanel(){

        super();
        
        Font F=new Font("Serial", Font.PLAIN, 9);
        rules_label.setFont(F);
        rules_label.setVerticalAlignment(SwingConstants.CENTER);
        rules_label.setHorizontalAlignment(SwingConstants.CENTER);

        already_found.setText(" ");
        already_found.setEditable(false);
        already_found.setForeground(Color.BLACK);
        already_found.setBackground(Color.WHITE);
        already_found.setFont(new Font("Serial", Font.PLAIN, 10));
        JScrollPane alreadyFoundScrollPane = new JScrollPane(already_found);
        
        Dimension d=new Dimension(1000,10);
        total_gauge.setPreferredSize(d);
        total_gauge.setValue(0);

        setLayout(new BorderLayout());
        add(rules_label,BorderLayout.NORTH);
        add(alreadyFoundScrollPane,BorderLayout.CENTER);
        add(total_gauge,BorderLayout.SOUTH);
        
        this.setBorder(BorderFactory.createTitledBorder(""));
        this.setPreferredSize(new Dimension(100,200));
    }

    public void setMaxPoints(int max){
        max_points=max;
    }

    public void resetWordsFound(){
        already_found.setText("");
        total_points=0;
        total_gauge.setValue(0);
        total_gauge.setToolTipText("0/"+max_points);
    }

    public void addWord(String word){
        already_found.setText(" "+word.toUpperCase()+" "+already_found.getText());
        total_points+=Solver.getPoints(word,Main.connection.REWARD_BIG_WORDS);
        int pourcent=(100*total_points)/max_points;
        total_gauge.setValue(pourcent);
        total_gauge.setToolTipText(total_points+"/"+max_points);
    }

    public boolean contains(String text){
        return already_found.getText().contains(" "+text.toUpperCase()+" ");
    }

    public void setMinWordLength(int min_length){
        String s=min_length+" lettres minimum";
        rules_label.setText(s);
    }

    public void setLanguage(String lang){
        int i=0;
        if(lang.equals("fr")){
            i=0;
        }else if(lang.equals("en")){
            i=1;
        }
        rules_label.setIcon(language_icons[i]);
    }
    
    public void setGameMode(String mode){
        if(mode.equals("trad")){
            rules_label.setToolTipText(Language.getString(74));
        }else if(mode.equals("all")){
            rules_label.setToolTipText(Language.getString(14));
        }
    }
    
    public void disableAll(){
        already_found.setEnabled(false);
    }

    public void enableAll(){
        already_found.setEnabled(true);
    }
}

