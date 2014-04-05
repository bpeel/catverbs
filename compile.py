import verbdata
import struct
import os
import re
import unicodedata

def make_index_value(display_name):
    buf = bytearray()

    for ch in display_name:
        decomposition = unicodedata.decomposition(ch)
        if len(decomposition) > 0:
            ch = chr(int(decomposition.split()[0], 16))
        if ch >= 'a' and ch <= 'z':
            buf.append(ord(ch))

    return buf.decode("ASCII")

def compress_integer(buffer, value):
    while value > 0x7f:
        buffer.append((value & 0x7f) | 0x80)
        value >>= 7

    buffer.append(value)

def get_compressed_integer_length(value):
    if value <= 0:
        return 1
    return (value.bit_length() + 6) // 7

def encode_value(variables, value):
    def make_replacement(variable):
        return chr(variables.get_index(variable.group(1)) + 0xE000)
    return verbdata.BRACKET_RE.sub(make_replacement, value).encode('UTF-8')

class TrieNode:
    def __init__(self, character):
        self.character = character
        self.children = []
        self.articles = []
        self.length_cache = None

    def get_length(self):
        if self.length_cache is None:
            length = len(self.character.encode("UTF-8"))

            for article in self.articles:
                length += 2

                if article.display_name:
                    length += len(article.display_name.encode("UTF-8"))

            for child in self.children:
                child_length = child.get_length()
                length += (child_length +
                           get_compressed_integer_length(child_length << 1))

            self.length_cache = length

        return self.length_cache

    def add_article(self, article):
        self.articles.append(article)

    def get_child_or_create(self, character):
        for pos in range(0, len(self.children)):
            child = self.children[pos]

            if child.character == character:
                return child

            # The list is sorted alphabetically so if we reach a
            # character that is greater than the new one then we have
            # found an insert position
            if child.character > character:
                node = TrieNode(character)
                self.children.insert(pos, node)
                return node

        node = TrieNode(character)
        self.children.append(node)
        return node

    def _compress_article(self, buffer, article, last):
        value = article.article_num
        if not last:
            value |= 0x8000
        if article.display_name:
            value |= 0x4000
        buffer.extend(struct.pack("<H", value))
        if article.display_name:
            display_name_bytes = article.display_name.encode("UTF-8")
            buffer.append(len(display_name_bytes))
            buffer.extend(display_name_bytes)

    def compress(self, buffer):
        # Add the node length
        length = self.get_length() << 1
        if len(self.articles) > 0:
            length |= 1
        compress_integer(buffer, length)

        # Character for this node
        buffer.extend(self.character.encode('UTF-8'))

        for article_num in range(0, len(self.articles)):
            article = self.articles[article_num]
            self._compress_article(buffer,
                                   article,
                                   article_num == len(self.articles) - 1)

        for child in self.children:
            child.compress(buffer)

class Trie:
    def __init__(self):
        # The character for the root node isn't used anywhere. [ is
        # the next character after Z
        self.root = TrieNode('[')

    def add_article(self, article):
        node = self.root

        for ch in article.index_value:
            node = node.get_child_or_create(ch)

        node.add_article(article)

    def compress(self):
        buffer = bytearray()
        self.root.compress(buffer)
        return buffer

class VariableList:
    def __init__(self):
        self.variables = {}
        self.next_index = 0

    def get_index(self, variable_name):
        if variable_name not in self.variables:
            self.variables[variable_name] = self.next_index
            self.next_index += 1
        return self.variables[variable_name]

vd = verbdata.Dictionary()
trie = Trie()
variables = VariableList()

article_num = 0

for verb in vd:
    infinitive = verb.get_value('infinitive')
    verb.article_num = article_num
    verb.index_value = make_index_value(infinitive)
    if verb.index_value != infinitive:
        verb.display_name = infinitive
    else:
        verb.display_name = None

    trie.add_article(verb)

    article_num += 1

out = open("articles.dat", "wb")

# Write the number of articles
out.write(struct.pack('<H', article_num))
# Leave space for the article offsets
out.seek(article_num * 4, os.SEEK_CUR)
# Write the trie
out.write(trie.compress())

offset = 0

article_num = 0

for verb in vd:
    verb.offset = offset

    if "parent" in verb.values:
        parent = vd.get_verb(verb.values["parent"])
        out.write(struct.pack('<H', parent.article_num))
    else:
        out.write(b'\xff\xff')
    offset += 2

    for variable in verb.values:
        if variable == "parent":
            continue

        value = encode_value(variables, verb.values[variable])
        out.write(struct.pack('BB', variables.get_index(variable), len(value)))
        out.write(value)
        offset += len(value) + 2

    # Article terminator
    out.write(b'\xff')
    offset += 1

    article_num += 1

out.seek(2, os.SEEK_SET)

for verb in vd:
    out.write(struct.pack('<I', verb.offset))

out.close()
