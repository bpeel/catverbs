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

e = ET.parse(sys.stdin)

verb_re = re.compile(r'\{\{-verb-\|ca(?:talà)?\}\}')
group_re = re.compile(r'{{ca\.v\.conj\.(.*)}}')

for text in e.findall(".//{http://www.mediawiki.org/xml/export-0.8/}text"):
    content = ''.join(text.itertext())

    if verb_re.search(content):
        m = group_re.search(content)
        if m:
            print(m.group(1))
