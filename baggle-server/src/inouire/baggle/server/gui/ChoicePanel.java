package inouire.baggle.server.gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import inouire.baggle.server.Main;

/**
 *
 * @author edouard
 */
public class ChoicePanel extends JPanel{

    public ChoicePanel(){
        JPanel a=new JPanel();
        JPanel b=new JPanel();
        JPanel c=new JPanel();

        JLabel al = new JLabel("b@ggle-server v"+Main.VERSION);
        al.setFont(new Font("Serial", Font.PLAIN, 12));
        al.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/wizard.png")));
        a.add(al);

        JButton bb = new JButton("Créer une partie accessible sur internet");
        bb.setFont(new Font("Serial", Font.PLAIN, 18));
        bb.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/web.png")));
        bb.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Main.mainFrame.configuration.listenOnLan=true;
                Main.mainFrame.configuration.registerToMasterServer=true;
                Main.mainFrame.switchToTestPane();
            }
        });
        b.add(bb);

        JButton cb = new JButton("Créer une partie en réseau local             ");
        cb.setFont(new Font("Serial", Font.PLAIN, 18));
        cb.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/lan.png")));
        cb.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Main.mainFrame.configuration.listenOnLan = true;
                Main.mainFrame.configuration.registerToMasterServer=false;
                Main.mainFrame.switchToSetup();
            }
        });
        c.add(cb);

        this.setLayout(new GridLayout(0,1));
        add(a);
        add(b);
        add(c);
    }

}
