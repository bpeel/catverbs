/*
 * Catverbs - A portable Catalan conjugation reference for Android
 * Copyright (C) 2012, 2014  Neil Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.busydoingnothing.catverbs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

class TrieStack
{
  private int[] data;
  private int size;

  public TrieStack ()
  {
    this.data = new int[64];
    this.size = 0;
  }

  public int getTopStart ()
  {
    return data[size - 3];
  }

  public int getTopEnd ()
  {
    return data[size - 2];
  }

  public int getTopStringLength ()
  {
    return data[size - 1];
  }

  public void pop ()
  {
    size -= 3;
  }

  public boolean isEmpty ()
  {
    return size <= 0;
  }

  public void push (int start,
                    int end,
                    int stringLength)
  {
    /* If there isn't enough space in the array then we'll double its
     * size. The size of the array is initially chosen to be quite
     * large so this should probably never happen */
    if (size + 3 >= data.length)
      {
        int[] newData = new int[data.length * 2];
        System.arraycopy (data, 0, newData, 0, data.length);
        data = newData;
      }

    data[size++] = start;
    data[size++] = end;
    data[size++] = stringLength;
  }
}

public class Trie
{
  private byte data[];

  private static void readAll (InputStream stream,
                               byte[] data,
                               int offset,
                               int length)
    throws IOException
  {
    while (length > 0)
      {
        int got = stream.read (data, offset, length);

        if (got == -1)
          throw new IOException ("Unexpected end of file");
        else
          {
            offset += got;
            length -= got;
          }
      }
  }

  private static final int extractInt (byte[] data,
                                       int offset)
  {
    return (((data[offset + 0] & 0xff) << 0) |
            ((data[offset + 1] & 0xff) << 8) |
            ((data[offset + 2] & 0xff) << 16) |
            ((data[offset + 3] & 0xff) << 24));
  }

  private static final int extractShort (byte[] data,
                                         int offset)
  {
    return (((data[offset + 0] & 0xff) << 0) |
            ((data[offset + 1] & 0xff) << 8));
  }

  private static final int extractVarInt (byte[] data,
                                          int offset)
  {
    int pos = 0;
    int value = 0;

    while (true)
      {
        value |= (data[offset] & 0x7f) << pos;

        if ((data[offset] & 0x80) == 0)
          return value;

        pos += 7;
        offset++;
      }
  }

  private static final int getVarIntLength (byte[] data,
                                            int offset)
  {
    int length = 1;

    while ((data[offset++] & 0x80) != 0)
      length++;

    return length;
  }

  public Trie (InputStream dataStream)
    throws IOException
  {
    byte lengthBytes[] = new byte[8];
    int lengthPos = 0;
    int totalLength;
    int value;

    /* Read enough bytes to get the total length */
    while (true)
      {
        value = dataStream.read ();
        if (value == -1)
          throw new IOException("Unexpected EOF");

        lengthBytes[lengthPos++] = (byte) value;

        if ((value & 0x80) == 0)
          break;
      }

    totalLength = extractVarInt (lengthBytes, 0) >> 1;

    /* Create a byte array big enough to hold the entire file and copy
     * the length we just read into the beginning */
    data = new byte[totalLength + lengthPos];
    System.arraycopy (lengthBytes, 0, data, 0, lengthPos);

    /* Read the rest of the data */
    readAll (dataStream, data, lengthPos, totalLength);
  }

  /* Gets the number of bytes needed for a UTF-8 sequence which begins
   * with the given byte */
  private static int getUtf8Length (byte firstByte)
  {
    if (firstByte >= 0)
      return 1;
    if ((firstByte & 0xe0) == 0xc0)
      return 2;
    if ((firstByte & 0xf0) == 0xe0)
      return 3;
    if ((firstByte & 0xf8) == 0xf0)
      return 4;
    if ((firstByte & 0xfc) == 0xf8)
      return 5;

    return 6;
  }

  private static boolean compareArray (byte[] a,
                                       int aOffset,
                                       byte[] b,
                                       int bOffset,
                                       int length)
  {
    while (length-- > 0)
      if (a[aOffset++] != b[bOffset++])
        return false;

    return true;
  }

  private String getCharacter (int offset)
  {
    return new String (data, offset, getUtf8Length (data[offset]));
  }

  /* Searches the trie for words that begin with 'prefix'. The results
   * array is filled with the results. If more results are available
   * than the length of the results array then they are ignored. If
   * less are available then the remainder of the array is untouched.
   * The method returns the number of results found */
  public int search (String prefix,
                     SearchResult[] results)
  {
    /* Convert the string to unicode to make it easier to compare with
     * the unicode characters in the trie. 'getBytes' with no
     * parameters converts the string to the default charset. This
     * assumes the default charset is always UTF-8 which seems to be
     * the case on Android */
    byte[] prefixBytes = prefix.getBytes ();

    int trieStart = 0;
    int prefixOffset = 0;

    while (prefixOffset < prefixBytes.length)
      {
        int characterLen = getUtf8Length (prefixBytes[prefixOffset]);
        int childStart;

        /* Find the position of the character within the node. This is
         * after the var int */
        int nodeCharacterPos = trieStart + getVarIntLength (data, trieStart);

        /* Get the total length of this node */
        int offset = extractVarInt (data, trieStart);

        /* Skip the character for this node */
        childStart = getUtf8Length (data[nodeCharacterPos]) + nodeCharacterPos;

        /* If the low bit in the offset is set then it is followed by
         * the matching articles which we want to skip */
        if ((offset & 1) != 0)
          {
            boolean hasNext;

            do
              {
                hasNext = (data[childStart + 1] & 0x80) != 0;
                boolean hasDisplayName = (data[childStart + 1] & 0x40) != 0;

                childStart += 2;

                if (hasDisplayName)
                  childStart += (data[childStart] & 0xff) + 1;
              } while (hasNext);
          }

        offset >>= 1;

        int trieEnd = nodeCharacterPos + offset;

        trieStart = childStart;

        /* trieStart is now pointing into the children of the
         * selected node. We'll scan over these until we either find a
         * matching character for the next character of the prefix or
         * we hit the end of the node */
        while (true)
          {
            /* If we've reached the end of the node then we haven't
             * found a matching character for the prefix so there are
             * no results */
            if (trieStart >= trieEnd)
              return 0;

            /* If we've found a matching character then start scanning
             * into this node */
            if (compareArray (prefixBytes,
                              prefixOffset,
                              data,
                              trieStart + getVarIntLength (data, trieStart),
                              characterLen))
              break;
            /* Otherwise skip past the node to the next sibling */
            else
              trieStart += ((extractVarInt (data, trieStart) >> 1) +
                            getVarIntLength (data, trieStart));
          }

        prefixOffset += characterLen;
      }

    StringBuilder stringBuf = new StringBuilder (prefix);

    /* trieStart is now pointing at the last node with this string.
     * Any children of that node are therefore extensions of the
     * prefix. We can now depth-first search the tree to get them all
     * in sorted order */

    TrieStack stack = new TrieStack ();

    stack.push (trieStart,
                trieStart +
                (extractVarInt (data, trieStart) >> 1) +
                getVarIntLength (data, trieStart),
                stringBuf.length ());

    int numResults = 0;
    boolean firstChar = true;

    while (numResults < results.length &&
           !stack.isEmpty ())
      {
        int searchStart = stack.getTopStart ();
        int searchEnd = stack.getTopEnd ();

        stringBuf.setLength (stack.getTopStringLength ());

        stack.pop ();

        int offset = extractVarInt (data, searchStart);
        int characterPos = searchStart + getVarIntLength (data, searchStart);
        int characterLen = getUtf8Length (data[characterPos]);
        int childrenStart = characterPos + characterLen;
        int oldLength = stringBuf.length ();

        if (firstChar)
          firstChar = false;
        else
          stringBuf.append (new String (data,
                                        characterPos,
                                        characterLen));

        /* If this is a complete word then add it to the results */
        if ((offset & 1) != 0)
          {
            boolean hasNext = true;

            while (hasNext && numResults < results.length)
              {
                int article = ((data[childrenStart] & 0xff) |
                               ((data[childrenStart + 1] & 0xff) << 8));
                hasNext = (article & 0x8000) != 0;
                boolean hasDisplayName = (article & 0x4000) != 0;

                childrenStart += 2;

                article &= 0x3fff;

                String word;
                if (hasDisplayName)
                  {
                    int len = data[childrenStart] & 0xff;
                    word = new String (data,
                                       childrenStart + 1,
                                       len);
                    childrenStart += len + 1;
                  }
                else
                  word = stringBuf.toString ();

                results[numResults++] = new SearchResult (word, article);
              }

          }

        offset >>= 1;

        /* If there is a sibling then make sure we continue from that
         * after we've descended through the children of this node */
        if (characterPos + offset < searchEnd)
          stack.push (characterPos + offset, searchEnd, oldLength);

        /* Push a search for the children of this node */
        if (childrenStart < characterPos + offset)
            stack.push (childrenStart,
                        characterPos + offset,
                        stringBuf.length ());
      }

    return numResults;
  }

  /* Test program */
  public static void main (String[] args)
    throws IOException
  {
    if (args.length != 2)
      {
        System.err.println ("Usage: java Trie <index> <prefix>");
        System.exit (1);
      }

    FileInputStream inputStream = new FileInputStream (args[0]);

    /* Skip the article offsets */
    byte[] articleCountBytes = new byte[2];
    readAll(inputStream, articleCountBytes, 0, 2);
    int articleCount = ((articleCountBytes[0] & 0xff) |
                        ((articleCountBytes[1] & 0xff) << 8));
    byte[] articleOffsets = new byte[articleCount * 4];
    readAll(inputStream, articleOffsets, 0, articleOffsets.length);

    Trie trie = new Trie (inputStream);

    SearchResult result[] = new SearchResult[100];

    int numResults = trie.search (args[1], result);

    for (int i = 0; i < numResults; i++)
      System.out.println (result[i].getWord () + ": " +
                          result[i].getArticle ());
  }
}
