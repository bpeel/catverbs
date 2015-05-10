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
import sys

DATA_DIR = "scraped-data"
OUT_DIR = "vic-verbs"
CONJS = ["jo", "tu", "ell", "nosaltres", "vosaltres", "ells"]
PARTICIPLES = ["m", "f", "pm", "pf"]

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

list_re = re.compile(r'\s*,\s*')
gerund_re = re.compile(r'^gerundi$')
participles_re = re.compile(r'^participis?$')
dash_re = re.compile(r'^[-–]?$')
eix_re = re.compile(r'eix(?:o|es||en|i|is|in)$')

ALTERNATIVE_OVERRIDES = { 'is_tu' : re.compile(r'[eiï]sses$'),
                          'is_nosaltres' : re.compile(r'[éí]ssem$'),
                          'is_vosaltres' : re.compile(r'[éí]sseu$'),
                          'is_ells' : re.compile(r'[eiï]ssen$') }

class ParseError(Exception):
    pass

class NoTableError(ParseError):
    pass

def get_table(soup):
    for th in soup.find_all('th'):
        this_title = th.get_text().strip()
        if this_title == "Formes personals simples":
            return th.parent.parent

    raise NoTableError("Couldn't find table")

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

def get_special_part(table, name, reg):
    for child in table.children:
        if isinstance(child, bs4.element.Tag) and child.name == "tr":
            if child.th and reg.match(child.th.text.strip().lower()):
                return child

    raise ParseError("Couldn't find special part " + name)

def add_alternatives_from_row(values, row):
    var_num = 0
    to_add = []

    for child in row.children:
        if isinstance(child, bs4.element.Tag) and child.name == "td":
            # Only add the alternative if the row doesn't have a note
            if var_num >= len(CONJS):
                return

            to_add.append(child.text.strip())
            var_num += 1

    for i in range(0, len(to_add)):
        if to_add[i] != "":
            values[i].append(to_add[i])

def add_alternatives(values, part):
    while True:
        part = part.next_sibling
        if part == None:
            break
        if isinstance(part, bs4.element.Tag) and part.name == "tr":
            if part.th:
                break

            td = part.td
            if td and td.has_attr('colspan'):
                break

            add_alternatives_from_row(values, part)

def fixup_alternatives(variable, alts):
    # Move the alternative ending in ‘eix’ to the end
    for i in range(0, len(alts)):
        if eix_re.search(alts[i]):
            value = alts[i]
            del alts[i]
            alts.append(value)
            break

    if len(alts) != 2:
        return

    if variable in ALTERNATIVE_OVERRIDES:
        if ALTERNATIVE_OVERRIDES[variable].search(alts[1]):
            del alts[1]

def dump_conjugation(out, part, prefix):
    var_num = 0
    values = []

    for child in part.children:
        if isinstance(child, bs4.element.Tag) and child.name == "td":
            value = child.text.strip()
            if dash_re.match(value):
                value = "—"
            values.append([value])
            var_num += 1
            if var_num >= len(CONJS):
                break

    if var_num < len(CONJS):
        raise ParseError("Not enough tds in row")

    add_alternatives(values, part)

    for i in range(0, len(values)):
        alts = values[i]
        variable = prefix + "_" + CONJS[i]
        fixup_alternatives(variable, alts)
        out.write(variable + "=" + alts[0])
        if len(alts) > 1:
            out.write(" (o " + " o ".join(alts[1:]) + ")")
        out.write("\n")

def process_verb(out, soup):
    table = get_table(soup)

    for main_part in SECTIONS:
        for sub_part in main_part[1:]:
            part = get_part(table, main_part[0], sub_part[0])
            dump_conjugation(out, part, sub_part[1])
            out.write("\n")

    gerund = get_special_part(table, "gerund", gerund_re)
    out.write("gerund=" + gerund.td.text.strip() + "\n")

    participles_row = get_special_part(table, "participles", participles_re)
    participles = list_re.split(participles_row.td.text.strip())

    if len(participles) != len(PARTICIPLES):
        raise ParseError("Invalid participles")

    for i in range(0, len(participles)):
        out.write(PARTICIPLES[i] + "_participle=" + participles[i] + "\n")

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

        out_filename = os.path.join(OUT_DIR, infinitive + ".txt")
        if os.path.isfile(out_filename):
            print("Skipping " + infinitive + ", already exported")
            continue

        f = open(os.path.join(DATA_DIR, filename), 'r', encoding='UTF-8')
        soup = bs4.BeautifulSoup(f)
        f.close()

        f = open(out_filename, 'w', encoding='UTF-8')

        try:
            process_verb(f, soup)
        except NoTableError as e:
            os.remove(out_filename)
            sys.stderr.write(out_filename + ": " + str(e) + " (continuing)\n")
        except ParseError as e:
            os.remove(out_filename)
            sys.stderr.write(out_filename + ": " + str(e) + "\n")
            break

        f.close()
