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
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import java.io.IOException;
import java.util.Vector;

class ArticlePart
{
  public int resourceId;
  public Object[] items;

  ArticlePart (int resourceId,
               Object ... items)
  {
    this.resourceId = resourceId;
    this.items = items;
  }
}

class ArticleTab
{
  public String tag;
  public int name;
  public int resourceId;

  ArticleTab (String tag,
              int name,
              int resourceId)
  {
    this.tag = tag;
    this.name = name;
    this.resourceId = resourceId;
  }
}

public class ArticleActivity extends TabActivity
  implements TabHost.TabContentFactory
{
  public static final String EXTRA_ARTICLE_NUMBER =
    "uk.co.busydoingnothing.catverbs.ArticleNumber";

  private int articleNumber;

  private boolean stopped;
  private boolean reloadQueued;

  private View pageViews[];

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
    new ArticlePart (R.id.gerund_content,
                     ArticleVariables.GERUND),
    new ArticlePart (R.id.participle_content,
                     ArticleVariables.M_PARTICIPLE,
                     ArticleVariables.F_PARTICIPLE,
                     ArticleVariables.PM_PARTICIPLE,
                     ArticleVariables.PF_PARTICIPLE),
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
    new ArticlePart (R.id.simple_past_indicative_content,
                     ArticleVariables.SPI_JO,
                     ArticleVariables.SPI_TU,
                     ArticleVariables.SPI_ELL,
                     ArticleVariables.SPI_NOSALTRES,
                     ArticleVariables.SPI_VOSALTRES,
                     ArticleVariables.SPI_ELLS),
    new ArticlePart (R.id.perfect_indicative_content,
                     "he ", ArticleVariables.M_PARTICIPLE,
                     "has ", ArticleVariables.M_PARTICIPLE,
                     "ha ", ArticleVariables.M_PARTICIPLE,
                     "hem ", ArticleVariables.M_PARTICIPLE,
                     "heu ", ArticleVariables.M_PARTICIPLE,
                     "han ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.pluperfect_indicative_content,
                     "havia ", ArticleVariables.M_PARTICIPLE,
                     "havias ", ArticleVariables.M_PARTICIPLE,
                     "havia ", ArticleVariables.M_PARTICIPLE,
                     "haviem ", ArticleVariables.M_PARTICIPLE,
                     "havieu ", ArticleVariables.M_PARTICIPLE,
                     "havien ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.periphrastic_past_indicative_content,
                     "vaig ", ArticleVariables.INFINITIVE,
                     "vas ", ArticleVariables.INFINITIVE,
                     "va ", ArticleVariables.INFINITIVE,
                     "vam ", ArticleVariables.INFINITIVE,
                     "vau ", ArticleVariables.INFINITIVE,
                     "van ", ArticleVariables.INFINITIVE),
    new ArticlePart (R.id.anterior_past_indicative_content,
                     "haguí ", ArticleVariables.M_PARTICIPLE,
                     "hagueres ", ArticleVariables.M_PARTICIPLE,
                     "hagué ", ArticleVariables.M_PARTICIPLE,
                     "haguérem ", ArticleVariables.M_PARTICIPLE,
                     "haguéreu ", ArticleVariables.M_PARTICIPLE,
                     "hagueren ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.periphrastic_anterior_past_indicative_content,
                     "vaig haver ", ArticleVariables.INFINITIVE,
                     "vas haver ", ArticleVariables.INFINITIVE,
                     "va haver ", ArticleVariables.INFINITIVE,
                     "vam haver ", ArticleVariables.INFINITIVE,
                     "vau haver ", ArticleVariables.INFINITIVE,
                     "van haver ", ArticleVariables.INFINITIVE),
    new ArticlePart (R.id.future_perfect_indicative_content,
                     "hauré ", ArticleVariables.M_PARTICIPLE,
                     "hauràs ", ArticleVariables.M_PARTICIPLE,
                     "haurà ", ArticleVariables.M_PARTICIPLE,
                     "haurem ", ArticleVariables.M_PARTICIPLE,
                     "haureu ", ArticleVariables.M_PARTICIPLE,
                     "hauran ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.conditional_perfect_indicative_content,
                     "hauria ", ArticleVariables.M_PARTICIPLE,
                     "hauries ", ArticleVariables.M_PARTICIPLE,
                     "hauria ", ArticleVariables.M_PARTICIPLE,
                     "hauríem ", ArticleVariables.M_PARTICIPLE,
                     "hauríeu ", ArticleVariables.M_PARTICIPLE,
                     "haurien ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.perfect_subjunctive_content,
                     "hagi ", ArticleVariables.M_PARTICIPLE,
                     "hagis ", ArticleVariables.M_PARTICIPLE,
                     "hagi ", ArticleVariables.M_PARTICIPLE,
                     "hàgim ", ArticleVariables.M_PARTICIPLE,
                     "hàgiu ", ArticleVariables.M_PARTICIPLE,
                     "hagin ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.pluperfect_subjunctive_content,
                     "hagués ", ArticleVariables.M_PARTICIPLE,
                     "haguessis ", ArticleVariables.M_PARTICIPLE,
                     "hagués ", ArticleVariables.M_PARTICIPLE,
                     "haguéssim ", ArticleVariables.M_PARTICIPLE,
                     "haguéssiu ", ArticleVariables.M_PARTICIPLE,
                     "haguessin ", ArticleVariables.M_PARTICIPLE),
    new ArticlePart (R.id.periphrastic_past_subjunctive_content,
                     "vagi ", ArticleVariables.INFINITIVE,
                     "vagis ", ArticleVariables.INFINITIVE,
                     "vagi ", ArticleVariables.INFINITIVE,
                     "vàgim ", ArticleVariables.INFINITIVE,
                     "vàgiu ", ArticleVariables.INFINITIVE,
                     "vagin ", ArticleVariables.INFINITIVE),
    new ArticlePart (R.id.periphrastic_anterior_past_subjunctive_content,
                     "vagi haver ", ArticleVariables.INFINITIVE,
                     "vagis haver ", ArticleVariables.INFINITIVE,
                     "vagi haver ", ArticleVariables.INFINITIVE,
                     "vàgim haver ", ArticleVariables.INFINITIVE,
                     "vàgiu haver ", ArticleVariables.INFINITIVE,
                     "vagin haver ", ArticleVariables.INFINITIVE)
  };

  private static final ArticleTab articleTabs[] =
  {
    new ArticleTab ("article_page1",
                    R.string.article_page1,
                    R.layout.article_page1),
    new ArticleTab ("article_page2",
                    R.string.article_page2,
                    R.layout.article_page2),
    new ArticleTab ("article_page3",
                    R.string.article_page3,
                    R.layout.article_page3)
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

        for (Object item : part.items)
          {
            if (item instanceof Integer)
              {
                article.getValue (((Integer) item).intValue (), buffer);
                buffer.append ('\n');
              }
            else
              {
                buffer.append (item.toString ());
              }
          }

        for (View view : pageViews)
          {
            TextView tv = (TextView) view.findViewById (part.resourceId);
            if (tv != null)
              tv.setText (buffer);
          }
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

    pageViews = new View[articleTabs.length];

    LayoutInflater layoutInflater = getLayoutInflater ();

    for (int i = 0; i < pageViews.length; i++)
      pageViews[i] = layoutInflater.inflate (articleTabs[i].resourceId, null);

    TabHost tabHost = (TabHost) findViewById (android.R.id.tabhost);
    Resources res = getResources ();

    for (ArticleTab articleTab : articleTabs)
      {
         TabHost.TabSpec tab = tabHost.newTabSpec (articleTab.tag);
         tab.setIndicator (res.getText (articleTab.name));
         tab.setContent (this);
         tabHost.addTab (tab);
      }

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

  @Override
  public View createTabContent(String tag)
  {
    for (int i = 0; i < pageViews.length; i++)
      if (articleTabs[i].tag.equals (tag))
        return pageViews[i];

    /* This shouldn't be reached */
    return null;
  }
}
