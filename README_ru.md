# resourcepack
[English version](./README.md) | [Контрибьютинг](https://github.com/modoruru/.github/blob/main/CONTRIBUTING_ru.md)

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

## Отдельное спасибо
[GSit](https://github.com/gecolay/GSit) - для референса логики поз (и некоторые части кода)