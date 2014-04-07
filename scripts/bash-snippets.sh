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

function groups() {
  # Sort the groups from the todo list in reverse order of frequency
  sed -r s/'^([^|]*)\|.*/\1/' docs/verbs-todo.txt | \
      sort | \
      uniq -c | \
      sort -k 1,1n
}

function remove_group {
  # Remove a group from the todo list
  sed -ni '/^'"$1"'|\([^|]\+\)$/! p' docs/verbs-todo.txt
  git add docs/verbs-todo.txt
}

function generate_group {
  if test "$#" -lt 3; then
    echo "usage: generate_group <group_name> <parent>" \
        "<infinitive_suffix> [variable]";
    return 1
  fi
  var="$4"
  if test -z "$var"; then
      var=stem;
  fi
  # Generate files for a group
  for x in `sed -n 's/^'"$1"'|\([^|]\+\)$/\1/p' < docs/verbs-todo.txt `; do
      echo -e "$var=$x\nparent=$2" > data/"$x$3".txt;
      git add data/"$x$3".txt;
  done
}
