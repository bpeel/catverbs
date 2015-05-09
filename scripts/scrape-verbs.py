# Catverbs - A portable Catalan conjugation reference for Android
# Copyright (C) 2015  Neil Roberts
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; version 2 of the License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import verbdata
import os
import subprocess
import sys
import errno

OUT_DIR = "scraped-data"

try:
    os.mkdir(OUT_DIR)
except OSError as e:
    if e.errno != errno.EEXIST:
        raise

for verb in verbdata.Dictionary():
    infinitive = verb.get_value("infinitive")

    filename = os.path.join(OUT_DIR, infinitive + ".html")

    if not os.path.isfile(filename):
        retcode = subprocess.call(["wget",
                                   "-O", filename,
                                   "http://ca.wiktionary.org/wiki/" +
                                   infinitive])
        if retcode != 0:
            sys.exit(1)
