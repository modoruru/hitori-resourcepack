# resourcepack
[English version](./README.md)

Это репозиторий содержит модуль resourcepack для [hitori](https://github.com/modoruru/hitori) фреймворка.\
Этот модуль в основном состоит из API для других модулей. Сам по себе он почти не содержит механик.
## Основные особенности
- Кастомные блоки
  - через баррьеры: позволяет установить несколько блоков за раз (например для лавочек)
  - через interaction энтити: позволяет кастомизировать размер хитбокса
  - можно взаимодействовать с ними (могут излучать свет, работать как сиденье и менять внешний вид)
- Конвейер генерации кастомного контента (кастомные предметы, item model'ы, глифы, звуки, переводы, кастом блоки и другие ассеты)
- Локальный хостинг ресурспака
- Позы (/sit, /lay, /crawl)

## Использование
Вы можете получить jar во вкладке [Actions](https://github.com/modoruru/hitori-resourcepack/actions). Модуль собран после почти каждого коммита.\
Также, вы можете получить модуль из [Releases](https://github.com/modoruru/hitori-resourcepack/releases) (если они есть).

После скачивания jar, просто поместите его в папку hitori. После, перезагрузите сервер.
## API
Модуль опубликован через [JitPack](https://jitpack.io/)

Последняя версия: [![](https://jitpack.io/v/modoruru/hitori-resourcepack.svg)](https://jitpack.io/#modoruru/hitori-resourcepack)

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

## Отдельное спасибо
[GSit](https://github.com/gecolay/GSit) - для референса логики поз (и некоторые части кода)