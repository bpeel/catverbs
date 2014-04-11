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

import xml.etree.ElementTree as ET
import sys
import re
import os

NAMESPACE = "http://www.mediawiki.org/xml/export-0.8/"

def get_page_text(page):
    text_element = page.find("./{" + NAMESPACE + "}revision/"
                             "{" + NAMESPACE + "}text")
    return ''.join(text_element.itertext())

def get_verb_list_text(root):
    for page in root.findall(".//{" + NAMESPACE + "}page"):
        title_element = page.find("./{" + NAMESPACE + "}title")
        title = ''.join(title_element.itertext())
        if title == "Viccionari:Llista dels verbs catalans":
            return get_page_text(page)

def get_verb_list(root):
    text = get_verb_list_text(root)
    return re.findall(r'^\* *\[\[([^\]]+?)(?:-se(?:'+"'"+'n)?|'"'"'s)?\]\] *$',
                      text,
                      re.MULTILINE)

class Verb:
    def __init__(self, name):
        self.name = name
        self.done = False
        self.have_page = False
        self.in_list = False

e = ET.parse(sys.stdin)

verbs = {}

verb_re = re.compile(r'\{\{-verb-\|ca(?:talà)?\}\}')
group_re = re.compile(r'{{ca\.v\.conj\.(.*)}}')

data_dir = os.path.join(os.path.split(__file__)[0], '..', 'data')

for verb_name in get_verb_list(e):
    verb = Verb(verb_name)
    verbs[verb_name] = verb
    file = os.path.join(data_dir, verb_name + '.txt')
    verb.in_list = True
    verb.group = None

data_re = re.compile(r'^(.*)\.txt$')

for file_name in os.listdir(data_dir):
    m = data_re.match(file_name)
    if m is not None:
        verb_name = m.group(1)
        if verb_name not in verbs:
            verbs[verb_name] = Verb(verb_name)
        verbs[verb_name].done = True

for page in e.findall(".//{" + NAMESPACE + "}page"):
    title_element = page.find("./{" + NAMESPACE + "}title")
    title = ''.join(title_element.itertext())

    if title not in verbs:
        continue

    verb = verbs[title]
    verb.have_page = True

    text = get_page_text(page)

    if verb_re.search(text):
        m = group_re.search(text)
        if m:
            verb.group = m.group(1)

for verb_name in sorted(verbs):
    verb = verbs[verb_name]
    if verb.in_list:
        if verb.done:
            continue
        print('+ ' + verb_name, end='')
        if verb.group:
            print(' ' + verb.group, end='')
        print()
    elif verb.done:
        print('! ' + verb_name)
    else:
        print('? ' + verb_name)
