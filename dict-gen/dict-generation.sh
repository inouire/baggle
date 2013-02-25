#!/bin/bash

function generateDawg() {
    type=$1
    lang=$2
    echo "Generate $1 for lang $2"
    cp "plain-dict/plain_"$type"_"$lang".txt" bin/plain_dic.txt
    cd bin
    ./blitzkrieg &> /dev/null
    cd ..
    rm bin/plain_dic.txt
    rm bin/dawg_dic.txt
    mv bin/dawg_dic.dat "../baggle-solver/src/inouire/baggle/dict/dawg_"$type"_"$lang".dat"
}

function generateDawgGrid5() {
    type=$1
    lang=$2
    echo "Generate $1 5x5 for lang $2"
    cp "plain-dict/plain_"$type"_"$lang"_5x5.txt" bin/plain_dic.txt
    cd bin
    ./blitzkrieg-5x5 &> /dev/null
    cd ..
    rm bin/plain_dic.txt
    rm bin/dawg_dic.txt
    mv bin/dawg_dic.dat "../baggle-solver/src/inouire/baggle/dict/dawg_"$type"_"$lang"_5x5.dat"
}

#compile generator
cd bin
gcc -o blitzkrieg blitzkrieg-trie-attack-dawg-creator.c
gcc -o blitzkrieg-5x5 blitzkrieg-trie-attack-dawg-creator-5x5.c
cd ..

#generate dict one by one

generateDawg "dict" "fr"
generateDawg "dict" "en"
generateDawg "blacklist" "fr"
generateDawgGrid5 "dict" "fr"
