# resourcepack
[Русская версия](./README_ru.md)

This repo contains resourcepack module for [hitori](https://github.com/modoruru/hitori) framework.\
This module is basically an API for another modules. It itself contains almost no mechanics.
## Main features
- Custom blocks
  - via barriers: allows several blocks to be placed at once (for example for benches)
  - via interaction entities: allows to customize hitbox size
  - can be interactable (emit light, work as a seat and change appearance)
- Custom content generation pipeline (custom items, item models, glyphs, sounds, translations, custom blocks and other assets)
- Resource pack local hosting
- Poses (/sit, /lay, /crawl)

## Usage
You can get a jar from [Actions](https://github.com/modoruru/hitori-resourcepack/actions) tab. Module is built almost after every commit.\
Also, you can get module from [Releases](https://github.com/modoruru/hitori-resourcepack/releases) (if there's any).

After downloading the jar, just put it into hitori folder. Then restart the server.

## API
This module is published via [JitPack](https://jitpack.io/)

Latest version: [![](https://jitpack.io/v/modoruru/hitori-resourcepack.svg)](https://jitpack.io/#modoruru/hitori-resourcepack)

<details>
<summary>maven</summary>

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

```xml
	<dependency>
	    <groupId>com.github.modoruru</groupId>
	    <artifactId>hitori-resourcepack</artifactId>
	    <version>version</version>
	</dependency>
```
</details>
<details>
<summary>gradle</summary>

```groovy
repositories {
    // ...
    maven { url 'https://jitpack.io' }
}
```

```groovy
dependencies {
    // ...
    implementation 'com.github.modoruru:hitori-resourcepack:version'
}
```
</details>

## Special thanks
[GSit](https://github.com/gecolay/GSit) - for poses logic reference (and some code parts)
[Meek](https://github.com/Meekiavelique) - for Animated-Java fork with plugin-mode fixed