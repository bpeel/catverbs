import xml.etree.ElementTree as ET
import sys
import re

e = ET.parse(sys.stdin)

verb_re = re.compile(r'\{\{-verb-\|català\}\}')
group_re = re.compile(r'{{ca\.v\.conj\.(.*)}}')

for text in e.findall(".//{http://www.mediawiki.org/xml/export-0.8/}text"):
    content = ''.join(text.itertext())

    if verb_re.search(content):
        m = group_re.search(content)
        if m:
            print(m.group(1))
