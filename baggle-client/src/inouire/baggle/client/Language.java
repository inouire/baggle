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

/**
 *
 * @author Edouard de Labareyre
 */
public class Language {

    static String[] tooltip_fr = new String[]{
        "Lister les serveurs sur le réseau officiel",//0
        "Lister les serveurs en réseau local",//1
    };
    static String[] tooltip_en = new String[]{
        "List servers of the official network",//0
        "List servers on the local network",//1
    };
   
    static String[] fr = new String[] {
        "Protégé par un mot de passe",//0
        "Serveur:","Port:","Connecter",//1
        "Vous devez choisir un pseudo:",//4
        "Le port doit être un entier.",//5
        "Erreur",//6
        "Connexion au serveur...",//7
        "B@ggle, connecté sur ",//8
        "J'ai lancé un serveur",//9
        "Avatar:","Pseudo:","Scan du réseau local...",//10
        "Récupération de la liste des salons...",//13
        "Tous les mots trouvés comptent",//14
        "Rejoindre une partie","Connexion manuelle",//15
        "Mode barre intelligente","Mode jeu uniquement","Mode discussion uniquement",//17
        "Temps restant à jouer pour cette partie",//20
        "Joueurs","Chat",//21
        "Saisir un mot",//23
        "(click ou Shift-Enter pour changer de mode)",//24
        "Mots trouvés",//25
        "Rechercher une définition",//26
        "Impossible de lancer un navigateur internet.",//27
        "À propos de b@ggle",//28
        "Se déconnecter de ce salon",//29
        "Impossible d'envoyer un mot, pas de partie en cours.",//30
        "Je suis prêt","Signaler au serveur que vous êtes prêt à jouer (SHIFT-Enter)",//31
        "Faire une petite pause.","Revenir dans le jeu.","Changer de grille",//33
        "Signaler au serveur que vous voulez changer de grille (SHIFT-Backspace)",//36
        "Nombre de mots que "," a trouvé par rapport au nombre total de mots trouvés",//37
        " joueurs","Entrez le mot de passe pour ce salon:",//39
        "Résultats",//41
        "Impossible de se connecter à internet.","Connexion bloquée par un proxy ou un firewall",//42
        "Choisir un salon depuis la liste",//44
        "Nom d'hôte inconnu.",//45
        "Serveur injoignable sur ce port.\n\nÊtes vous bien connectés au reseau ?\nLe serveur est il bien lancé sur le port ",//46
        " ?\nLe port est il ouvert sur le firewall de la machine serveur ?",//47
        "Numéro de port invalide.",//48
        "La partie va commencer...","Nouvelle grille !",//49
        "Impossible de se connecter à ce salon.",//51
        "La connexion avec le serveur a été perdue",//52
        "Password incorrect !",//53
        "Mot à chercher:",//54
        "Aller voir le tableau des scores","Impossible de lancer un navigateur internet, rendez-vous manuellement sur "+Main.OFFICIAL_WEBSITE+"/scores.php",//55
        "Tourner la grille dans le sens horaire (SHIFT-DROITE)","Tourner la grille dans le sens anti-horaire (SHIFT-GAUCHE)",//57
        "Modifier la taille de la grille",//59
        "Pas de réponse de la part du server principal",//60
        "inconnue",//61
        "Ajouter un robot","Deconnecter le robot",//62
        "Finalement, non !","Annuler la demande de changement de grille (SHIFT-Backspace)",//64
        "Mot valide","Le mot est trop court","Le mot n'est pas dans la grille","Le mot n'est pas dans le dictionnaire","Le mot a été filtré par le controle parental",//66
        "Me voilà !","à bientôt",//71
        "Signaler au serveur que vous n'êtes pas prêt à jouer",//73
        "Seuls les mots que je suis seul à trouver comptent",//74
        "est disponible","Aller à la page de téléchargement",//75
        "La nouvelle version peut être téléchargée sur",//77
        "Pas de partie en cours","Cliquer sur 'Je suis prêt' pour jouer",//78
        "Obtenir plus d'informations sur les règles dans ce salon","Règles",//80
        "Réseau officiel","Réseau local",//82
        "Il n'y a plus de place dans ce salon.",//84
        "Palmarès"//85,
       
    };

    static String[] en = new String[] {
        "Protected by a password",
        "Server:",
        "Port:",
        "Connect",
        "You must choose a nick:",
        "The port number must be an integer",
        "Error",
        "Connecting to server...",
        "B@ggle, connected on ",
        "I started a server",
        "Avatar:",
        "Nick:",
        "Local network auto scan...",
        "Collecting rooms list...",
        "Each word found earn points",
        "Join a game",
        "Manual connection",
        "Smart field mode",
        "Game Only Mode ",
        "Chat Only Mode",
        "Time left for this game",
        "Players",
        "Chat",
        "Enter a word",
        "(click or shift-enter to change mode)",
        "Found words",
        "Look for a definition",
        "Error while starting an internet browser",
        "About b@ggle...",
        "Disconnect from this room",
        "Unable to send word, no game running now",
        "I'm ready",
        "Notify server you're ready to play (SHIFT-Enter)",
        "Have a break",
        "Resume game",
        "Change grid",
        "Tell the server that you want to play with a new grid (SHIFT-Backspace)",
        "Number of words ",
        "has found compared to the total number of words found by all the players.",
        "players",
        "Enter a password for this room: ",
        "Results",
        "Impossible to connect to the internet",
        "Connection may be locked by a proxy or a firewall",
        "Pick a room from the list",//44
        "Unknown host name",
        "Server unreachable on this port.\n\nAre you connected to the network ?\nIs the server listenning on port ",
        "?\nIs the port opened on the firewall of the server machine ?",
        "Invalid port number",
        "The game is about to start...",
        "New grid !",
        "Impossible to connect to this room",
        "Lost server connection",
        "Wrong password !",
        "Word to find:",//54,
        "Go to the hall of fame","Error while starting an internet browser, go to "+Main.OFFICIAL_WEBSITE+"/scores.php",//55
        "Turn the grid clockwise (SHIFT-RIGHT)","Turn the grid counter-clockwise (SHIFT-LEFT)",//57
        "Modify grid size",//59
        "No response from master server",//60
        "unknown",//61
        "Connect a robot","Disconnect bot",//62
        "Well... finally, no !","Cancel grid change request (SHIFT-Backspace)",//63
        "Word accepted","This word is too short","This word is not in the grid","This word is not in the dictionnary","This word has been filtered",//66
        "Here I am !","Good bye",//71
        "Tell the server that you're not so ready to play",//73
        "Words that I'm the only one to find earn points",//74
        "is out","Go to download page",//75
        "The new version can be downloaded at",//77
        "No game in progress","Click on 'I'm ready' to play",//78
        "Know more about the rules in this room","Rules",//80
        "Official network","Local network",//82
        "Maximum number of players has been reached, try another room",//84
        "Ranking"//85
       
    };

    public static String getString(int id){
        String a="###";
        try{
            if(Main.LOCALE.equals("fr")){
                a=fr[id];
            }else if(Main.LOCALE.equals("en")){
                a=en[id];
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            return a;
        }
    }

    public static String getToolTip(int id){
        String a="###";
        try{
            if(Main.LOCALE.equals("fr")){
                a=tooltip_fr[id];
            }else if(Main.LOCALE.equals("en")){
                a=tooltip_en[id];
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            return a;
        }
    }
   
    public static String getAboutMessage(){
        String s;
        if(Main.LOCALE.equals("fr")){
            s="B@ggle version "+  Main.VERSION + "\n\n"+
            "B@ggle est un logiciel libre (license GPLv3) principalement écrit par Edouard de Labareyre.\n"+
            "- Le dictionnaire français est régulièrement mis à jour par Bernard Javerliat\n"+ 
            "- La liste des mots du filtre parental a été constituée par Ersatz.\n"+
            "- L'excellente structure de stockage du dictionnaire (DAWG) est de JohnPaul Adamovsky.\n"+
            "  http://pathcom.com/~vadco/dawg.html\n\n"+
            "Si vous ne connaissez pas les règles du jeu de boggle, il est possible de les afficher\n"+
            "une fois que vous êtes connectés à un salon de jeu.\n\n"+
            "Pour plus d'informations, visitez le site "+Main.OFFICIAL_WEBSITE;
        }else{
            s="B@ggle version "+  Main.VERSION + "\n\n"+
            "B@ggle is a free software (GPLv3 license) mainly developed by Edouard de Labareyre.\n"+
            "- The french dictionnary is continuously updated by Bernard Javerliat\n"+  
            "- The list of french words for parental filter has been made by Ersatz.\n"+ 
            "- The awesome storage structure for dictionnary (DAWG) is from JohnPaul Adamovsky.\n"+
            "  http://pathcom.com/~vadco/dawg.html\n\n"+
            "If you don't know boggle rules, you can display them when you are connected to a room.\n\n"+
            "For more information you can visit the website at "+Main.OFFICIAL_WEBSITE;
        }
        return s;
    }
   
    public static String[] getRules(){
       
        String[] rules = new String[11];
       
        if(Main.LOCALE.equals("fr")){
            rules[0]= "Règles génériques";
            rules[1]="Le but du boggle est de former des mots à partir des lettres "+
              "de la grille, sachant que les lettres doivent se toucher (sur le coté ou en diagonale).\n"+
              "Les mots doivent être dans le dictionnaire, mais peuvent être mis au pluriel, "+
              "conjugués, accordés en genre et en nombre...";
            rules[2]="Exemple: on peut trouver le mot TRIA dans cette grille (du verbe trier).";
            rules[3]="Attention, un même dé ne peut pas être utilisé pour un mot donné.";
            rules[4]="Exemple: on peut former le mot SERRA (du verbe serrer), mais pas PAVA !";
            rules[5]="Règles spécifiques à ce salon";
            rules[6]="Dans ce salon c'est la règle officielle qui s'applique. Ainsi ne rapportent "+
                    "des points que les mots qu'un joueur est le seul à trouver. ";
            rules[7]="Dans ce salon, la règle qui s'applique est un dérivé de la règle officielle. Tous "+
                    "les mots comptent, sachant que plus ils sont longs plus ils rapportent de points. ";
            rules[8]="Les mots doivent comporter au minimum ";
            rules[9]=" lettres pour être acceptés. ";
            rules[10]="En cas de problème n'hésitez pas à demander de l'aide via le chat !";
        }else{
            rules[0]= "Generic rules";
            rules[1]="The purpose of boggle is to form words from the letters of the grid,\n"+
                     "knowing that the letters must touch (on the side or diagonally)."+
                     "Words must be in the dictionary, but may be plural, conjugated\n"+
                     "granted in gender and number...";
            rules[2]="Example: you can find the word CORE in this grid.";
            rules[3]="Note that a dice cannot be used twice in the same word.";
            rules[4]="Example: you cannot find the word CANCEL in this grid because C cannot be used twice !";
            rules[5]="Specific rules for this room";
            rules[6]="In this room the official boggle rule is used. It means that you have to be the only"+
                     "player who finds a word in order to earn points with this word.";
            rules[7]="In this room, the rule is a derivate from the official rule. All the words "+
                     "count, and the longer they are, the more points you win. ";
            rules[8]="Words shall contain at least ";
            rules[9]=" letters to be valid. ";
            rules[10]="If you have troubles to play, feel free to ask fro help in the chat !";
        }
       
        return rules;
    }
   
}