package inouire.baggle.client.gui.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.datagrams.STATUSDatagram;
import inouire.baggle.types.Status;

/**
 *
 * @author edouard
 */
public class StatusActionPanel extends JPanel{

    private Icon[] mode_icons = {
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_ready_small.png")),
        new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/player_status_reset_small.png")),
    };

    private JButton action_button=new JButton();

    private boolean is_ready_button;

    private boolean button_mode=true;//true-> status on (ready/reset), false-> well not !

    public StatusActionPanel(boolean is_ready_button){

        this.add(action_button);

        this.is_ready_button= is_ready_button;

        if(is_ready_button){
            action_button.setText(Language.getString(31));
            action_button.setToolTipText(Language.getString(32));
            action_button.setIcon(mode_icons[0]);
        }else{
            action_button.setText(Language.getString(35));
            action_button.setToolTipText(Language.getString(36));
            action_button.setIcon(mode_icons[1]);
        }
        
        action_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonClicked();
            }
        });
    }

    //change the mode of the button
    public void setToMode(boolean button_mode){
        this.button_mode=button_mode;
        if(is_ready_button){
            if(button_mode){
                action_button.setText(Language.getString(31));
                action_button.setToolTipText(Language.getString(32));
            }else{
                action_button.setText(Language.getString(64));
                action_button.setToolTipText(Language.getString(73));
            }
        }else{
            if(button_mode){
                action_button.setText(Language.getString(35));
                action_button.setToolTipText(Language.getString(36));
            }else{
                action_button.setText(Language.getString(64));
                action_button.setToolTipText(Language.getString(65));
            }
        }
    }

    public void buttonClicked(){
        if(button_mode){
            if(is_ready_button){
                Main.connection.send(new STATUSDatagram(Status.READY).toString());
            }else{
                Main.connection.send(new STATUSDatagram(Status.RESET).toString());
            }

        }else{
            Main.connection.send(new STATUSDatagram(Status.IDLE).toString());
        }
        setToMode(!button_mode);
    }
}