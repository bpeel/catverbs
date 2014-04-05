/*
 * Catverbs - A portable Catalan conjugation reference for Android
 * Copyright (C) 2013  Neil Roberts
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
import android.content.SharedPreferences;
import android.os.Bundle;

/* This activity is just like a landing page to select the right
 * initial activity. */

public class StartActivity extends Activity
{
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    MenuHelper.goSearch (this);

    /* Finish this activity to get it out of the call stack */
    finish ();
  }
}
