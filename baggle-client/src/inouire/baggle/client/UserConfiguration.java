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
package inouire.baggle.client;

import java.io.*;
import inouire.utils.Utils;


/**
 *
 * @author Edouard de Labareyre
 */
public class UserConfiguration {
    
    public String NAME;
    public String AVATAR;
    public int POSX=-1;
    public int POSY=-1;
    public int WIDTH=900;
    public int HEIGHT =590;
    public int BOARD_WIDTH=300;
    public boolean SHOW_SCORES=true;
    
    public static File CONFIG_FILE;
        
    public UserConfiguration(){
        
        //create config dir if necessary
        if(!Main.CONFIG_FOLDER.exists()){
            Main.logger.info("Config dir not found, creating it: "+Main.CONFIG_FOLDER.getAbsolutePath());
            Main.CONFIG_FOLDER.mkdir();
            if(!Main.CONFIG_FOLDER.exists()){
                Main.logger.error("Impossible to create directory: "+Main.CONFIG_FOLDER.getAbsolutePath());
                return;
            }
        }
                
        CONFIG_FILE=new File(Main.CONFIG_FOLDER,"baggle.conf");

    }
    
    /**
     * Save the configuration to file
     */
    public void saveToFile(){

        try{
            FileWriter fw=new FileWriter(CONFIG_FILE);
            fw.write("name="+NAME+"\n");
            fw.write("avatar="+AVATAR+"\n");
            fw.write("width="+WIDTH+"\n");
            fw.write("height="+HEIGHT+"\n");
            fw.write("posx="+POSX+"\n");
            fw.write("posy="+POSY+"\n");
            fw.write("board_width="+BOARD_WIDTH+"\n");
            fw.write("show_scores="+SHOW_SCORES+"\n");
            fw.close();
            Main.logger.info("Config saved to "+CONFIG_FILE.getAbsolutePath());
        }catch(IOException e){
            Main.logger.error("Impossible to save config to "+CONFIG_FILE.getAbsolutePath());
        }
        
    }
    
    /**
     * Load the configuration from file
     */
    public void loadFromFile(){
        FileInputStream in;
        try {
            in = new FileInputStream(CONFIG_FILE);
        } catch (FileNotFoundException ex) {
            Main.logger.error("Impossible to open file at "+CONFIG_FILE.getAbsolutePath());
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try{
            String line="";
            String[] arg;
            ConfigKey key;
            String value;

            while((line = reader.readLine()) != null ){
                arg= line.split("=");
                if(arg.length==2){
                    key=ConfigKey.valueOf(arg[0].trim().toUpperCase());
                    value=arg[1].trim();
                    switch(key){
                        case NAME:
                            NAME=value;
                            break;
                        case AVATAR:
                            AVATAR=value.toLowerCase();
                            break;
                        case POSX:
                            POSX=Utils.getIntValue(value, -1);
                            break;
                        case POSY:
                            POSY=Utils.getIntValue(value, -1);
                            break;
                        case WIDTH:
                            WIDTH=Utils.getIntValue(value, -1);
                            break;
                        case HEIGHT:
                            HEIGHT=Utils.getIntValue(value, -1);
                            break;
                        case BOARD_WIDTH:
                            BOARD_WIDTH=Utils.getIntValue(value, 300);
                            break;
                        case SHOW_SCORES:
                            SHOW_SCORES=Utils.getBoolValue(value, true);
                            break;
                    }
                }
            }
            Main.logger.info("Successfully loaded config file: "+CONFIG_FILE.getAbsolutePath());
            
            Main.mainFrame.setSize(WIDTH,HEIGHT);
            Main.mainFrame.setLocation(POSX,POSY);
            Main.mainFrame.connectionPane.sideConnectionPane.setPlayerAvatar(Main.configuration.AVATAR);
            Main.mainFrame.connectionPane.sideConnectionPane.setPlayerName(Main.configuration.NAME);
            Main.mainFrame.connectionPane.sideConnectionPane.scoresPane.setShowed(Main.configuration.SHOW_SCORES);
        
        }catch(Exception e){
            Main.logger.warn("Error wile loading config file: "+CONFIG_FILE.getAbsolutePath());
        }finally{
            try{
                reader.close();
                in.close();
            }catch(IOException e){}
        }
    }
    
    public void updateName(String name){
        NAME=name;
    }
    public void updateAvatar(String avatar){
        AVATAR=avatar;
    }
    
    public void updateWindowSetting(int posX,int posY,int width,int height){
        POSX=posX;
        POSY=posY;
        WIDTH=width;
        HEIGHT=height;
    }
    
    public void updateBoardSize(int size){
        BOARD_WIDTH=size;
    }
    
}
enum ConfigKey{
    NAME,AVATAR,
    POSX,POSY,WIDTH,HEIGHT,
    BOARD_WIDTH,SHOW_SCORES
}