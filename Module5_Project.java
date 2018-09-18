package module5_project;
import java.io.*;
import java.util.*;

/**
 * Student Name: Jorge Velez & Alexander Campaneria
 * Panther ID: 3512474 & 3310249
 * Date: 6/7/18
 * Section: Online
 */


class Data{
    public String attNames[];
    public String tuples[][];
}
public class Module5_Project {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    
    static Data testData;
    static int numTuples;
    static int numAttributes;
    
    public static void main(String[] args) throws Exception {
        
        //read in training data
        File f = new File("training.txt");
        //System.out.println(f.getAbsolutePath());
        Scanner scan = new Scanner(f);
        String attributes = scan.nextLine();
        StringTokenizer tokenizer = new StringTokenizer(attributes);
        
        numAttributes = tokenizer.countTokens();
        numTuples = countLines();
        
        testData = new Data();
        testData.attNames = new String[numAttributes];
        testData.tuples = new String[numTuples][numAttributes];
        for(int i = 0; i < numAttributes; i++){
            testData.attNames[i] = tokenizer.nextToken();
            //System.out.print(testData.attNames[i] + " ");
        }
        //System.out.println("");
        for(int i = 0; scan.hasNextLine(); i++){
            String line = scan.nextLine();
            //System.out.println(line);
            StringTokenizer tok = new StringTokenizer(line);
            for(int j = 0; tok.hasMoreTokens(); j++){
                testData.tuples[i][j] = tok.nextToken();
                //System.out.print(testData.tuples[i][j] + " ");
            }
            //System.out.println("");
        }
        
        //generate decision tree using information gain and store the index of the attribute
        int maxIG = generate_decision_tree();
        
        //prompt user for input
        String userInput[] = promptUser();        
        
        //print correct table based on user input
        classifyUser(userInput, maxIG);
        
    }
    
    //count the number of lines in the training data
    static int countLines() throws FileNotFoundException{
        Scanner counter = new Scanner(new File("training.txt"));
        counter.nextLine();
        int count;
        
        for(count = 0; counter.hasNextLine(); count++){
            counter.nextLine();
        }
        
        return count;
    }
    
    //generates decision tree and returns the index of the attribute selected by information gain
    static int generate_decision_tree(){
        //calculate entropy for information gain
        double entropy = calcEntropy();
        
        //loop through attributes and calculate total information gain
        int maxIGindex = -1;
        double maxIGvalue = -1;
        
        for(int i = 0; i < numAttributes -1; i++){
            double currentIG = 0;
            String[][] attribute = countAttributes(getColumn(testData.tuples, i));
            
            for(String[] option: attribute){
                double optionCount;
                optionCount = Integer.parseInt(option[1]);
                //System.out.println(option[0] + " count: " + optionCount);
                currentIG += (optionCount/numTuples * calcEntropy(i, option[0]));
            }
            
            currentIG = entropy - currentIG;
            
            if(currentIG > maxIGvalue){
                maxIGvalue = currentIG;
                maxIGindex = i;
            }
        }
        
        System.out.println("Part 2: Decision Tree:");
        System.out.println(testData.attNames[maxIGindex] + "?");
        
        String selectedAttribute[][] = countAttributes(getColumn(testData.tuples, maxIGindex));
        
        for(String[] option: selectedAttribute){
            System.out.format("---------- %s ----------\n", option[0]);
            for(int i = 0; i < numAttributes; i++){
                if(i != maxIGindex){
                    System.out.format("%-15s", testData.attNames[i]);
                }
            }
            System.out.println("");
            
            String[][] selectedArray = selectOption(testData.tuples, maxIGindex, option[0]);
            for(String[] row : selectedArray){
                for(String column: row){
                    if(!column.equals(option[0])){
                        System.out.format("%-15s" ,column);
                    }
                }
                System.out.println("");
            }
        }
        
        return maxIGindex;
    }
    
    static String[] promptUser(){
        String userInput[] = new String[numAttributes -1];
        Scanner scan = new Scanner(System.in);
        
        System.out.println("\n-------------------------------\nPart 3: User Input\n");
        
        for(int i = 0; i < numAttributes - 1; i++){
            System.out.println("Please enter attribute (" + testData.attNames[i] + "): ");
            userInput[i] = scan.next();
        }
        
        return userInput;
    }
    
    
    //Get a single attribute from the data set
    static String[] getColumn(String[][] array2D, int column){
        String array1D[] = new String[array2D.length];
        
        for(int i = 0; i < array2D.length; i++){
            array1D[i] = array2D[i][column];
        }
        
        return array1D;
    }
    
    
    //Counts all different possibilities for a given attribute and how often each occurs
    static String[][] countAttributes(String[] attributes){
        String result[][];
        List asList = Arrays.asList(attributes);
        Set<String> mySet = new HashSet<String>(asList);
        
        int count = 0;
        for(String s: mySet){
            count++;
        }
        result = new String[count][2];
        count = 0;
        
        for(String s: mySet){
            result[count][0] = s;
            result[count++][1] = "" + Collections.frequency(asList,s);
        }
        
        return result;
    }
    
    //Calculate entropy for Information Gain, assumes discrete value is last attribute
    static double calcEntropy(){
        double entropy = 0;
        
        String attributeCount[][];
        attributeCount = countAttributes(getColumn(testData.tuples, numAttributes-1));
        
        for(int i = 0; i < attributeCount.length; i++){
            double attCount = Integer.parseInt(attributeCount[i][1]);
            entropy += -(attCount/numTuples) * (Math.log(attCount/numTuples)/Math.log(2));
        }

        return entropy;
    }
    
    //Help calculate information needed for each attribute
    static double calcEntropy(int index, String option){
        double entropy = 0;
        String[][] optionArray = selectOption(testData.tuples, index, option);
        
        String attributeCount[][];
        attributeCount = countAttributes(getColumn(optionArray, numAttributes-1));

        //System.out.println(option);
        for(int i = 0; i < attributeCount.length; i++){
            //System.out.println(attributeCount[i][0] + ": " + attributeCount[i][1]);
            double attCount = Integer.parseInt(attributeCount[i][1]);
            entropy += -(attCount/optionArray.length) * (Math.log(attCount/optionArray.length)/Math.log(2));
        }

        return entropy;
    }
    
    //removes rows from data that do not have a certain option in a given attribute index
    static String[][] selectOption(String[][] original, int index, String option){
        String result[][];
        int numRows = 0;
        int count = 0;
        
        //find out how many rows we need to store
        String attributeCount[][];
        attributeCount = countAttributes(getColumn(original, index));
        for(String[] row: attributeCount){
            if (row[0].equals(option)){
                numRows = Integer.parseInt(row[1]);
            }
        }
        
        
        //initiate new array and fill contents
        result = new String[numRows][original[0].length];
       
        for (String[] row : original) {
            if (row[index].equals(option)) {
                result[count++] = row;
            }
        }
        
        return result;
    }
   
   
    static void classifyUser(String userInput[], int maxIG){
        
        System.out.println("---------- Results: ----------");
        for(int i = 0; i < numAttributes; i++){
            if(i != maxIG){
                System.out.format("%-15s", testData.attNames[i]);
            }
        }
        System.out.println("");
        String[][] selectedArray = selectOption(testData.tuples, maxIG, userInput[maxIG]);
        for(String[] row : selectedArray){
            for(String column: row){
                if(!column.equals(userInput[maxIG])){
                    System.out.format("%-15s" ,column);
                }
            }
            System.out.println("");
        }
    }
}
