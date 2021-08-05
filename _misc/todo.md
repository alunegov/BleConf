# TODO

- [x] автоматическая синхронизация времени при подключении к серверу
- [x] получать Coeff через subscribe (notify на сервере)
- [x] настройки сервера:
  - ADC_COEFF (float)
  - ADC_EMON_NUM?
  - ADC_AVER_NUM?
  - ADC_IMBA_NUM?
  - ADC_IMBA_MIN_CURRENT? (float)
  - ADC_IMBA_MIN_SWING? (float)
  - ADC_IMBA_THRESHOLD (float)
- [x] пароль для доступа к настройкам сервера
- [x] ui (отступы, расположение, цвета и т.д.)
- [x] splash screen
- [x] "красивый" checkbox включения/выключения датчика (как на iOS, и справа?)
- [x] "ждите" на время долгих операций
- [x] отдельный текст для пустых списков
- [x] l10n
- [x] описание или имя датчика через ble-дескриптор
- [ ] включение BT-адаптера?, локации?
- [x] переименование проекта (com.github.alunegov.bleconf)
- [ ] swipe to refresh
- [ ] сообщения об ошибках через snackbar
- [ ] l10n общего (не android) кода (вьюмодели, домен, сервисы)
- [ ] валидация значений настроек

## features

- [ ] core.splashscreen (когда перейдём на API level 31)
- [ ] использовать JSON для передачи истории (см. BleServerConnImpl::decodeHistory)

## ext

- название
- launcher icon (желательно векторная)
- цвета
- пароль настроек - 7777
