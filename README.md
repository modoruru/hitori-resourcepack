# resourcepack
[Русская версия](./README_ru.md) | [Contributing](https://github.com/modoruru/.github/blob/main/CONTRIBUTING.md)

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
<details>
<summary>maven</summary>

```xml
<repository>
  <id>modoru-releases</id>
  <name>modoru repository</name>
  <url>https://repository.modoru.fun/releases</url>
</repository>
```

```xml
<dependency>
  <groupId>su.hitori</groupId>
  <artifactId>hitori-resourcepack</artifactId>
  <version>1.1.3</version>
</dependency>
```
</details>
<details>
<summary>gradle</summary>

```groovy
maven {
  name = "modoruReleases"
  url = uri("https://repository.modoru.fun/releases")
}
```

```groovy
dependencies {
    // ...
    implementation 'su.hitori:hitori-resourcepack:1.1.3'
}
```
</details>

## Special thanks
[GSit](https://github.com/gecolay/GSit) - for poses logic reference (and some code parts)