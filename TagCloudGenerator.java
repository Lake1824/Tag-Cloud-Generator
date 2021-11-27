import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Creating a Tag Cloud from a input text file from client using OSU components.
 *
 * @author Matthew Lake, Griffin Lavy
 */
public final class TagCloudGenerator {

    private static class StringLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    private static class IntegerLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int result = o2.getValue().compareTo(o1.getValue());
            if (result == 0) {
                // In ascending order
                result = (o1.getKey().compareTo(o2.getKey()));
            }
            return result;
        }
    }

    /**
     * Constructs the header for the HTML page.
     *
     * @param out
     *            the output stream
     * @param name
     *            the input file name
     * @param occurence
     *            the most occurring word and least occurring word
     * @param terms
     *            list of terms to make the tag cloud with and the amount of
     *            those words
     * @param n
     *            number of words to put in the tag cloud
     * @requires out.is_open
     */
    public static void htmlSetup(PrintWriter out, String name, int n,
            List<Map.Entry<String, Integer>> terms, List<Integer> occurence) {
        final int twenty = 20;
        final int thirtySeven = 37;
        final int eleven = 11;
        //Setting up the HTML page
        out.println("<html>");
        out.println("<head>");
        //Name of the input file
        out.println("<title> Top " + n + " words in " + name + "</title>");
        out.println("<link href=\"http://web.cse.ohio-state.edu/software/"
                + "2231/web-sw2/assignments/projects/"
                + "tag-cloud-generator/data/tagcloud.css\""
                + " rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2> Top " + n + " words in " + name + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");
        while (terms.size() > 0) {
            Map.Entry<String, Integer> add = terms.remove(0);
            int fontSize;
            if (occurence.get(0) == occurence.get(1)) {
                fontSize = twenty;
            } else {
                fontSize = ((int) ((double) (add.getValue() - occurence.get(0))
                        / (occurence.get(1) - occurence.get(0)) * thirtySeven)
                        + eleven);
            }
            out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count: " + add.getValue() + "\">"
                    + add.getKey() + "</span>");
        }
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Fills Sequence with lines from txt document input by the client.
     *
     * @param infile
     *            input stream
     * @param seporators
     *            hashSet of separators
     * @param terms
     *            hashMap for words and how many times they occur
     *
     * @requires infile is stream open and the file not to be empty
     *
     * @return hashMap of words and how many times they occur
     */
    public static List<Map.Entry<String, Integer>> fileRead(
            BufferedReader infile, Map<String, Integer> terms,
            HashSet<Character> seporators) {
        //reads file input by client
        String add = "";
        List<Map.Entry<String, Integer>> ret = new ArrayList<Map.Entry<String, Integer>>();
        //reads through file until end of stream
        try {
            add = infile.readLine();
            while (add != null) {
                int position = 0;
                while (position < add.length()) {
                    String token = nextWordOrSeparator(add, position,
                            seporators);
                    //gets word or separator to check if it is within txtline
                    if (!seporators.contains(token.charAt(0))) {
                        //checks if returned token contains a separator
                        token = token.toLowerCase();
                        if (terms.containsKey(token)) {
                            int terminc = terms.get(token);
                            terms.remove(token);
                            terminc++;
                            terms.put(token, terminc);
                        } else {
                            terms.put(token, 1);
                        }

                    }
                    position += token.length();
                    //increments position by the end of the substring
                }
                add = infile.readLine();
            }
            //Creating a iterator for iterating over a Map to create
            //a List of Map pairs
            Set<Map.Entry<String, Integer>> s = terms.entrySet();
            Iterator<Map.Entry<String, Integer>> it = s.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                ret.add(entry);
            }
        } catch (IOException e) {
            System.err.println("Error reading file");
        }
        return ret;
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param seporators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            HashSet<Character> seporators) {
        assert text != null : "Violation of: text is not null";
        assert seporators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String ret = "";
        if (seporators.contains(text.charAt(position))) {
            int i = position;
            //if a separator is detected at the first position of text
            while (i < text.length() && seporators.contains(text.charAt(i))) {
                i++;
            }
            ret = text.substring(position, i);
            //returns the separator within the string
        } else {
            int j = position;
            while (j < text.length() && !seporators.contains(text.charAt(j))) {
                j++;
            }
            ret = text.substring(position, j);
            //returns a substring ignoring separators
        }
        return ret;
        //returns substring to check if ret is within Sequnce terms
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";
        //creates seperators to check for within definition
        for (int i = 0; i < str.length(); i++) {
            if (!strSet.contains(str.charAt(i))) {
                //checks if characters of str is anywhere within strSet
                strSet.add(str.charAt(i));
                //if it is not character i of str is added
            }
        }
    }

    /**
     * Sort List terms and remove the Map pairs that we do not need for tag
     * cloud.
     *
     * @param n
     *            number of terms the client wants on the Tag Cloud
     * @param terms
     *            the unique words and how many times they occur
     */
    private static void nTermOccurences(int n,
            List<Map.Entry<String, Integer>> terms) {
        Comparator<Map.Entry<String, Integer>> intOrder = new IntegerLT();
        terms.sort(intOrder);
        while (terms.size() > n) {
            terms.remove(terms.size() - 1);
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     * @throws IOException
     */
    public static void main(String[] args) {
        //create variables and IO Streams
        String inputFileName = "";
        String fileName = "";
        Scanner input = new Scanner((System.in));
        BufferedReader inputFile;
        PrintWriter outputFile;
        //asking the user for the input and output file names
        System.out.print("Enter the name of your input file: ");
        inputFileName = input.nextLine();
        System.out.print("Enter the name of your output file: ");
        fileName = input.nextLine();
        try {

            outputFile = new PrintWriter(
                    new BufferedWriter(new FileWriter(fileName)));

        } catch (IOException e) {
            System.err.println("Error opening output file");
            //closing stream
            input.close();
            return;
        }
        try {
            inputFile = new BufferedReader(new FileReader(inputFileName));
        } catch (IOException e) {
            System.err.println("Error opening the input file");
            //closing streams
            input.close();
            outputFile.close();
            return;

        }
        Map<String, Integer> termsTemp = new HashMap<String, Integer>();
        //new hashSet to store separators
        HashSet<Character> seporators = new HashSet<Character>();
        generateElements(" ,.@#$%^&*()'<>_-~+=;:{}[]!?\t\n\r\"", seporators);
        List<Map.Entry<String, Integer>> terms = fileRead(inputFile, termsTemp,
                seporators);
        //Getting the number of words for the tag cloud
        System.out.print("Please input number of terms in html: ");
        int n = Integer.parseInt(input.nextLine());
        while (n < 0 || n > terms.size()) {
            System.out.print("Please input a valid value: ");
            n = Integer.parseInt(input.nextLine());
        }
        nTermOccurences(n, terms);
        List<Integer> occurence = new ArrayList();
        //Get the most occurred word and the least
        occurence.add(terms.get(terms.size() - 1).getValue());
        occurence.add(terms.get(0).getValue());
        //put the words in order
        Comparator<Map.Entry<String, Integer>> alphOrder = new StringLT();
        terms.sort(alphOrder);
        //Make the html page
        htmlSetup(outputFile, inputFileName, n, terms, occurence);
        //Close streams
        input.close();
        try {
            inputFile.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream");
        }

        outputFile.close();
    }

}
