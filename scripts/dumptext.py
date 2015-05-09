# Catverbs - A portable Catalan conjugation reference for Android
# Copyright (C) 2014, 2015  Neil Roberts
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
import cgi
import os
import errno

def dump_conjugations(f, verb, name_prefix):
    for name in ["jo", "tu", "ell", "nosaltres", "vosaltres", "ells"]:
        variable = name_prefix + "_" + name
        f.write(variable + "=" + cgi.escape(verb.get_value(variable)) + "\n")

    f.write("\n")

dictionary = verbdata.Dictionary()

try:
    os.mkdir("txt")
except OSError as e:
    if e.errno != errno.EEXIST:
        raise

infinitives = []

for verb in dictionary:
    infinitive = verb.get_value("infinitive")
    infinitives.append(infinitive)

    f = open(os.path.join("txt", infinitive + ".txt"), "w", encoding="UTF-8")

    dump_conjugations(f, verb, "pi")
    dump_conjugations(f, verb, "ii")
    dump_conjugations(f, verb, "spi")
    dump_conjugations(f, verb, "future")
    dump_conjugations(f, verb, "cond")

    dump_conjugations(f, verb, "ps")
    dump_conjugations(f, verb, "is")

    dump_conjugations(f, verb, "imp")

    f.close()
