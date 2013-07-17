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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import inouire.baggle.server.Main;
/**
 *
 * @author edouard
 */
public class MonitoringPanel extends JPanel{

    JTextPane log = new JTextPane();
    JButton stop =new JButton();
    JProgressBar time=new JProgressBar();
    JProgressBar players=new JProgressBar();
    JLabel name;

    static String server_name="";
    static int max_players=6;
    static boolean all_words_count=false;
    static int port=12345;
    static boolean ghost=false;
    static boolean parental=false;
    static boolean disable_chat=false;
    static String password="";

    JTabbedPane Tp=new JTabbedPane();

    public MonitoringPanel(){
        this.setLayout(new BorderLayout());
        JScrollPane jsp=new JScrollPane(log);

        JPanel top = new JPanel(new BorderLayout());
        name=new JLabel("le salon de b@ggle");
        name.setFont(new Font("Serial", Font.BOLD, 20));
        name.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/wait.png")));
        JButton stop_server = new JButton();
        stop_server.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/stop.png")));
        stop_server.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(players.getValue()>0){
                    JOptionPane.showMessageDialog(Main.mainFrame,"Il reste des joueurs dans le salon !");
                }else{
                    Main.server.stopServer();
                    Main.mainFrame.switchToSetup();
                }
            }
        });
        top.add(name,BorderLayout.CENTER);
        top.add(stop_server,BorderLayout.EAST);
        this.add(top,BorderLayout.NORTH);

        JPanel time_panel=new JPanel(new BorderLayout());
                JLabel clock=new JLabel();
                clock.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/clock.png")));
            time_panel.add(clock,BorderLayout.WEST);
            time_panel.add(time,BorderLayout.CENTER);

        JPanel players_panel=new JPanel(new BorderLayout());
                JLabel player=new JLabel();
                player.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/players.png")));
            players_panel.add(player,BorderLayout.WEST);
            players_panel.add(players,BorderLayout.CENTER);

        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        log.setEditable(false);
        
        Tp.add(jsp,"Log");

        add(Tp,BorderLayout.CENTER);
        
        redirectSystemStreams();

    }

    public void resetComponent() {
        name.setText(MonitoringPanel.server_name);
        name.setIcon(new ImageIcon(getClass().getResource("/inouire/baggle/server/icons/wait.png")));
        time.setValue(0);
        players.setValue(0);
        players.setMaximum(MonitoringPanel.max_players);
        log.setText("");
    }
    
    
    //////////////////////////////
    //code from http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Document doc = log.getDocument();
                try {
                    doc.insertString(doc.getLength(), text, null);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
                log.setCaretPosition(doc.getLength() - 1);
            }
        });
    }
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
            updateTextPane(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
            updateTextPane(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
            }
        };
        //Main.server.addLog4jAppender(out);
    }
    //////////////////////////////////////////////
}
