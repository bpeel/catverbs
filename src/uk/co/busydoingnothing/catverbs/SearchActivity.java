/*
 * Catverbs - A portable Catalan conjugation reference for Android
 * Copyright (C) 2012, 2013, 2014  Neil Roberts
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

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends ListActivity
  implements TextWatcher
{
  private static final String CATALAN_DICTIONARY_HOST =
    "www.catalandictionary.org";
  private static final String[] REFLEXIVE_ENDINGS = { "-se", "'s" };

  private SearchAdapter searchAdapter;

  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.search);

    ListView lv = getListView ();

    searchAdapter = new SearchAdapter (this, Trie.getDefault (this));

    lv.setAdapter (searchAdapter);

    TextView tv = (TextView) findViewById (R.id.search_edit);
    tv.addTextChangedListener (this);

    lv.setOnItemClickListener (new AdapterView.OnItemClickListener ()
      {
        public void onItemClick (AdapterView<?> parent,
                                 View view,
                                 int position,
                                 long id)
        {
          SearchAdapter adapter =
            (SearchAdapter) parent.getAdapter ();
          SearchResult result = adapter.getItem (position);
          Intent intent = new Intent (view.getContext (),
                                      ArticleActivity.class);
          intent.putExtra (ArticleActivity.EXTRA_ARTICLE_NUMBER,
                           result.getArticle ());
          startActivity (intent);
        }
      });
  }

  private void setIntendedSearch(TextView tv)
  {
    Intent intent = getIntent ();

    if (intent == null)
      return;

    String action = intent.getAction ();

    if (action == null || !Intent.ACTION_VIEW.equals (action))
      return;

    Uri data = intent.getData ();

    if (!data.getScheme ().equals ("http") ||
        !data.getHost ().equals (CATALAN_DICTIONARY_HOST))
      return;

    String query = data.getQueryParameter ("q");

    if (query == null ||
        !query.startsWith ("conjugator/"))
      return;

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

    tv.setText (verb);
  }

  @Override
  public void onStart ()
  {
    super.onStart ();

    View tv = findViewById (R.id.search_edit);

    setIntendedSearch ((TextView) tv);

    tv.requestFocus ();

    InputMethodManager imm =
      (InputMethodManager) getSystemService (INPUT_METHOD_SERVICE);

    if (imm != null)
      imm.showSoftInput (tv,
                         0, /* flags */
                         null /* resultReceiver */);
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu)
  {
    MenuInflater inflater = getMenuInflater ();

    inflater.inflate (R.menu.search_menu, menu);

    menu.removeItem (R.id.menu_search);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item)
  {
    if (MenuHelper.onOptionsItemSelected (this, item))
      return true;

    return super.onOptionsItemSelected (item);
  }

  @Override
  protected Dialog onCreateDialog (int id)
  {
    return MenuHelper.onCreateDialog (this, id);
  }

  @Override
  public void afterTextChanged (Editable s)
  {
    searchAdapter.getFilter ().filter (s);
  }

  @Override
  public void beforeTextChanged (CharSequence s,
                                 int start,
                                 int count,
                                 int after)
  {
  }

  @Override
  public void onTextChanged (CharSequence s,
                             int start,
                             int before,
                             int count)
  {
  }
}
