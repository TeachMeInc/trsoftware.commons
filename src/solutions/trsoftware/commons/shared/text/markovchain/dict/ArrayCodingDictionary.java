package solutions.trsoftware.commons.shared.text.markovchain.dict;

import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.ArrayList;

/**
 * An implementation of CodingDictionary which uses an array of the strings,
 * the indices of which give the encoding of the words.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
public abstract class ArrayCodingDictionary<K,V extends Number> implements CodingDictionary<V> {

  /** Words indexed by their integer code */
  private ArrayList<K> words = new ArrayList<K>();

  // subclasses should override these two methods to handle the datatypes they wish to support

  protected abstract K encodeKey(String key);
  protected abstract String decodeKey(K key);

  protected abstract V makeValue(int value);

  /** Returns a canonical short code for the word */
  protected final V getOrInsert(K word) {
    int nextIndex = words.size();
    int result = lookup(word, nextIndex);
    if (nextIndex == result) {
      words.add(result, word);
    }
    return makeValue(result);
  }

  /**
   * Looks up the given word and returns its existing index.  If not found,
   * returns the provided nextIndex and adds it to the lookup table if applicable.
   */
  protected int lookup(K word, int nextIndex) {
    int index = ArrayUtils.linearSearch(words.toArray(), word);
    if (index < 0)
      return nextIndex;
    return index;
  }

  /** Gets the entry indexed by the given code */
  protected K get(V code) {
    return words.get(code.intValue());
  }

  /**
   * Returns a canonical code for the word
   * Subclasses where K != String should override this method.
   */
  public V encode(String word) {
    return getOrInsert(encodeKey(word));
  }

  /**
   * Translates a canonical code back to the word.
   * Subclasses where K != String should override this method.
   */
  public String decode(V code) {
    return decodeKey(get(code));
  }

  /** @return the number of unique words in the dictionary */
  public int size() {
    return words.size();
  }

}