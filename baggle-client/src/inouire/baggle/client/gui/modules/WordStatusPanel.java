package inouire.baggle.client.gui.modules;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import inouire.baggle.client.Language;
import inouire.baggle.client.gui.ColorFactory;
import inouire.baggle.types.Words;

/**
 *
 * @author Edouard de Labareyre
 */
public class WordStatusPanel extends JPanel{

    private JLabel word_status=new JLabel();

    private Icon[] word_status_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_good.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_already_found.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_short.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_not_in_grid.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_not_in_dic.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/word_filtered.png")),
    };

    public WordStatusPanel(){
        Font F=new Font("Serial", Font.BOLD, 24);
        word_status.setFont(F);
        word_status.setText("");
        this.add(word_status);
        this.setPreferredSize(new Dimension(1000,38));
    }

    public void reset(){
        word_status.setText("");
        word_status.setIcon(null);
    }

    public void setWord(String word,Words status){
        String s=word;
        
        switch(status){
            case GOOD:
                word_status.setIcon(word_status_icons[0]);
                word_status.setToolTipText(Language.getString(66));
                word_status.setForeground(ColorFactory.GOOD_WORD);
                break;
            case ALREADY_FOUND:
                word_status.setIcon(word_status_icons[1]);
                word_status.setToolTipText(Language.getString(66));
                word_status.setForeground(ColorFactory.BAD_WORD);
                break;
            case SHORT:
                word_status.setIcon(word_status_icons[2]);
                word_status.setToolTipText(Language.getString(67));
                word_status.setForeground(ColorFactory.BAD_WORD);
                break;
            case NOT_IN_GRID:
                word_status.setIcon(word_status_icons[3]);
                word_status.setToolTipText(Language.getString(68));
                word_status.setForeground(ColorFactory.BAD_WORD);
                break;
            case NOT_IN_DIC:
                word_status.setIcon(word_status_icons[4]);
                word_status.setToolTipText(Language.getString(69));
                word_status.setForeground(ColorFactory.BAD_WORD);
                break;
            case FILTERED:
                word_status.setIcon(word_status_icons[5]);
                word_status.setToolTipText(Language.getString(70));
                word_status.setForeground(ColorFactory.BAD_WORD);
                break;
        }
        word_status.setText(s);
    }


}
