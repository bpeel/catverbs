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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.util.Vector;

public class ArticleActivity extends Activity
{
  public static final String EXTRA_ARTICLE_NUMBER =
    "uk.co.busydoingnothing.catverbs.ArticleNumber";
  private Vector<TextView> sectionHeaders;
  private Vector<TextView> sectionContents;

  private DelayedScrollView scrollView;
  private View articleView;
  private int articleNumber;

  private RelativeLayout layout;

  private boolean stopped;
  private boolean reloadQueued;

  private LinearLayout loadArticle (int articleNum)
    throws IOException
  {
    Article article = ArticleLoader.getDefault (this).loadArticle (articleNum);
    LinearLayout layout = new LinearLayout (this);

    setTitle (article.getValue (ArticleVariables.INFINITIVE));

    return layout;
  }

  private void loadIntendedArticle ()
  {
    Intent intent = getIntent ();

    sectionHeaders.setSize (0);
    sectionContents.setSize (0);

    if (intent != null)
      {
        int article = intent.getIntExtra (EXTRA_ARTICLE_NUMBER, -1);

        if (article != -1)
          {
            try
              {
                this.articleNumber = article;
                if (articleView != null)
                  scrollView.removeView (articleView);
                articleView = loadArticle (article);
                scrollView.addView (articleView);
              }
            catch (IOException e)
              {
                throw new IllegalStateException ("Error while loading an " +
                                                 "article", e);
              }
          }
      }
  }

  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    setContentView (R.layout.article);

    scrollView = (DelayedScrollView) findViewById (R.id.article_scroll_view);
    layout = (RelativeLayout) findViewById (R.id.article_layout);

    sectionHeaders = new Vector<TextView> ();
    sectionContents = new Vector<TextView> ();

    stopped = true;
    reloadQueued = true;
  }

  @Override
  public void onStart ()
  {
    super.onStart ();

    stopped = false;

    if (reloadQueued)
      {
        reloadQueued = false;
        loadIntendedArticle ();
      }
  }

  @Override
  public void onStop ()
  {
    stopped = true;

    super.onStop ();
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu)
  {
    MenuInflater inflater = getMenuInflater ();

    inflater.inflate (R.menu.article_menu, menu);

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
  public boolean onKeyDown (int keyCode,
                            KeyEvent event)
  {
    if (keyCode == KeyEvent.KEYCODE_SEARCH)
      {
        MenuHelper.goSearch (this);
        return true;
      }

    return super.onKeyDown (keyCode, event);
  }
}
