##ubimon##

This is a very simple pokemon clone that exercises ubiquitous computing, also called pervasive computing [1], principles to enhance game experience. The game focuses on capturing monsters around the world (yeah, the actual world) using your current geographical location as an indicator of what type of monster you are able to capture. Go near a lake or river, a capture water types, go to a high place and capture flying types, you get the idea... Find fellow trainer in your journey and you'll have the chance to check what they already captured and, maybe you can catch something different when you do that. Now run and go catch'em all! Check the [Wiki](https://github.com/lhsantos/ubimon/wiki) for tutorials.

This is the fornmal definition of the game, following Schell's elemental tetrad of game design[2]:

####Aesthetics####

This is a 2D game, with top view camera, pixelated graphics in the UI and HUD, and a real time map view for the main screen.

####Mechanics####

The game stimulates the player to explore different places around the world in search of ubimon - the ubiquitous monsters. The player uses its (ideally mobile) device to enter the world. The main screen is a world map, with points of  interest, other players and some statistical data available. As the player moves around, there's a chance he will find a ubimon in his path. If the player has a radar device, he will have a feedback in the map when the chances of an encounter increases.

When a ubimon is found, the player will enter the battle mode, with his default ubimon released. The game is turn based. On each turn, the player may choose only one of the following actions: run, switch the current ubimon, attack or use an item. If the player tries to run, there's a chance he will not be successfull.

The player may use an Aura Ball to try and capture a ubimon. If the player successfully captures a ubimon, he will be available for future battles.

There are five basic types of ubimon: Fire, Water, Eletric, Flying and Grass. Each ubimon may have one main type and one secondary type, that will be achieved when the ubimon is exposed to a stone. Once a ubimon has two types, he may have up to 3 evolutions, the last one being its legendary form. The evolution depends on the two main types of the pokemon and can only be achieved if the player battles and gains experience in the right geographical places to enhance its pokemon strength. The goal of the game is capturing at least one of the 10 type combinations and having one legendary pokemon of each basic type. To become a Ubimon Master, the player must defeat another player in battle, facing at lest 4 legendary pokemon.

When two players are (physically) near enough, both will have access to the other player's profile. One player may propose a battle of 1, 3, 5 or 7 rounds, each one with two ubimon battling, no repetitions allowed, or a free for all battle, when each player chooses 6 ubimon and the player is defeat when all their ubimon are fainted.

After battling with another player in a certain place, the trainer will have access to that player's zoo anytime they come back near that certain place (there may be multiple spots, but only the last 10 are stored). In the zoo, the player will be able to view some of the other player's ubimon, and may choose to battle and try to capture them, but with only one ubimon and facing a tougher version of the ubimon.

####Story####

There's something strange going on... Weird creatures are poping out from nowhere all around the world, but it seems they have a special connection with certain elements of nature, and appear only in certain places, according to that connection. You are a master researcher of the ubimon, these strange new creatures. UbiCorp gave you a ubidex, a portable ubimon database and tracker, so you can journey around the planet searching for them. If you find fellow researches, you will be able to share your knowlege with them and explore their findings! Your final goal is study the legendary form of the ubimon,  its most powerful state discovered so far. Go! Do it for science!


####Technology####

This game uses the uOS middleware (links bellow) to establish the basic communication infrastructure in the ubiquitous computing smart space and Unity 3D as its game engine. The game will be playable in PC (Windows, Linux and Mac) and portable devices (Android and iOS).

With regards to the ubiquitous infrastructure, the game will use the following DSOA elements:

- The main game server will be modeled as a device (on the web?) the provides the drivers to track players, ubimon and places. The services of each driver include listing, according to the given position, and loging in and registering the current position, in case of the player driver.

- A local google maps driver will provide an abstraction to the google maps service API. The driver will have the service of rendering the world map given a coordinate.


##Links##

* [uos_core](https://github.com/UnBiquitous/uos_core)
* [uos_socket_plugin](https://github.com/UnBiquitous/uos_socket_plugin)

##References##
====
1. WEISER, Mark. 1991. **The Computer for the 21st Century**. http://dl.acm.org/citation.cfm?id=329126.]
2. SCHELL, Jesse. 2008. **The Art of Game Design: A Book of Lenses**. Morgan Kaufmann Publishers Inc. San Francisco, CA, USA.
