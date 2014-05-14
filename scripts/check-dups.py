# Catverbs - A portable Catalan conjugation reference for Android
# Copyright (C) 2014  Neil Roberts
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

import re
import os
import sys
import verbdata

dictionary = verbdata.Dictionary()

def get_value(verb, variable):
    while True:
        if variable in verb.values:
            return verb.values[variable]
        if "parent" not in verb.values:
            return None
        verb = dictionary.get_verb(verb.values["parent"])

def process_file(verbname, filename):
    verb = dictionary.get_verb(verbname)

    if "parent" not in verb.values:
        return

    parent = dictionary.get_verb(verb.values["parent"])

    fin = open(filename, 'r', encoding='UTF-8')
    fout = open(filename + '.tmp', 'w', encoding='UTF-8')

    for line in fin:
            m = verbdata.VARIABLE_RE.match(line)
            if (m == None or
                get_value(parent, m.group(1)) != verb.values[m.group(1)]):
                fout.write(line)
    os.rename(filename + '.tmp', filename)

data_dir = os.path.join(os.path.split(__file__)[0], '..', 'data')

for short_filename in os.listdir(data_dir):
    if not short_filename.endswith('.txt'):
        continue
    name = os.path.splitext(short_filename)[0]
    process_file(name, os.path.join(data_dir, short_filename))
