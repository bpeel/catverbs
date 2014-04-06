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

public class BinaryUtils
{
  public static final int extractInt (byte[] data,
                                      int offset)
  {
    return (((data[offset + 0] & 0xff) << 0) |
            ((data[offset + 1] & 0xff) << 8) |
            ((data[offset + 2] & 0xff) << 16) |
            ((data[offset + 3] & 0xff) << 24));
  }

  public static final int extractShort (byte[] data,
                                        int offset)
  {
    return (((data[offset + 0] & 0xff) << 0) |
            ((data[offset + 1] & 0xff) << 8));
  }

  public static final int extractVarInt (byte[] data,
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

  public static final int getVarIntLength (byte[] data,
                                           int offset)
  {
    int length = 1;

    while ((data[offset++] & 0x80) != 0)
      length++;

    return length;
  }
}
