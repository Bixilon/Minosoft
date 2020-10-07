# Modding

## mod.json
In your jar file (the mod) must be a file called `mod.json`. 
### Example
```json
{
  "uuid": "1f37a8e0-9ec7-45db-ad2f-40afd2eb5a07",
  "versionId": 123,
  "versionName": "1.0",
  "authors": [
    "Example dev"
  ],
  "name": "Example Mod",
  "identifier": "example",
  "mainClass": "de.example.mod.Main",
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
- `versionId` like in android there is a numeric version id. It is used to compare between versions (and as identifier). **Required**
- `versionName`, `authors`, `name` is the classic implementation of metadata. Can be anything, will be displayed in the mod list. **Required**
- `identifier` is the prefix of items (for Minecraft it is `minecraft`). Aka the thing before the `:`.  **Required**
- `mainClass` the Main class of your mod (self explaining). The main class needs to extent the abstract class `MinosoftMod`. **Required**
- `loading` Loading attributes. **Optional**
  - `priority` should the mod be loaded at the beginning or at the end. Possible values are `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST` **Optional**
- `dependencies` Used if you need another mod to work **Optional**
  - `hard` These mods are **needed** to work. If the loading fails, your mod is not getting loaded, and a warning message is being displayed. **Optional**
  - `soft` These mods are **optional** to work. Both use the following format: **Optional**
    - `uuid` the uuid of the mod to load. **Required**
    - `version` Specifies the version you need to load. **Optional**
      - `minimum`: Minimum versionId required. **Maximum, minimum or both**
      - `maximum`: Maximum versionId required. **Maximum, minimum or both**

## Mod loading (aka Main class)
Your main class must extend the following class: `de.bixilon.minosoft.MinosoftMod`.

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
<dependency>
    <groupId>de.bixilon.gitlab.bixilon</groupId>
    <artifactId>minosoft</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>

```
Create a Main class, here is an example
```java
package de.bixilon.example.mod.main;

import de.bixilon.minosoft.modding.MinosoftMod;
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
  "versionId": 1,
  "versionName": "1.0",
  "authors": ["Bixilon"],
  "name": "Example Mod",
  "identifier": "example",
  "mainClass": "de.bixilon.example.mod.main.TestMod"
}
```

## Events
There are global events (which works on all connections) and connections events (server specific).

To register a global event you need to use (in the `INITIALIZING` phase) `getEventManager().registerGlobalListener(new XYEventListener());`.
If you want to register an event depending on an IP (like server specific support, you can use the following):
`getEventManager().registerConnectionListener(new XYEventListener(), new ServerAddress("127.0.0.1", 25565));`

Your XYListener class needs to implement `de.bixilon.minosoft.modding.event.EventListener`;
```java
import de.bixilon.minosoft.modding.event.EventListener;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendingEvent;

public class ChatEvent extends EventListener {
    @Override
    public void onChatMessageReceiving(ChatMessageReceivingEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getMessage().getMessage().contains("Bixilon")) {
            MinosoftExampleMod.getInstance().getLogger().game("Bixilon is awful, suppressing this potential bad chat message!");
            event.setCancelled(true);
        }
    }

    @Override
    public void onChatMessageSending(ChatMessageSendingEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getMessage().contains("jeb_ is stupid")) {
            event.setCancelled(true);
            event.getConnection().getSender().sendChatMessage("jeb_ is awesome!");
        }
    }
}
```
The following code would suppress messages containing the word "Bixilon" and if you write "jeb_" into the chat, the message's text will be "jeb_ is awesome".
To see a list of all events look into `de.bixilon.minosoft.modding.event.events`. There is also a javadoc.