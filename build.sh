#! /bin/bash

[[ "$1" == "test" ]] && javac -d . tests/Test.java
javac -d . -sourcepath . Main.java
mkdir -p target
mv Main.class target 2> /dev/null
for folder in $(ls -d */); do
    grep ".git" <<< $folder > /dev/null && continue
    grep "target" <<< $folder > /dev/null && continue
    mkdir -p target/$folder
    mv $folder/*.class target/$folder 2> /dev/null
done

. ./deps.sh
