# Modding

## Disclaimer: This is completely outdated and will be rewritten soon. Just wait with mod writing! Sorry

## mod.json

In the root folder of your jar file (the mod) must be a file called `mod.json`. It contains metadata for the mod loader.

### Example

```json
{
  "uuid": "1f37a8e0-9ec7-45db-ad2f-40afd2eb5a07",
  "version_id": 123,
  "version_name": "1.0",
  "authors": [
    "Example dev"
  ],
  "name": "Example Mod",
  "modding_api_version": 1,
  "resource_namespace": "example",
  "main_class": "de.example.mod.Main",
  "loading": {
    "priority": "NORMAL"
  },
  "dependencies": {
    "hard": [
      {
        "uuid": "7fd5d30c-246e-42f6-9dfe-82f2383b4b68",
        "version": {
          "minimum": 123,
          "maximum": 123
        }
      }
    ],
    "soft": [
      {
        "uuid": "af8c9c7e-c3c7-41e0-bcc6-3882202d0c18",
        "version": {
          "minimum": 123,
          "maximum": 123
        }
      }
    ]
  }
}
```
### Explanation

- `uuid` is a unique id for the mod. Generate 1 and keep it in all versions (used for dependencies, etc). **Required**
- `version_id` like in android there is a numeric version id. It is used to compare between versions (and as identifier). **Required**
- `version_name`, `authors` and `name` is the classic implementation of metadata. Can be anything, will be displayed in the mod list. **Required**
- `modding_api_version` Modding API version of minosoft. Currently `1` **Required**
- `resource_namespace` is the prefix of items (for Minecraft it is `minecraft`). Aka the thing before the `:`.  **Required**
- `main_class` the Main class of your mod (self explaining). The main class needs to extend the abstract class `MinosoftMod`. **Required**
- `loading` Loading attributes. **Optional**
  - `priority` should the mod be loaded at the beginning or at the end. Possible values are `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST` **Optional**
- `dependencies` Used if you need another mod to work **Optional**
  - `hard` These mods are **needed** to work. If the loading fails, your mod is not getting loaded, and a warning message is being displayed. **Optional**
  - `soft` These mods are **optional** to work. Both use the following format: **Optional**
    - `uuid` the uuid of the mod to load. **Required**
    - `version` Specifies the version you need to load. **Optional**
      - `minimum` Minimum versionId required. **Maximum, minimum, both or none**
      - `maximum` Maximum versionId required. **Maximum, minimum, both or none**

## Getting started
Add Minosoft to your maven dependencies with  
Repository:
```xml
<repositories>
	<repository>
    	<id>jitpack.io</id>
    	<url>https://jitpack.io</url>
	</repository>
</repositories>
```  
Dependency:  
```xml
<dependencies>
    <dependency>
        <groupId>de.bixilon.gitlab.bixilon</groupId>
        <artifactId>minosoft</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Instead of `master-SNAPSHOT` you should write the latest commit, if there is no release tagged yet.

## Mod loading (aka Main class)
Your main class must extend the following class: `de.bixilon.minosoft.modding.MinosoftMod`.

### Phases
There are different phases (states) for the loading. There are the following phases:
 1. `BOOTING` Happens after loading all configuration files and while displaying the server list.
 2. `INITIALIZING` All mods are loaded into the ram and everything before registering anything happens here (like OpenGL stuff, etc). You should also register Events here.
 3. `LOADING` You have custom items, entities, blocks, etc? Load it here.
 4. `STARTING` All items, etc are loaded. If you want to do anything else, do it here.
 5. `STARTED` The loading is complete

The most important thing is performance. To archive fast loading times, etc, all mods (if they don't on each other) are getting loaded async.
One phase is completed (= Next phase starts), once all mods have completed the previous phase. Everything is async.
If your start function needs much time, you can set the loading priority in the `mod.json` to start at the beginning. The `start` method returns a success boolean.


Create a Main class, here is an example

```java
package de.bixilon.example.mod.main;

import de.bixilon.minosoft.modding.loading.ModPhases;

public class TestMod extends MinosoftMod {

    public boolean start(ModPhases phase) {
        if (phase == ModPhases.BOOTING) {
            getLogger().info("Hello world!");
        }
        return true;
    }
}
```
Your `mod.json` can look like this

```json
{
  "uuid": "1f37a8e0-9ec7-45db-ad2f-40afd2eb5a07",
  "version_id": 1,
  "version_name": "1.0",
  "authors": [
    "Bixilon"
  ],
  "name": "Example Mod",
  "resource_namespace": "example",
  "main_class": "de.bixilon.example.mod.main.TestMod"
}
```

## Events
There are global events (which works on all connections) and connections events (server specific).

To register a global event you need to use (in the `INITIALIZING` phase) `getEventManager().registerGlobalListener(new XYEventListener());`.
If you want to register an event depending on a server address (like server specific support, you can use the following), you can use `EventManager::registerConnectionListener` method.
It takes 2 arguments: The first one is your listener, the second one is a `ServerAddressValidator`.
There are several validators, choose one or write your own:
 - `HostnameValidator` Simply check the hostname. For example: `bixilon.de`.
 - `PortValidator` Get all connections with a specific port. For example: `25565`
 - `SimpleAddressValidator` Check for server address and port. e.g.: `bixilon.de:25565`
 - `RegexValidator` Check for a regular expression like: `*.de`
 
 Use the following: `getEventManager().registerConnectionListener(new XYEventListener(), new HostnameValidator("127.0.0.1"));`

Your event methods need to be annotated by `EventHandler`. `EventHandler` **can** take these arguments:
 - `priority` Pretty much self explaining. `HIGH` means, that it gets executed at "the beginning", `LOW` means the opposite. Defaults to `NORMAL`.
 - `ignoreCancelled` If it is a cancellable event, your method only gets executed, when all prior listeners (potentially with a higher priority) did not cancel the connectionEvent. Defaults to `false`.

Your XYEventListener class needs to extend `de.bixilon.minosoft.modding.event.EventListener`;

```java
import de.bixilon.minosoft.modding.EventPriorities;
import de.bixilon.minosoft.modding.event.EventListener;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendEvent;
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler;

public class ChatEvent extends EventListener {

  @EventHandler(priority = EventPriorities.HIGHEST)
  public void onChatMessageReceiving(ChatMessageReceiveEvent event) {
    if (event.getMessage().getMessage().contains("Bixilon")) {
      MinosoftExampleMod.getInstance().getLogger().info("Bixilon wrote a potential bad chat message. Suppressing it!");
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onChatMessageSending(ChatMessageSendEvent event) {
    if (event.getMessage().contains("jeb_ is stupid")) {
      event.setCancelled(true);
      event.getConnection().getSender().sendChatMessage("jeb_ is awesome!");
    }
  }
}
```

The following code would suppress messages containing the word "Bixilon" and if you write "jeb_ is stupid" into the chat, the message's text will be "jeb_ is awesome". To see a list of all events look into `de.bixilon.minosoft.modding.connectionEvent.events`. There is also sometimes a javadoc, but generally just search for a file or open an issue.

## Debugging and running

Use a classic zip script that replaces the .jar in your mods folder:)

Just kidding, no clue yet. We need a build system, #12
