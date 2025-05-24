# Getting Started

So, you want to create your own space? This guide will help you understand the first steps to take.

## Creating a Space

To begin, go to the `/spawn` if you aren't there already.
Once there, open the `My Spaces` menu, using this item in your inventory:

![My Spaces Item](https://i.imgur.com/ZQVPCd5.png)

And then click on the `Create Space` item:

![Create Space Item](https://i.imgur.com/6bwxKYU.png)

Once you created it, click on the newly created item in the menu to join your space.
If it's not available you likely reached the limit of spaces you can create, in this case you will have to use an existing space to follow along.

## The Modes

There are 3 modes for each space:

* `Play` - This one is available to everyone, and is where the fun happens.
* `Build` - This is the same world as `Play` but you are in creative mode and unaffected by space code.
  Only you (the owner) and people you add to `/space builders` can use this mode.
* `Code` - This is a separate world where you can create and edit custom code for your space.
  Only you (the owner) and people you add to `/space developers` can enter and view this area.

To switch modes, simply run `/play`, `/build` or `/code`.

## Basic Coding Controls

Generally, when in the coding area you can use left click to destroy nodes, wires and similar.
Right click is to interact, like moving nodes and creating wires, and Swap-Hands (usually F) is for creating new elements.

## Creating Nodes

To create new nodes, either use `/add <name>` if you already know what you are looking for, or `/add? <query>` to search.
For getting a menu with all nodes (recommended for new players), just press F while looking at an empty space.

![Node Menu](https://i.imgur.com/msDDpCi.png)

In this menu you can right-click categories to open them, or nodes to place them. Left-clicking this menu will remove it.

Begin by creating two nodes: `On Player Join` and `Send Message`.
You can find them in `Events` and `Player` -> `Effect` respectively.

![Both Nodes](https://i.imgur.com/5qXj1gP.png)

If you didn't create them next to another like this, you can right-click the background to start/stop dragging.

## Creating Wires

To create a wire, right-click a node output and then right-click a node input.
You can also right-click in between to set midpoints the wire will go through, or left-click to abort or remove the last midpoint.

Create 2 wires, once connecting the two signals, and one connecting the player.
The signal says that when a player joins, it will send a message. So if you connect it to something else it will do the other thing instead.
The player wire is to specify who to send the message to, in this case the player that joined.

![Connected Nodes](https://i.imgur.com/wFalF6y.png)

## Specifying Values

To specify a value, in our case the message to be sent, type in chat like normal, while looking at the corresponding input you want to change.
I will go with `<gold>Hello World!` for the message, but you can be creative. For formatting your text, you can have a look at the [MiniMessage Documentation](https://docs.advntr.dev/minimessage/format.html).

If you did everything like me, it should look like this:

![Updated Nodes](https://i.imgur.com/6wSkODm.png)

## Testing your code

Now to see your code in action, type `/reload` and then go into play mode (`/play`).

This is how it looks like for me:

![Welcome Message](https://i.imgur.com/hNsEKPY.png)

## Web Editor
There also is a web interface for editing code, the link you need depends on which server you are playing on.
To open the web interface, you will need to open said link like `http://example.com/#123` where 123 is the space id.
After you open the link, you will need to grant access from in-game, look for a chat message that prompts you to do so.
The controls are mostly the same as in-game, with the exception that if you hold a mouse button, instead of clicking quickly. It will send a second click once you let go.
You also can not write or read chat messages, but only set values and run a few select commands using the `t` key