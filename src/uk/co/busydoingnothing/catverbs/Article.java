/*
 * Catverbs - A portable Catalan conjugation reference for Android
 * Copyright (C) 2014  Neil Roberts
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

import java.io.IOException;
import java.util.Vector;

class ArticleVariable
{
  int number;
  String value;
}

public class Article
{
  private ArticleLoader loader;
  private Vector<ArticleVariable> variables = new Vector<ArticleVariable> ();
  int parent;

  public Article (ArticleLoader loader,
                  BinaryReader data)
    throws IOException
  {
    this.loader = loader;

    parent = data.readShort ();

    while (true)
      {
        int variableNum = data.readByte ();

        if (variableNum == 0xff)
          break;

        int stringLength = data.readByte ();
        byte valueBytes[] = new byte[stringLength];
        data.readAll (valueBytes);

        ArticleVariable variable = new ArticleVariable ();
        variable.number = variableNum;
        variable.value = new String (valueBytes);

        variables.add (variable);
      }
  }

  private String getValueNoRecursion (int variableNum)
  {
    int i;

    for (i = 0; i < variables.size (); i++)
      {
        ArticleVariable variable = variables.elementAt (i);

        if (variable.number == variableNum)
          return variable.value;
      }

    return null;
  }

  private void expandSubstitutions (CharSequence value, StringBuilder buffer)
    throws IOException
  {
    int i;
    char ch;

    for (i = 0; i < value.length (); i++)
      {
        ch = value.charAt (i);

        if (ch >= 0xe000 && ch <= 0xf8ff) /* private use range */
          getValue (ch - 0xe000, buffer);
        else
          buffer.append (ch);
      }
  }

  public void getValue (int variable, StringBuilder buffer)
    throws IOException
  {
    Article article = this;

    while (true)
      {
        String value = article.getValueNoRecursion (variable);

        if (value != null)
          {
            expandSubstitutions (value, buffer);
            break;
          }

        if (article.parent == 0xffff)
          throw new IllegalStateException ("Missing value for variable " +
                                           variable);

        article = loader.loadArticle (article.parent);
      }
  }

  public CharSequence getValue (int variable)
    throws IOException
  {
    StringBuilder buffer = new StringBuilder ();

    getValue (variable, buffer);

    return buffer;
  }
}
