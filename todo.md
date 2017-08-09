# To Do List:
-Must Finish before 1.0.0 Release.

#1: Chat handeling:
[DONE] a) fix crashes (client) or internal server error (server) when player without Empire decides to chat.
[DONE] b) Add abbrieviations for Empire ranks: [Leader] = [L]; [Officer] = [O]; [Member] = [M]
c) Add optional herochat support.
d) Add private message support.



#2: Relationships:
a) Add new set of flags for relationships, based on the original ones: [Build, PVP, Access, Loot, etc]
b) Add "Neutral" relationship: Simple relationship in which empires can be neutral to one another.
c) Add "Peace" relationship: Simple relationship signifying that two empires are at peace.
d) Add "Enemy" relationship: simple relationship signifying that two empires are at war.
e) Add support for "Enemy" relationships being unable to execute specified commands (in config).
f) Add support for "Enemy" relationships being unable to execute essentials commands (in config?).



#3) Raiding Mechanisms:
[DONE] a) Add "stregnth" system based on faction's "power" system. (if power <= territory then empire = raidable).
[DONE] b) Add citizen power system in which citizens start with 0.00 power that updates by 0.2 every 20 minutes.
[DONE] c) Doing /empire citizen <name> should show power as a result in the header.
[DONE] d) Doing /empire info <name> should show power as a result in the header.
[DONE] e) Add configuration support for different rates of power (increase/decrease).
[DONE] f) Allow other "Empires" to over-claim or unclaim land of empires where power<=territory.


#4) Misc:
[Not Possible] a) Add new "Empire Border Block" to replace lapis lazuli border.
[DONE] b) Graphical changes to how the commands appear in game.
[DONE] c) Cleanup, organize imports.
