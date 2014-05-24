##ubimon##

This is a very simple Pokémon clone that exercises ubiquitous computing principles, also called pervasive computing [1], to enhance game experience. The game focuses on capturing and collecting monsters around the world (yeah, the actual world) using your current geographical location as an indicator of what type of monster you are able to capture. Go near a lake or river, and capture water types, go to high places and capture flying types, you get the idea... Find fellow trainer in your journey and you'll have the chance to check what they've already captured and, maybe you can catch something different when you do that. Now run and go catch'em all! Check the [Wiki](https://github.com/lhsantos/ubimon/wiki) for tutorials.

This is the formal definition of the game, following Schell's elemental tetrad of game design[2]:

####Aesthetics####

This is a 2D game, with top view camera, pixelated graphics in the UI and HUD, and a real time map view for the main screen. The game will be played in personal computing devices, such as computer or handheld devices. While in the world map, the player will see the world as on Google Maps, but the HUD will provide aditional information to help the player make decisions about his movements. The map always shows nearby players, known places and the probabitlity of finding ubimon of different types, as a color grading. When the player is near an ubimon, he must use the camera to look around and find it. The physical size of the mobile device the player is using as well as the size of each ubimon determines how many monsters may be carried in each device. Also, the player may interact with other private or public devices, to exchange ubimon between them.

In the battling mode, the player will see in the larger upper part of the screen the two currently battling ubimon, including their name, level and health, as well as the remaining number of available ubimon the oponent (if he/she is another trainer) has. The action buttons will be located in the lower part of the screen. The player interacts with them by touching the screen or clicking with the mouse. The buttons are "Attack", "Throw AuraBall", "Switch Ubimon" and "Run". The "Throw AuraBall" button takes the player to an animation while the game decides if the capture was successful. The "Switch Ubimon" button will show a list of the available ubimon for the battle, along with their level, health and status. The "Attack" button will show the available attacks for the current battling ubimon, as a list, sorted by name.

In the lab mode, there is a menu which will allow the player to list and view ubimon and choose to battle them; to view the lab's owner profile; and to exit to the main screen (map).

####Mechanics####

The game stimulates the player to explore different places around the world in search of ubimon - the ubiquitous monsters. The player uses its mobile device to enter the world. The main screen is a world map, with points of  interest, other players and some statistical data available. As the player moves around, there's a chance he will find a ubimon in his path, the player has a radar that gives feedback in the map when the chances of an encounter increases. When there's certainly a ubimon around, the player must use the camera to search for it in the surroundings. When the ubimon is visible in the screen, the player enters the battle mode, and must choose a ubimon to start battle, if there's no default ubimon set.

The battle is turn based. On each turn, the player may choose only one of the following actions: run, switch the current ubimon, attack or throw an AuraBall (if he/she is facing a wild ubimon, not a trained one). If the player tries to run, there's a chance he will not be successfull. If the ubimon attacks, the enemy may suffer some amount of damage, depending on each of the battling ubimon types and levels. If an ubimon health reaches 0, he will faint and may not battle anymore. A player loses the battle when all of his ubimon are fainted. To heal his ubimon, the player must walk around, after some minimum distance is covered, all the ubimon in a device will receive some heal points, proportional to their level, and their health will increase until it reaches a maximum, also based on the level. When an ubimon is stored in another device, when the player enters a trainer's lab or a gathering point (described in the next paragraphs), all the ubimon are instantly healed.

If the player successfully captures a ubimon, he will be available for future battles, however, there's a limit to how many ubimon each device may carry. Each ubimon requires a minimum physical size of screen area to be held in a device. If a device has no space to hold a ubimon, it will not be taken and will be released. There are known store stations in the world where the player has unlimited space to hold ubimon. The player must move close to one of these stations to store his ubimon and empty his device. The player may also use several devices to hold ubimon, but may battle with only one at a time, and may use his home desktop to hold more ubimon than a handheld device could. In the main map, there's a manager button that gives access to the ubimon exchange center. There, there's a list of nearby known devices, store stations and other players labs. A device is considered known if it has a copy of the game (which announces its presence to the smartspace) and that copy has a profile for the current player. Store stations are public devices and are automatically known by the game by their geographical location. Other player labs are only available when the player is near a gathering point or a place where he has found another player. When there's another trainer around, the game issues a notification and the player may open it to recognize that trainer, so the current location is stored and the player may come back near it in the future to check that trainer's lab. When 10 or more player recognize each other inside a certain region of limited size, that region becomes a gathering point and will remain that way as long as no more than a week passes without it being visited by any player. Inside a gathering point, any player may access the lab of any trainer who passed there in the last month.

Inside a trainer's lab, the player may choose to battle that trainer, but the other player must be around and accept the battle, or battle any of the ubimon currently shown at the lab (they are chosen randomly by the game from the last team that player had when he passed that location, and will have a higher level than the player's current tougher ubimon), and maybe capture them.

There are five basic types of ubimon: Fire, Water, Eletric, Flying and Grass. Each ubimon may have one main type and one secondary type, that will be achieved when the ubimon has battled enough enemies of that type. Once a ubimon has two types, he may have up to 3 evolutions, the last one being its legendary form. The evolution depends on the two main types of the ubimon and can only be achieved if the player battles and gains experience in the right geographical places to enhance its ubimon strength. The goal of the game is capturing at least one of the 10 type combinations and having one legendary ubimon of each basic type. To become a Ubimon Master, the player must defeat another player in battle, facing at lest 4 legendary ubimon.

####Story####

There's something strange going on... Weird creatures are poping out from nowhere all around the world, but it seems they have a special connection with certain elements of nature, and appear only in certain places, according to that connection. You are a master researcher of the ubimon, these strange new creatures. UbiCorp gave you a ubidex, a portable ubimon database and tracker, so you can journey around the planet searching for them. If you find fellow researches, you will be able to share your knowlege with them and explore their findings! Your final goal is study the legendary form of the ubimon,  its most powerful state discovered so far. Go! Do it for science!


####Technology####

This game uses the uOS middleware\[3\] (links bellow) to establish the basic communication infrastructure in the ubiquitous computing smart space and Unity 3D as its game engine. The game will be playable in PC (Windows, Linux and Mac) and portable devices (Android and iOS).

With regards to the ubiquitous infrastructure, the game will use the following DSOA\[4\] elements:

- The smart space for the game is potentially the whole world, but initially the tests will be done at Brasilia's central area (Plano Piloto) with a store station a LAICO lab inside UnB. The various geographic regions of the city (lake, high areas, etc..) will serve as initial ubimon finding sites.

- The devices used by the game will be:
 - personal computers;
 - tablets and cell phones;
 - one or more central servers on the web.

- The central server will provide a LocationDriver, with services to:
  - check in with the server as an entity;
  - register the current location, once checked in;
  - list nearby entities.

- The game itself will have services to:
 - check in with another player;
 - list profile and ubimon;
 - ask for a battle;
 - send battle commands;
 - checkin with another device;
 - exchange ubimon;

- All the mobile devices will have a local driver GoogleMapsDriver. The driver will have the service of rendering the world map given a coordinate.


##Links##

* [uos_core](https://github.com/UnBiquitous/uos_core)
* [uos_socket_plugin](https://github.com/UnBiquitous/uos_socket_plugin)

##References##
====
1. WEISER, Mark. 1991. **The Computer for the 21st Century**. http://dl.acm.org/citation.cfm?id=329126.
2. SCHELL, Jesse. 2008. **The Art of Game Design: A Book of Lenses**. Morgan Kaufmann Publishers Inc. San Francisco, CA, USA.
3. BUZETO, Fabricio N. 2013. **uOS: A resource rerouting middleware for ubiquitous games**. Ubiquitous Intelligence and Computing, 2013 IEEE 10th International Conference on and 10th International Conference on Autonomic and Trusted Computing (UIC/ATC).
4. BUZETO, Fabricio N. 2010. **DSOA: A service oriented architecture for ubiquitous applications**. 5th International Conference, GPC 2010, Hualien, Taiwan, May 10-13, 2010.
