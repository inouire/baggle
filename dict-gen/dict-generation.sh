#!/bin/bash

function generateDawg() {
    type=$1
    lang=$2
    cp "plain-dict/plain_"$type"_"$lang".txt" bin/plain_dic.txt
    cd bin
    ./blitzkrieg
    cd ..
    rm bin/plain_dic.txt
    rm bin/dawg_dic.txt
    mv bin/dawg_dic.dat "../baggle-solver/src/inouire/baggle/dict/dawg_"$type"_"$lang".dat"
}

#compile generator
cd bin
gcc -o blitzkrieg blitzkrieg-trie-attack-dawg-creator.c
cd ..

#generate dict one by one
generateDawg "dict" "fr"
generateDawg "dict" "en"
generateDawg "blacklist" "fr"

