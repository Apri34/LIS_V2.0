package com.lis.lis

/**
 * * * * * Autor: Andreas Pribitzer * * * * *

 Da wir im Zuge dieser Diplomarbeit das erste Mal eine App programmiert haben,
 mangelte es uns zu Beginn an Erfahrung und Wissen. Deshalb befinden
 sich in der ersten Version einige Unreinheiten, unsaubere Programmzeilen und
 Bugs, die zu einem Programmabsturz führen können. Aus diesem Grund wurde eine
 zweite Version geschrieben, welche keine dieser unsauberen Implementierungen
 enthalten soll. Grundsätzlich war die Funktion in der ersten Version gegeben,
 weshalb sich ein Großteil des Codes stark ähnelt. Unten finden Sie eine
 Auflistung, was genau in der zweiten Version abgeändert wurde.
 Anzumerken ist, dass in unserer schriftlichen Arbeit nur die erste
 Version behandelt wurde, da diese Version erst nach der Abgabe programmiert wurde.
 Die Grundfunktionen bleiben allerdings gleich.

 + Die meisten Klassen wurden aufgrund der Präferenzen des Programmierers
 nun in Kotlin geschrieben, und nicht in Java. Kotlin ist mit Java kompatibel.

 + Die Datenbank wurde komplett neu überarbeitet. Die SQLiteOpenHelper Klasse
 ist teilweise kompliziert zu implementieren und enthält daher in unserem Programm
 einige Fehleranfälligkeiten. In sämtlichen Tests wurden ohne bekanntem Grund
 Tabellen gelöscht, was zu einigen Verzögerungen geführt hat. Die Room library
 bietet eine saubere, stabilere und einfachere Implementierung.

 + Zugriffsmodifizierer gesetzt und viele unnötige Codepassagen gelöscht
 bzw. unnötig lange Passagen gekürzt.

 + HintAdapter hinzugefügt: ArrayAdapter für spinner, die zu Beginn
 "Select ..." anzeigen. Sobald man etwas ausgewählt hat, verschienet diese Auswahl.

 + Beim Löschen wird ein AlertDialog zum Bestätigen aufgerufen.
*/