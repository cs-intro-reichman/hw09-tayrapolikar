import java.util.HashMap;
import java.util.Random;


public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {

        String window = "";
        char chr = ' ' ;

        In in = new In(fileName);

        for (int i = 0; i < windowLength; i++)
        {
            chr = in.readChar();
            window += chr;
        }

        while (!in.isEmpty())
        {
            chr = in.readChar();
            if (CharDataMap.containsKey(window))
            {
                CharDataMap.get(window).update(chr);
            }
            else
            {
                List probs = new List();
                probs.addFirst(chr);
                CharDataMap.put(window, probs);
            }
            window = window.substring(1) + chr;
        }

        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);


    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    public void calculateProbabilities(List probs) {
        int totalCountOfChar = 0;

        for (int i = 0; i < probs.getSize(); i++) {
            CharData data = probs.get(i);
            totalCountOfChar += data.count;
        }

        double cumulativeProb = 0.0;

        for (int i = 0; i < probs.getSize(); i++) {
            CharData data = probs.get(i);
            double probability = (double) data.count / totalCountOfChar;
            data.p = probability;

            cumulativeProb += probability;
            data.cp = cumulativeProb;
        }

        if (probs.getSize() > 0) {
            CharData lastData = probs.get(probs.getSize() - 1);
            lastData.cp = 1.0;
        }
    }



    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();

        for (int i = 0; i < probs.getSize(); i++) {
            CharData data = probs.get(i);
            if (data.cp >= r) {
                return data.chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
    }

    /*
     * Generates a random text, based on the probabilities that were learned during training.
     * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
     * doesn't appear as a key in Map, we generate no text and return only the initial text.
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        String window = "";
        String mytext = initialText;
        char chr;

        if (windowLength > initialText.length() || initialText.length() >= textLength)
        {
            return initialText;
        }
        else
        {
            window = initialText.substring(initialText.length() - windowLength);
            while (mytext.length() - windowLength < textLength)
            {
                if (CharDataMap.containsKey(window))
                {
                    chr = getRandomChar(CharDataMap.get(window));
                    mytext += chr;
                    window = window.substring(1) + chr;
                }
                else
                {
                    return mytext;
                }
            }
            return mytext;
        }

    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {

    }
}


