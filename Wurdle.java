/* WurdleHelper: Made to help solve Wurdle puzzles by looking at a dictionary and finding the most common factors*/

import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Vector;
import java.util.TreeSet;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// main class
public class Wurdle
{
     static Manager mng;
     public static void main(String[] args)
     {
          // Try / Catch block to catch any exception and just ignore it.
          try
          {
               // load file into scanner and then stream through the file in the
               // Manager class to extract dictionary words.
               mng = new Manager(new Scanner(new File("words_alpha.txt")));

               // Generate a count of all the letters individually in a hashmap
               mng.generateFrequentLetters(); 
               
               // get most common
               String str = mng.genMostCommonStr();
               System.out.println("The most frequency is in the word: " + str + "\n");
               
               // Solver test
               Scanner scan = new Scanner(System.in);
               //if (scan == null)
               
               boolean isQuit = false;
               while (!isQuit)
               {
                    if (scan.hasNext())
                    {
                         String temp = scan.next();
                         if (temp.equalsIgnoreCase("quit"))
                              isQuit = true;
                         else if (temp.equalsIgnoreCase("remove"))
                         {
                              if (scan.hasNext())
                                   mng.sendWord(scan.next());
                         }
                         else if (temp.equalsIgnoreCase("wildcard")) 
                         {
                              if (scan.hasNext())                                   
                                   mng.sendWildCard(scan.next()); // placeholder
                         }
                         else if (temp.equalsIgnoreCase("exact") || temp.equalsIgnoreCase("confirm"))
                         {
                              if (scan.hasNext())                                   
                                   mng.sendExact(scan.next());
                         }
                         else if (temp.equalsIgnoreCase("killword"))
                         {
                              if (scan.hasNext())                                   
                                   mng.sendKillWord(scan.next());
                         }
                         
                         // determine new common str
                         str = mng.genMostCommonStr();
                         System.out.println("The most frequency is in the word: " + str + "\n");
                    }                                        
               }
          }
          catch(Exception e) // todo: maybe actually error out the exception?
          {
               System.out.println(e);
          }
     }
}

// Some global settings. Public class requires more .java but variables in a class can be public anyway.
class Settings
{
     public static final int characterLimit = 5;
     public static final boolean limitOnLoad = true;  
     public static final boolean requireValidWord = true;
     // Dont need to check for duplicate letters anymore because of the new hashmap to determine
     // frequency for the character per slot in the length of the string.
     public static final boolean duplicateLetterCheck = true;
     public static final boolean freqWithoutPos = true;
}


/*
 * Load the list of words (trim to game size if limit on Loa)
 * Do any of the words contain duplicate letters (Ensure wide spread results to get gray or successful letters).
 * total frequency of a word? (for size of string letterfrequency + letterfrequency)
 *  - not going to subtract duplicates because a word Frequency can still be high enough and we can look for non duplicate results
 *
 */
class Words
{
     // the word
     private String str = new String();

     // if it has duplicate letters.
     private boolean hasDuplicateChar = false;
     
     // Added frequency count
     private long wordFrequency = 0;
     
     // A current game check for the word to become marked avalable / unavaliable
     private boolean allowed = true;

     // constructors
     Words() { }
     Words(String str) { this.str = str; }
     
     // getters / setters
     final String getString() { return str; }
     final boolean containsDuplicate() { return hasDuplicateChar; }
     final long getWordFrequency() { return wordFrequency; }
     final boolean isAllowed() { return allowed; }
     void setString(String str) { this.str = str; }
     void setDuplicate(final boolean hasDuplicateChar) { this.hasDuplicateChar = hasDuplicateChar; }
     void setWordFrequency(final long wordFrequency) { this.wordFrequency = wordFrequency; }
     void setAllowed(final boolean isAllowed) { allowed = isAllowed; }
     
     
     // other functions / methods (ew calling it method)
     void calcFreqWithoutPos(HashMap<Character, Integer> letterFreq)
     {
          if (letterFreq == null)
               return;

          // Set of characters getting added together.
          Set<Character> tempSet = new HashSet<Character>();
          for (int i = 0; i < str.length(); ++i)
          {
               Integer innerVal = letterFreq.get(str.charAt(i));
               if (innerVal != null)
               {
                    if (!Settings.duplicateLetterCheck)
                         wordFrequency += innerVal.intValue();
                    else if (!tempSet.contains(str.charAt(i)) && Settings.duplicateLetterCheck) 
                    {                              
                         tempSet.add(str.charAt(i));
                         wordFrequency += innerVal.intValue();
                    }
               }
          }
     }
     
     void calcWordFreq(HashMap<Character, HashMap<Integer, Integer>> letterFreq)
     {
          if (letterFreq == null)
               return;

          // Set of characters getting added together.
          Set<Character> tempSet = new HashSet<Character>();
          for (int i = 0; i < str.length(); ++i)
          {
               HashMap<Integer, Integer> innerMap = letterFreq.get(str.charAt(i));
               if (innerMap != null)
               {
                    Integer calledCount = innerMap.get(i);
                    if (calledCount != null)
                    {
                         if (!Settings.duplicateLetterCheck)
                              wordFrequency += calledCount.intValue();
                         else if (!tempSet.contains(str.charAt(i)) && Settings.duplicateLetterCheck) 
                         {                              
                              tempSet.add(str.charAt(i));
                              wordFrequency += calledCount.intValue();
                         }
                    }
               }
          }
     }
     
     // when more letters are inputed
     void updateChosenLetters(Set<Character> usedLetters)
     {
          if (allowed)
          {
               for (int i = 0; i < str.length(); ++i)
               {
                    if (usedLetters.contains(str.charAt(i)))
                    {
                         allowed = false;
                         return;
                    }
               }
          }
     } 
     
     // same 00y00 because y cannot be that slot. its wild but not that
     void updateWildCard(String wildStr)
     {
          // wildcard means its not in the slot
          // remove if it is not in the slot
          for (int i = 0; i < str.length(); ++i)
          {
               if (str.charAt(i) == wildStr.charAt(i))
               {
                    allowed = false;
                    return;
               }
          }
          
          // if the string doesnt contain anything from the wildcard string
          // then allowed = false;
          for (int i = 0; i < wildStr.length(); ++i)
          {
               if (str.indexOf(wildStr.charAt(i)) == -1 && wildStr.charAt(i) != '0')
               {
                    allowed = false;
                    return;
               }
          }
     }
     
     void updateConfirmed(String confirmedStr)
     {
          for (int i = 0; i < str.length(); ++i)
          {
               if (confirmedStr.charAt(i) != '0' && str.charAt(i) != confirmedStr.charAt(i))
               {
                    allowed = false;
                    return;
               }
          }
     }
     
     // Some games do not include some words.
     void killWord(String str)
     {
          if (this.str.equalsIgnoreCase(str))
               allowed = false;
     }
}


// Class to handle dictionary etc
class Manager
{
     // list of objects in the class
     private Game game;                                                 // A game Object if the user wants to play
     private Set<Character> usedLetters = new TreeSet<Character>();     // List of used letters
     private Vector<Words> dict = new Vector<Words>();                  // dictionary of words
     
     // letter to position and frequency of position
     private HashMap<Character, HashMap<Integer, Integer>> letterFrequency = new HashMap<Character, HashMap<Integer, Integer>>();
     private HashMap<Character, Integer> letterFreqWithoutPos = new HashMap<Character, Integer>();

     // constructors
     Manager(Scanner scan)
     {
          if (scan == null)
               return;
          
          while (scan.hasNext())          // loop over file till no next
          {
               String temp = scan.next(); // store next in temp string
               if ((Settings.limitOnLoad && Settings.characterLimit == temp.length()) || !Settings.limitOnLoad)
                    dict.add(new Words(temp)); // add to dictionary set (map if include defintion)
          }          
          scan.close();         
     }
     
     // Check the validity of a word against the dictionary 
     boolean isValidWord(String word)
     {
          if (!Settings.requireValidWord)
               return true;
          
          Enumeration<Words> it = dict.elements();
          while (it.hasMoreElements())
          {
               if (word == it.nextElement().getString())
                    return true; 
          }
          return false;
     }
     
     void generateFrequentLetters()
     {
          // clear hashmaps
          letterFrequency.clear();
          letterFreqWithoutPos.clear();
          
          // loop over dictionary
          Enumeration<Words> it = dict.elements();          
          while (it.hasMoreElements())
          {
               Words wrd = it.nextElement();
               String str = wrd.getString();
               for (int i = 0; i < str.length(); ++i)
               {
                    // Current char
                    char ch = str.charAt(i);
                    
                    // increment frequent char
                    Integer secondRef = letterFreqWithoutPos.get(ch);
                    letterFreqWithoutPos.put(new Character(ch), secondRef == null ? new Integer(1) : new Integer(secondRef + 1) );
                    
                    // increment frequent char-position
                    HashMap<Integer, Integer> ref = letterFrequency.get(ch);
                    if (ref == null)
                         ref = new HashMap<Integer, Integer>();     
                    
                    Integer countFromPos = ref.get(i);
                    ref.put(new Integer(i), countFromPos == null ? new Integer(1) : new Integer(countFromPos + 1));
                    letterFrequency.put(new Character(ch), ref);
               }   
          }
          
          // Calculate every words individual frequency
          for (int i = 0; i < dict.size(); ++i)
          {
               Words wrd = dict.elementAt(i);
               if (Settings.freqWithoutPos)
                    wrd.calcFreqWithoutPos(letterFreqWithoutPos);
               else
                    wrd.calcWordFreq(letterFrequency);
                    
          }
     }

     // update a list with a list and then update word validity
     void updateInvalidLetters(Set<Character> chars)
     {
          usedLetters.addAll(chars);           // update set

          for (Words wrd : dict)
               wrd.updateChosenLetters(chars); // Update allowed Letters
     }
     
     Words genMostCommonWord()
     {
          Words finding = new Words();
          for (Words wrd : dict)
          {
               if (wrd.isAllowed() && finding.getWordFrequency() < wrd.getWordFrequency())
                    finding = wrd;
          }          
          return finding;
     }
     
     String genMostCommonStr()
     {
          Words wrd = genMostCommonWord();
          if (wrd == null)
               return "wrd was null";
          return wrd.getString();
     }
     
     boolean startGame()
     {
          game = new Game(dict);
          return game != null;
     }
     
     void sendWildCard(String str)
     {
          if (str.length() < Settings.characterLimit)
          {
               System.out.println("the string size needs to match the character limit. use '0' to mean anything");
               return;
          }
          
          for (Words wrd : dict)
               wrd.updateWildCard(str);
          
     }
     
     void sendKillWord(String str)
     {
          for (Words wrd : dict)
               wrd.killWord(str);          
     }
     
     void sendExact(String str)
     {
          for (Words wrd : dict)
               wrd.updateConfirmed(str);
     }
     
     void sendWord(String str)
     {
          Set<Character> tempSet = new HashSet<Character>();
          if (game == null) // if the game isn't being played and only the helper is in use, then ignore the list
                            // of new characters
          {
               for (int i = 0; i < str.length(); ++i)
                    tempSet.add(str.charAt(i));
               updateInvalidLetters(tempSet);
               
               
          }
          else // playing game, do the actual guess and compare
          {
               GuessedWord gw = game.guessWord(str);
               if (gw.isWinner)
               {
                    game.winGame();
               }
               else
               {
                    for (int i = 0; i < gw.states.size(); ++i)
                    {
                         switch (gw.states.get(i))
                         {
                              case NONE:
                                   tempSet.add(gw.str.charAt(i));
                                   break;
                              case CONTAINS:
                              case EXACT:
                                   break;
                         }
                    }
               }
          }
     }     
}


/* GuessedWord
 * - Designed to store the string that was guessed with a list of states for each character in the string
 * - NONE if no matches, CONTAINS if it is in the string and EXACT for when it is the same position
 */
class GuessedWord
{
     public enum State { NONE, CONTAINS, EXACT }
     
     // the word that was guessed
     public String str;

     // a list of states -> 0 = no word, 1 = ?, 2 = checkmark
     public List<State> states; 
     
     // store if it's a winner preemptively to avoid another loop
     public boolean isWinner = false;
}




// Actual game logic so users can play Wurdle.
class Game
{
     // The word if its playing
     private String gameWord;
     // possibly copy of the dictionary if i want to trim to 5 characters long
     private Vector<Words> dict = new Vector<Words>();
     // A list containing the users "guesses"
     private List<GuessedWord> guesses;
     
     // inits
     Game() { }
     Game(String str) { gameWord = str; }
     Game(Vector<Words> dictCpy)
     {
          if (dictCpy != null)
               dict = dictCpy;
     }
     
     // Generate a word to be used for the game instance of wurdle.\    
     boolean generateWord()
     {
          if (dict != null)
               gameWord = dict.elementAt(ThreadLocalRandom.current().nextInt(0, dict.size() + 1)).getString();
          return true;
     }
     
     void printGameWord()
     {
          System.out.println(gameWord);
     }
     
     void winGame()
     {
          System.out.println("Congratulations, you've guessed the correct word! " + gameWord);
          //resetStates();

          if (dict != null)
               gameWord = dict.elementAt(ThreadLocalRandom.current().nextInt(0, dict.size() + 1)).getString();
     }
     
     // return the custom class that holds the states of each letter.
     GuessedWord guessWord(String str)
     {
          GuessedWord temp = new GuessedWord();
          temp.str = str;
          
          for (int i = 0; i < str.length(); ++i)
          {
               boolean resolved = false;
               for (int j = 0; j < gameWord.length(); ++j)
               {
                    if (gameWord.charAt(j) == str.charAt(i))
                    {
                         if (j == i)
                              temp.states.add(i, GuessedWord.State.EXACT);                         
                         else
                              temp.states.add(i, GuessedWord.State.CONTAINS);                         
                         resolved = true;
                    }
               }
               if (!resolved)
                    temp.states.add(i, GuessedWord.State.NONE);   
          }
          guesses.add(guesses.size(), temp);
          
          if (str.equalsIgnoreCase(gameWord))
               temp.isWinner = true;
          return temp;
     }    
}