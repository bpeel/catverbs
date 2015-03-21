/*
 * Catverbs - A portable Catalan conjugation reference for Android
 * Copyright (C) 2013, 2015  Neil Roberts
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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

/* This activity is just like a landing page to select the right
 * initial activity. */

public class StartActivity extends Activity
{
  private static final String CATALAN_DICTIONARY_HOST =
    "www.catalandictionary.org";
  private static final String[] REFLEXIVE_ENDINGS = { "-se", "'s" };

  private String getIntendedSearch()
  {
    Intent intent = getIntent ();

    if (intent == null)
      return null;

    String action = intent.getAction ();

    if (action == null || !Intent.ACTION_VIEW.equals (action))
      return null;

    Uri data = intent.getData ();

    if (!data.getScheme ().equals ("http") ||
        !data.getHost ().equals (CATALAN_DICTIONARY_HOST))
      return null;

    String query = data.getQueryParameter ("q");

    if (query == null ||
        !query.startsWith ("conjugator/"))
      return null;

    String verb = query.substring (11);

    int verbEnd = verb.indexOf ('/');

    if (verbEnd != -1)
      verb = verb.substring (0, verbEnd);

    verb = Uri.decode (verb);

    for (int i = 0; i < REFLEXIVE_ENDINGS.length; i++)
      {
        if (verb.endsWith (REFLEXIVE_ENDINGS[i]))
          {
            verb = verb.substring (0,
                                   verb.length () -
                                   REFLEXIVE_ENDINGS[i].length ());
            break;
          }
      }

    return verb;
  }

  private boolean tryExactArticleSearch (String searchString)
  {
    Trie trie = Trie.getDefault (this);

    SearchResult[] results = new SearchResult[3];

    int numResults = trie.search (searchString, results);

    for (int i = 0; i < numResults; i++)
      {
        if (results[i].getWord ().equals (searchString))
          {
            MenuHelper.goArticle (this, results[i].getArticle ());
            return true;
          }
      }

    return false;
  }

  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    String searchString = getIntendedSearch ();

    if (searchString == null || !tryExactArticleSearch (searchString))
      MenuHelper.goSearch (this, searchString);

    /* Finish this activity to get it out of the call stack */
    finish ();
  }
}
