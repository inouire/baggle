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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.datagrams.CHATDatagram;
import inouire.baggle.datagrams.Datagram;
import inouire.baggle.datagrams.STATUSDatagram;
import inouire.baggle.datagrams.WORDDatagram;
import inouire.baggle.types.Status;
import inouire.baggle.types.Words;

/**
 *
 * @author Edouard de Labareyre
 */
public class WordEntryPanel extends JPanel{

    private JTextField word_field = new JTextField();
    private String previousWord="";
    
    public WordEntryPanel(){

        this.setLayout(new BorderLayout());
       
        word_field.setFont(new Font("Serial", Font.BOLD, 30));
        word_field.setHorizontalAlignment(JTextField.CENTER);
        word_field.setToolTipText(Language.getString(23));
        word_field.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyPressedAction(e);
            }
        });

        //handling escape key in word field
        this.word_field.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "actionName");
        this.word_field.getActionMap().put("actionName", new AbstractAction("actionName") {
            public void actionPerformed(ActionEvent evt) {
                Main.mainFrame.setState(JFrame.ICONIFIED);
            }
        });
        //handling up and down key in word field
        this.word_field.getInputMap().put(KeyStroke.getKeyStroke("UP"), "up");
        this.word_field.getActionMap().put("up", new AbstractAction("up") {
            public void actionPerformed(ActionEvent evt) {
               word_field.setText(previousWord);
            }
        });
        this.word_field.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "down");
        this.word_field.getActionMap().put("down", new AbstractAction("down") {
            public void actionPerformed(ActionEvent evt) {
               if(word_field.getText().trim().equals(previousWord)){
                   word_field.setText("");
               }
            }
        });

        //this.add(mode,BorderLayout.WEST);
        this.add(word_field, BorderLayout.CENTER);


    }

    

    private void keyPressedAction(KeyEvent e){
        if(e.getModifiers()==KeyEvent.SHIFT_MASK) {
            switch(e.getKeyCode()) {
                case (KeyEvent.VK_ENTER)://set ready status
                    Main.connection.send(new STATUSDatagram(Status.READY).toString());
                    break;
                case (KeyEvent.VK_LEFT)://turn grid
                    Main.mainFrame.roomPane.boardPane.rotateGrid(false);
                    break;
                case (KeyEvent.VK_RIGHT)://turn grid
                    Main.mainFrame.roomPane.boardPane.rotateGrid(true);
                    break;
                case (KeyEvent.VK_BACK_SPACE)://set/unsey reset status
                    if(Main.mainFrame.roomPane.playersPane.getPlayerStatus(Main.connection.my_id)==Status.RESET){
                        Main.connection.send(new STATUSDatagram(Status.IDLE).toString());
                    }else{
                        Main.connection.send(new STATUSDatagram(Status.RESET).toString());
                    }
                    break;
            }
        }else{
            char t = e.getKeyChar();
            if(this.word_field.getText().length()>=2){
                Main.mainFrame.roomPane.wordStatusPane.reset();
            }
            if(t=='\n'){
                sendWord(false);
            }
        }
    }

   

    /**
     * Gives the focus to the word entry field
     */
    public void giveFocus(){
        word_field.grabFocus();
        //word_field.requestFocus();
    }

    /**
     * Add some the text at the end of the field
     * @param s
     */
    public void addText(String s){
        word_field.setText(word_field.getText()+s);
        if(word_field.getText().length()>=3){
            Main.mainFrame.roomPane.wordStatusPane.reset();
        }
    }

    public void sendWord(boolean forceWord){

        String wordToSend = word_field.getText().trim();
        previousWord = wordToSend;
        
        if(Main.connection.in_game){
            if(!forceWord && Datagram.isChatMessage(wordToSend)){
                Main.connection.send(new CHATDatagram(Datagram.replaceAccents(wordToSend)).toString());
            }else{
                // check locally if the word has not already been found
                if(Main.mainFrame.roomPane.wordsFoundPane.contains(wordToSend)){
                    Main.mainFrame.roomPane.wordStatusPane.setWord(wordToSend.toUpperCase(), Words.ALREADY_FOUND);
                }else if(wordToSend.length()<3){//check that the word is not too short (<3)
                    Main.mainFrame.roomPane.wordStatusPane.setWord(wordToSend.toUpperCase(), Words.SHORT);
                }else{//send it to server
                    Main.connection.send(new WORDDatagram(Datagram.removeAccents(wordToSend)).toString());
                }
            }
        }else{//not in game -> it's a chat message
            Main.connection.send(new CHATDatagram(Datagram.replaceAccents(wordToSend)).toString());
        }
        //reset field and dices
        Main.mainFrame.roomPane.boardPane.resetDicesStatus();
        word_field.setText("");
    }
    

}


