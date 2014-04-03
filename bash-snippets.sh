function groups() {
  # Sort the groups from the todo list in reverse order of frequency
  sed -r s/'^([^|]*)\|.*/\1/' verbs-todo.txt | sort | uniq -c | sort -k 1,1n
}

function remove_group {
  # Remove a group from the todo list
  sed -ni '/^'"$1"'|\([^|]\+\)$/! p' verbs-todo.txt; git add verbs-todo.txt
}

function generate_group {
  if test "$#" -lt 3; then
    echo "usage: generate_group <group_name> <parent> <infinitive_suffix>";
    return 1
  fi
  # Generate files for a group
  for x in `sed -n 's/^'"$1"'|\([^|]\+\)$/\1/p' < verbs-todo.txt `; do
      echo -e "stem=$x\nparent=$2" > data/"$x$3".txt;
      git add data/"$x$3".txt;
  done
}
