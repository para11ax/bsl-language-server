# Типы и важность диагностик

Каждая диагностика относится к определенному типу и имеет определенную важность.  
Для того, чтобы выбрать тип и важность для создаваемой диагностики, ниже приведено их подробное описание.

## Важность

Важность диагностики указывается в аннотации `@DiagnosticMetadata` параметром `severity` и принимает значения типа `DiagnosticSeverity`.

Возможные варианты (общие случаи, если не сказано иного):

### Блокирущий (BLOCKER)

Ошибки, приводящие приложение в нерабочее состояние. Используется только для `Ошибок` и `Уязвимостей`.

### Критичный (CRITICAL)

Неправильно работающая ключевая бизнес-логика, дыры в системе безопасности, проблемы, приводящие к временной неработоспособности приложения или его компонент. Используется только для `Ошибок` и `Уязвимостей`.

### Важный (MAJOR)

Часть основной бизнес-логики работает некорректно, но есть обходные пути либо низкокачественный код, приводящий к проблемам производительности, эффективности, плавающим ошибкам. Используется только для `Ошибок` и `Дефектов кода`.

### Незначительный (MINOR)

Не нарушается бизнес-логика приложения, плавающая ошибка, низкокачественный, слабоподдерживаемый код, ошибки в редкоиспольземой функциональности. Используется только для `Ошибок` и `Дефектов кода`.

### Информационный (INFO)

Тривиальная ошибка, не касающаяся бизнес-логики приложения, плохо воспроизводимая проблема, малозаметная, не оказывающая никакого влияния на общее качество продукта. Используется только для `Дефектов кода`.

## Тип диагностики

Тип диагностики указывается в аннотации `@DiagnosticMetadata` параметром `type` и принимает значения типа `DiagnosticType`.

### Уязвимость (VULNERABILITY)

К этому типу диагностик относятся ошибки безопасности. Они всегда должны иметь важность `Блокирующий` в случае наличия известного способа компрометации, или `Критичный` если его нет либо ценность утечки не высока.  
Примеры

- компрометация персональных данных является блокирующей уязвимостью, т.к. кроме нарушения законодальства, полученная информация может быть использована в различных противоправных действиях.
- компрометация настроек отчетов пользователя относится к критичной уязвимости, т.к. может подсказать злоумышленнику как собрать важный отчет, но не дает доступа для выполнения запроса.

### Потенциальная уязвимость (SECURITY_HOTSPOT)

В отличие от уязвимостей, диагностики с данным типом выделяют фрагменты кода, чуствительные к безопасности, требующие дополнительного ручного анализа. После анализа либо будет обнаружена проблема, которую необходимо исправить, либо проблемы нет и стоит отметить замечание как неактуальное.
Диагностики с данным типом всегда должны иметь важность `Критичный`.
  
Примеры

- обращение к параметрам операционной системы, комьютера пользователя не всегда является уязвимостью, так например версия архитектуры ОС может быть использована для загрузки нужной версии библиотки, входящей в состав приложения.
- повышение привилегий пользователя может происходить запланировано, на время выполнения операции в соответствии с бизнес-процессом, что не явлется уязвимостью, в отличие от ситуации отключения контроля прав доступа пользователя полностью.

### Дефект кода (CODE_SMELL)

`Дефект кода` не приводит к ошибкам в программе, но усложняет дальнейшую разработку, возможность адаптировать и расширять функционал. Может иметь любую важность, кроме `Блокирующий` и `Критичный`. Требует исправления по мере важности в процессе рефакторинга.

### Ошибка (ERROR)

К данной категории относятся реальные ошибки при работе пользователя. Они могут быть любой важности, кроме `Информационный`, при этом:

- `Блокирующий` означает отсутствие известного способа обхода, необходимо срочно исправить. Примером такой ошибки является некомпилиремый код или обращение к несуществующему методу.
- `Критичный` означает наличие известного способа обхода (например отключения функциональности с ошибкой), но требующий максимально оперативного исправления.