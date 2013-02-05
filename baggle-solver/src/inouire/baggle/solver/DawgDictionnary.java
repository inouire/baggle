/*
 * This file is an adaptation of the java implementation of the awesome DAWG lexicon structure by JohnPaul Adamovsky
 * All credits too JohnPaul for this very lightweight lexicon structure
 * Check out his website: http://www.pathcom.com/~vadco/dawg.html
 * Many thanks to him for his support for integration of DAWG into b@ggle
 */

package inouire.baggle.solver;

import java.io.BufferedInputStream;
import java.io.DataInputStream;

public class DawgDictionnary {
	
            
    private static final int CHILD_BIT_SHIFT = 5;
    private static final int CHILD_INDEX_BIT_MASK = 0X3FFFFFE0;
    private static final int LETTER_BIT_MASK = 0X0000001F;
    private static final int END_OF_WORD_BIT_MASK = 0X80000000;
    private static final int END_OF_LIST_BIT_MASK = 0X40000000;
    

    private int numberOfNodes;

    private int[] theDawgArray;

    public void createDawg(String dawgFile) throws Exception {
        DataInputStream dawgDataFile = new DataInputStream(new BufferedInputStream(getClass().getResourceAsStream("/inouire/baggle/dict/"+dawgFile)));
        //DataInputStream dawgDataFile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        numberOfNodes = endianConversion(dawgDataFile.readInt());
        theDawgArray = new int[numberOfNodes];

        for (int x = 0; x < numberOfNodes; x++) {
            theDawgArray[x] = endianConversion(dawgDataFile.readInt());
        }
        dawgDataFile.close();
    }

    private int endianConversion(int thisInteger) {
        return ((thisInteger & 0x000000ff) << 24) + ((thisInteger & 0x0000ff00) << 8) + ((thisInteger & 0x00ff0000) >>> 8) + ((thisInteger & 0xff000000) >>> 24);
    }
	
    // These methods are used to extract information from the "theDawgArray" nodes.
    private char nodeLetter(int index) {
        return (char)((theDawgArray[index]&LETTER_BIT_MASK) + 'A');
    }
    private boolean nodeEndOfWord(int index) {
        return ((theDawgArray[index]&END_OF_WORD_BIT_MASK) != 0);
    }
    private int nodeNext(int index) {
        return ((theDawgArray[index]&END_OF_LIST_BIT_MASK) == 0)? (index + 1): 0;
    }
    private int nodeChild(int index) {
        return ((theDawgArray[index]&CHILD_INDEX_BIT_MASK)>>>CHILD_BIT_SHIFT);
    }

    private boolean searchForStringRecurse(String thisString, int position, int thisIndex) {
        int currentIndex = thisIndex;
        char currentChar = thisString.charAt(position);
        while ( currentIndex != 0 ) {
            if ( currentChar > nodeLetter(currentIndex) ) {//letter too small
                currentIndex = nodeNext(currentIndex);
            }else if ( currentChar < nodeLetter(currentIndex) ) {//letter too big
                return false;
            }else if ( thisString.length() == (position + 1) ) {//letter match + this should be the end of the word
                if ( nodeEndOfWord(currentIndex) ) {
                    return true;
                }
                else {
                    return false;
                }
            }else { //letter match, but there are other letters to search for
                return searchForStringRecurse(thisString, position + 1, nodeChild(currentIndex));
            }
        }
        return false;
    }

    public boolean contains(String toSearchFor) {
        String upperString = toSearchFor.toUpperCase();
        return searchForStringRecurse(upperString, 0, (upperString.charAt(0) - 'A' + 1));
    }
	
    private boolean searchForPrefixRecurse(String thisString, int position, int thisIndex) {
        int currentIndex = thisIndex;
        char currentChar = thisString.charAt(position);
        while ( currentIndex != 0 ) {
            if ( currentChar > nodeLetter(currentIndex) ) {//letter too small
                currentIndex = nodeNext(currentIndex);
            }else if ( currentChar < nodeLetter(currentIndex) ) {//letter too big
                return false;
            }else if ( thisString.length() == (position + 1) ) {//letter match + this should be the end of the word
                return true;
            }else { //letter match, but there are other letters to search for
                return searchForPrefixRecurse(thisString, position + 1, nodeChild(currentIndex));
            }
        }
        return false;
    }

    public boolean containsPrefix(String toSearchFor) {
        String upperString = toSearchFor.toUpperCase();
        return searchForPrefixRecurse(upperString, 0, (upperString.charAt(0) - 'A' + 1));
    }
    

    private String searchForStringRecurse(String thisString, int position, int thisIndex, boolean[] result) {
        int currentIndex = thisIndex;
        char currentChar = thisString.charAt(position);
        String addThisMessage = new String("----------------------------------------\n");
        addThisMessage += "Seek |" + currentChar + "| in position |" + position + "|.\n";
        String returnHolder;
        while ( currentIndex != 0 ) {
                addThisMessage += "Node|" + currentIndex + "| Letter|" + nodeLetter(currentIndex) + "| "; 
                if ( currentChar > nodeLetter(currentIndex) ) {
                        currentIndex = nodeNext(currentIndex);
                        addThisMessage += "- Letter too small.\n";
                }
                else if ( currentChar < nodeLetter(currentIndex) ) {
                        result[0]= false; 
                        return (addThisMessage + "- Letter too big\n\nWord Not Found\n");
                }
                else if ( thisString.length() == (position + 1) ) {
                        addThisMessage += "= Letter match.\n";
                        if ( nodeEndOfWord(currentIndex) ) {
                                result[0] = true;
                                return (addThisMessage + "\nWord Found.\n");
                        }
                        else {
                                result[0] = false;
                                return (addThisMessage + "\nWord Not Found.\n");
                        }
                }
                else {
                        addThisMessage += "= Letter match.\n";
                        returnHolder = searchForStringRecurse(thisString, position + 1, nodeChild(currentIndex), result);
                        addThisMessage += returnHolder;
                        return addThisMessage;
                }
        }
        result[0] = false;
        return (addThisMessage + "Reached end of list.\n\nWord Not Found\n");
    }

    public String searchForString(String toSearchFor) {
		boolean[] found = new boolean[1];
		String holder;
		String upperString = toSearchFor.toUpperCase();
		String traversalResult = new String("Searching for:  |" + upperString + "| - ");
		found[0] = false;
		holder = searchForStringRecurse(upperString, 0, (upperString.charAt(0) - 'A' + 1), found);
		if ( found[0] ) traversalResult += "Word Found.\n";
		else traversalResult += "Word Not Found.\n";
		traversalResult += holder;
		return traversalResult;
	}
    

}
