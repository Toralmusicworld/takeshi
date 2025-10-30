#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass any JVM options to Gradle and stored code listening for them.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "ERROR: $*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH_SEPARATOR=:
if $cygwin || $msys; then
  CLASSPATH_SEPARATOR=";"
fi

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

# Add the gradle-wrapper.jar to the classpath
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
CLASSPATH="$GRADLE_WRAPPER_JAR"

# Set GRADLE_OPTS to the default value if not already set.
if [ -z "$GRADLE_OPTS" ]; then
    GRADLE_OPTS="-Dorg.gradle.appname=$APP_BASE_NAME"
else
    GRADLE_OPTS="-Dorg.gradle.appname=$APP_BASE_NAME $GRADLE_OPTS"
fi

# Set JAVA_OPTS to the default value if not already set.
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="$DEFAULT_JVM_OPTS"
fi

# Prepend $JAVA_OPTS to GRADLE_OPTS
GRADLE_OPTS="$JAVA_OPTS $GRADLE_OPTS"

# Split up the JVM options based on spaces.
# The purpose of the quoting is to avoid empty strings in the JAVA_OPTS_ARRAY
# as a result of multiple spaces between options.
JAVA_OPTS_ARRAY=
for OP in $JAVA_OPTS; do
    JAVA_OPTS_ARRAY="$JAVA_OPTS_ARRAY \"$OP\""
done

# Add -XX:MaxPermSize for Java 7 and older
if ! `which java` -version 2>&1 | grep 'version "1.[89]' > /dev/null; then
  if ! `echo "$JAVA_OPTS" | grep "\-XX:MaxPermSize" > /dev/null`; then
    GRADLE_OPTS="$GRADLE_OPTS -XX:MaxPermSize=256m"
  fi
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# (Re)Discover JAVA_HOME either from the environment or through reasonable means
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses jre/sh/java
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  fi
  if [ -z "$JAVACMD" ] ; then
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum number of open files
if ! $cygwin && ! $darwin && ! $nonstop; then
    if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
        # Use the maximum available in the system
        MAX_FD_LIMIT=`ulimit -H -n`
        if [ $? -eq 0 ]; then
            if [ "$MAX_FD_LIMIT" != "unlimited" ]; then
                ulimit -n $MAX_FD_LIMIT
            fi
        else
            warn "Could not query maximum file descriptor limit"
        fi
    else
        ulimit -n $MAX_FD
        if [ $? -ne 0 ]; then
            warn "Could not set maximum file descriptor limit to $MAX_FD"
        fi
    fi
fi

# Collect all arguments for the java command, following the shell quoting rules
CMD_LINE_ARGS=
for ((i=1; i <= $#; i++)); do
    eval A=\$$i
    CMD_LINE_ARGS="$CMD_LINE_ARGS \"$A\""
done

# The EVAL_CMD is needed to properly handle the quoting of the arguments
EVAL_CMD="\"$JAVACMD\" -classpath \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain $CMD_LINE_ARGS"
eval $EVAL_CMD
