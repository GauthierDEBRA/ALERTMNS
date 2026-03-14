#!/usr/bin/env bash
set -euo pipefail

if JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null); then
  export JAVA_HOME="$JAVA_21_HOME"
elif JAVA_17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null); then
  export JAVA_HOME="$JAVA_17_HOME"
else
  echo "Aucun JDK 17 ou 21 n'a ete trouve. Installe Java 17 ou 21 pour executer Maven localement." >&2
  exit 1
fi

exec mvn "$@"
