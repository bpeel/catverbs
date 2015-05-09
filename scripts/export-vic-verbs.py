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

import bs4
import os
import errno
import re

DATA_DIR = "scraped-data"
OUT_DIR = "vic-verbs"
CONJS = ["jo", "tu", "ell", "nosaltres", "vosaltres", "ells"]

SECTIONS = [["indicatiu",
             ("present", "pi"),
             ("imperfet", "ii"),
             ("passat simple", "spi"),
             ("futur", "future"),
             ("condicional", "cond")],
            ["subjuntiu",
             ("present", "ps"),
             ("imperfet", "is")],
            ["imperatiu",
             ("present", "imp")]]

class ParseError(Exception):
    pass

def get_table(soup):
    for th in soup.find_all('th'):
        this_title = th.get_text().strip()
        if this_title == "Formes personals simples":
            return th.parent.parent

    raise ParseError("Couldn't find table")

def count_headers(row):
    header_count = 0
    for child in row.children:
        if isinstance(child, bs4.element.Tag) and child.name == "th":
            header_count += 1

    return header_count

def get_part(table, main_part, sub_part):
    in_main_part = False

    for child in table.children:
        if isinstance(child, bs4.element.Tag) and child.name == "tr":
            header_count = count_headers(child)
            if header_count >= 2:
                in_main_part = child.th.text.strip().lower() == main_part
            elif in_main_part and header_count == 1:
                if child.th.text.strip().lower() == sub_part:
                    return child
    raise ParseError("Couldn't find part " + main_part + ", " + sub_part)

def dump_conjugation(out, part, prefix):
    var_num = 0
    for child in part.children:
        if isinstance(child, bs4.element.Tag) and child.name == "td":
            value = child.text.strip()
            if value == "":
                value = "—"
            out.write(prefix + "_" + CONJS[var_num] + "=" + value + "\n")
            var_num += 1
            if var_num >= len(CONJS):
                return
    raise ParseError("Not enough tds in row")

def process_verb(out, soup):
    table = get_table(soup)

    for main_part in SECTIONS:
        for sub_part in main_part[1:]:
            part = get_part(table, main_part[0], sub_part[0])
            dump_conjugation(out, part, sub_part[1])
            out.write("\n")

try:
    os.mkdir(OUT_DIR)
except OSError as e:
    if e.errno != errno.EEXIST:
        raise

reg = re.compile(r'^(.*)\.html$')

for filename in os.listdir(DATA_DIR):
    mo = reg.search(filename)
    if mo != None:
        infinitive = os.path.basename(mo.group(1))

        f = open(os.path.join(DATA_DIR, filename), 'r', encoding='UTF-8')
        soup = bs4.BeautifulSoup(f)
        f.close()

        out_filename = os.path.join(OUT_DIR, infinitive + ".txt")
        f = open(out_filename, 'w', encoding='UTF-8')

        process_verb(f, soup)

        f.close()
