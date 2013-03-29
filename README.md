B@ggle: the fun & free multiplayer boggle game
==============================================

## What is b@ggle ?

B@ggle is a free online multiplayer boggle game. You can join an existing b@ggle room or create your own.
If you just want to play boggle, check out the official website http://baggle.org

------------------------------------------------------------------------------

B@ggle est un jeu libre et gratuit de boggle en réseau sur internet. Il est possible de rejoindre une partie existante ou bien d'héberger son propre salon de jeu.
Si vous souhaitez simplement jouer au boggle, allez faire un tour sur le site officiel http://baggle.org

## How to deploy a server

This repository is made for developement, but if you are looking for easy b@ggle server deployment, just check out http://github.com/inouire/baggle-server-deploy instead.

## How to play boggle

Boggle game is about finding words in a grid of random letters. Your opponents share the same grid, so you'll have to find more and longer words than them to win !
Each player searches for words that can be constructed from the letters of sequentially adjacent cubes, where "adjacent" cubes are those horizontally, vertically or diagonally neighboring.
Words must be at least three letters long, may include singular and plural (or other derived forms) separately, but may not use the same letter cube more than once per word.
A word earns points only if no other player has found it. By the way an alternative rule is to count all words.
The longer the word is, the more points is will give:
* 3 and 4 letters -> 1 point
* 5 letters -> 2 points
* 6 letters -> 3 points
* 7 letters -> 5 points
* 8 letters and more -> 11 points

## Architecture

B@ggle has a classical client-server architecture. Each server is a room game, to which players can connect through the desktop client.
The list of active servers is available through a master server which is hosted on baggle.org.
However it is also possible to list all servers from a local network.

B@ggle project is coded in java, but it does not need any application server.

The project is splitted into several sub-project:
* command line server
* graphical client
* solver library
* communication library
* dictionnary generation

