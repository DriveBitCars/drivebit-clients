package my.drivebit.search

fun searchFilterChipOrder(includeReset: Boolean): List<String> =
    buildList {
        if (includeReset) {
            add("Сбросить")
        }
        add("Марка")
        add("Привод")
        add("Кузов")
        add("Количество мест")
        add("Год выпуска")
        add("Километраж")
        add("Цена")
    }
