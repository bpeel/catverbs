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
import errno

try:
    os.mkdir("templates")
except OSError as e:
    if e.errno != errno.EEXIST:
        raise

e = ET.parse(sys.stdin)

template_re = re.compile(r'^Plantilla:(ca\.v\.conj\..*)')

for page in e.findall(".//{http://www.mediawiki.org/xml/export-0.8/}page"):
    title = page.find("./{http://www.mediawiki.org/xml/export-0.8/}title")
    if title is None:
        continue

    title = title.text

    m = template_re.match(title)
    if not m:
        continue

    name = m.group(1)

    text = page.find("./{http://www.mediawiki.org/xml/export-0.8/}revision/"
                     "{http://www.mediawiki.org/xml/export-0.8/}text")
    if text is None:
        continue

    content = ''.join(text.itertext())

    f = open(os.path.join("templates", name), "w", encoding="UTF-8")
    f.write(content)
    f.close()

