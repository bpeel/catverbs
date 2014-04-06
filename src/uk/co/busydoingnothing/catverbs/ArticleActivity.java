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

class ArticlePart
{
  public int resourceId;
  public int[] variableIds;

  ArticlePart (int resourceId,
               int ... variableIds)
  {
    this.resourceId = resourceId;
    this.variableIds = variableIds;
  }
}

public class ArticleActivity extends Activity
{
  public static final String EXTRA_ARTICLE_NUMBER =
    "uk.co.busydoingnothing.catverbs.ArticleNumber";

  private int articleNumber;

  private boolean stopped;
  private boolean reloadQueued;

  private static final ArticlePart articleParts[] =
  {
    new ArticlePart (R.id.present_indicative_content,
                     ArticleVariables.PI_JO,
                     ArticleVariables.PI_TU,
                     ArticleVariables.PI_ELL,
                     ArticleVariables.PI_NOSALTRES,
                     ArticleVariables.PI_VOSALTRES,
                     ArticleVariables.PI_ELLS),
    new ArticlePart (R.id.imperfect_indicative_content,
                     ArticleVariables.II_JO,
                     ArticleVariables.II_TU,
                     ArticleVariables.II_ELL,
                     ArticleVariables.II_NOSALTRES,
                     ArticleVariables.II_VOSALTRES,
                     ArticleVariables.II_ELLS),
    new ArticlePart (R.id.future_indicative_content,
                     ArticleVariables.FUTURE_JO,
                     ArticleVariables.FUTURE_TU,
                     ArticleVariables.FUTURE_ELL,
                     ArticleVariables.FUTURE_NOSALTRES,
                     ArticleVariables.FUTURE_VOSALTRES,
                     ArticleVariables.FUTURE_ELLS),
    new ArticlePart (R.id.conditional_indicative_content,
                     ArticleVariables.COND_JO,
                     ArticleVariables.COND_TU,
                     ArticleVariables.COND_ELL,
                     ArticleVariables.COND_NOSALTRES,
                     ArticleVariables.COND_VOSALTRES,
                     ArticleVariables.COND_ELLS),
    new ArticlePart (R.id.present_subjunctive_content,
                     ArticleVariables.PS_JO,
                     ArticleVariables.PS_TU,
                     ArticleVariables.PS_ELL,
                     ArticleVariables.PS_NOSALTRES,
                     ArticleVariables.PS_VOSALTRES,
                     ArticleVariables.PS_ELLS),
    new ArticlePart (R.id.imperfect_subjunctive_content,
                     ArticleVariables.IS_JO,
                     ArticleVariables.IS_TU,
                     ArticleVariables.IS_ELL,
                     ArticleVariables.IS_NOSALTRES,
                     ArticleVariables.IS_VOSALTRES,
                     ArticleVariables.IS_ELLS),
    new ArticlePart (R.id.imperative_content,
                     ArticleVariables.IMP_JO,
                     ArticleVariables.IMP_TU,
                     ArticleVariables.IMP_ELL,
                     ArticleVariables.IMP_NOSALTRES,
                     ArticleVariables.IMP_VOSALTRES,
                     ArticleVariables.IMP_ELLS),
    new ArticlePart (R.id.gerund_content,
                     ArticleVariables.GERUND),
    new ArticlePart (R.id.participle_content,
                     ArticleVariables.M_PARTICIPLE,
                     ArticleVariables.F_PARTICIPLE,
                     ArticleVariables.PM_PARTICIPLE,
                     ArticleVariables.PF_PARTICIPLE)
  };

  private void loadArticle (int articleNum)
    throws IOException
  {
    Article article = ArticleLoader.getDefault (this).loadArticle (articleNum);

    setTitle (article.getValue (ArticleVariables.INFINITIVE));

    StringBuilder buffer = new StringBuilder ();

    for (ArticlePart part : articleParts)
      {
        buffer.setLength (0);

        for (int variable : part.variableIds)
          {
            article.getValue (variable, buffer);
            buffer.append ('\n');
          }

        TextView tv = (TextView) findViewById (part.resourceId);
        tv.setText (buffer);
      }
  }

  private void loadIntendedArticle ()
  {
    Intent intent = getIntent ();

    if (intent != null)
      {
        int article = intent.getIntExtra (EXTRA_ARTICLE_NUMBER, -1);

        if (article != -1)
          {
            try
              {
                this.articleNumber = article;
                loadArticle (article);
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
