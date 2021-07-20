# BleConf

## dev

Для работы с проектом требуется beta-версия [Android Studio][1] - Arctic Fox (на момент написания Beta 5). Можно использовать встроенный JDK, либо установить [AdoptOpenJDK 11][2].

Все зависимости будут скачаны при первой сборке проекта.

Структура проекта:
- используемая архитектура - MVVM
- интерфейс - [Jetpack Compose][3] (пока в стадии beta/RC)
- l10n - [стандартная Android][4] (ресурсные файлы strings.xml), [пример][7]
- заставка - [тема SplashTheme][5] на момент старта приложения
- тема - стандартная [MaterialTheme][6] из Compose
- аналитика сбоев - [Firebase Crashlytics][8] (в проект androidApp нужно добавить файл [google-services.json][9])

[1]: https://developer.android.com/studio/preview
[2]: https://adoptopenjdk.net/
[3]: https://developer.android.com/jetpack/compose
[4]: https://developer.android.com/guide/topics/resources/localization
[5]: https://blog.davidmedenjak.com/android/2017/09/02/splash-screens.html
[6]: https://developer.android.com/jetpack/compose/themes
[7]: https://medium.com/i18n-and-l10n-resources-for-developers/a-deep-dive-into-internationalizing-jetpack-compose-android-apps-e4ed3dc2809c
[8]: https://firebase.google.com/docs/crashlytics
[9]: https://firebase.google.com/docs/android/setup#kotlin+ktx
