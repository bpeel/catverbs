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

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import android.content.Context;
import android.content.res.AssetManager;

class ArticleCacheEntry
{
  public int number;
  public Article article;
};

public class ArticleLoader
{
  private static final int ARTICLES_PER_FILE = 128;

  private static ArticleLoader singletonInstance;

  private AssetManager assetManager;

  private ArticleCacheEntry articleCache[] = new ArticleCacheEntry[8];

  public ArticleLoader (Context context)
  {
    assetManager = context.getAssets ();
  }

  private static void skipArticle (BinaryReader reader)
    throws IOException
  {
    /* Skip the parent article number */
    reader.skip (2);

    while (true)
      {
        int variableNum = reader.readByte ();

        if (variableNum == 0xff)
          break;

        int stringLength = reader.readByte ();
        reader.skip (stringLength);
      }
  }

  private static void skipArticles (BinaryReader reader, int numArticles)
    throws IOException
  {
    while (numArticles-- > 0)
      skipArticle (reader);
  }

  private Article loadUncachedArticle (int articleNum)
    throws IOException
  {
    String filename = String.format (Locale.US,
                                     "articles-%04x.dat",
                                     articleNum & ~(ARTICLES_PER_FILE - 1));
    InputStream in = assetManager.open (filename);
    BinaryReader reader = new BinaryReader (in);

    skipArticles (reader, articleNum & (ARTICLES_PER_FILE - 1));

    Article article = new Article (this, reader);

    in.close ();

    return article;
  }

  public Article loadArticle (int articleNum)
    throws IOException
  {
    int i;

    /* Check if we already have the article in the cache */

    for (i = 0; i < articleCache.length; i++)
      {
        if (articleCache[i] != null &&
            articleCache[i].number == articleNum)
          {
            ArticleCacheEntry entry = articleCache[i];

            /* Move the article to the front of the queue every time
             * it is used */
            System.arraycopy (articleCache, /* src */
                              0, /* srcPos */
                              articleCache, /* dest */
                              1, /* destPos */
                              i /* length */);
            articleCache[0] = entry;

            return entry.article;
          }
      }

    Article article = loadUncachedArticle (articleNum);
    ArticleCacheEntry entry = new ArticleCacheEntry ();
    entry.article = article;
    entry.number = articleNum;

    System.arraycopy (articleCache, /* src */
                      0, /* srcPos */
                      articleCache, /* dest */
                      1, /* destPos */
                      articleCache.length - 1 /* length */);

    articleCache[0] = entry;

    return article;
  }

  public static ArticleLoader getDefault (Context context)
  {
    if (singletonInstance == null)
      singletonInstance = new ArticleLoader (context);

    return singletonInstance;
  }
}
