# Container

## Wording

- container: something that holds item stacks in it
- item stack: an item with properties (e.g. count, enchantments, ...)
- item: A type of thing (can have properties such as max stack size or special behavior)

## Edit

### Bulk edits

If you want to bulk edit a container, you need to do the following:

```kotlin
val container: Container = …

container.lock() // first lock the container to prevent data inconsistency

container.clear() // remove all previous items from container
container[0] = ItemStack(item = apple, count = 15) // fill slot 0 with an apple
container[2] = ItemStack(item = sword) // fill slot 2 with a sword 

container.commit() // after doing all modifications unlock the container and notify all listeners that it changed
```

### Single edits

All examples below handle automatic locking for you. Beware if you do multiple changes

#### Removing item

```kotlin
val container: Container = …

// use one of the following ways to remove slot 0
container[0] = null //  preferred way
container -= 0
container.remove(0)
```

#### Adding item

```kotlin
val container: Container = …

// use one of the following ways to add an item to slot 0
container[0] = item // preferred way
container.set(0, item)
```

#### Modifying item

##### Modifying single property

```kotlin
val container: Container = …

val item: ItemStack? = container[0]
if (item != null) {
    item.item.count = 7
}
```

##### Modifying multiple properties

To prevent inconsistency you must lock the item first, otherwise it might have already changed, before you do the next edit.

```kotlin
val container: Container = …

val item: ItemStack? = container[0]
if (item != null) {
    item.lock()
    item.item.count = 7
    item.enchanting …
    item.commit()
}
```

## Getting items

### Getting item

```kotlin
val container: Container = …

// use one of the following ways to get an item from slot 0
val item: ItemStack? = container[0] // preferred way
val item: ItemStack? = container.get(0)
```

### Iterating

```kotlin
val container: Container = …

container.lock.acquire() // acquire lock to prevent errors and consistency
// this is only iterating over non-empty slots
for ((slot, stack) in container) {
    println("I got $stack in slot $slot")
}
container.lock.release() // allow further updates again
```

It is important that you iterate as fast as possible to prevent other components from editing the container.  
Releasing the container again is also really important, this always needs to be called, otherwise the container is locked forever. 
