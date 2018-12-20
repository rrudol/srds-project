# Symulacja przydziału miejsc w przedziałach w pociągu
Projekt przygotowany w ramach laboratoriów Systemy Rozproszone Dużej skali 

## Wprowadzenie do problemu
Analizowany przez nas temat dotyczy rezerwacji przejazdów kolejowych. Podróżny kupujący bilety zwykle nie podróżuje sam i kupując bilety dla grupy osób chciałby mieć pewność, iż znajdzie się z nimi w tym samym przedziale, a nie na drugim końcu pociągu. 
Rozpatrujemy zatem ciąg klientów (napływających w losowych odstępach czasu), w którym każdy klient określa, ile miejsc chce zarezerwować. Każdy pociąg składa się z przedziałów z ograniczoną liczbą miejsc.

Przykładowe rozwiązanie:

```
Ciąg klientów: 1, 2, 3, 3, 2, 1
Pojemność przedziałów w pociągu: [3, 3, 3]

Przydział do wagonów:
1] 1, 2
2] 3
3] 3

Klienci, którym musimy odmówić: 2, 1 (ostatni dwaj)
```

Ciąg klientów jest uporządkowany względem pojawienia się ich w systemie i obsłużenia przez system.
 
## Klasyczne rozwiązanie bazujące na bazie relacyjnej
W bazie danych reprezentujemy klientów, pociągi, miejsca w przedziałach i rezerwacje miejsc. Klient deklaruje ilość miejsc, które chciałby zarezerwować, a system w ramach jednej transakcji pobiera stan pociągu i znajduje wagon, który pomieści grupę.
Takie rozwiązanie zapewnia nam spójność danych, jeśli jednak uwzględnimy przypadek, w którym klient może zrezygnować z biletu może wystąpić następująca sytuacja:

```
Strumień klientów: 3, 2*, 4, 2, 4
Pojemność przedziałów w pociągu: [5, 5, 5]
// Klienci rozpatrywani są sekwencyjnie dzięki zastosowaniu transakcji
// * - klient zrezygnuje z biletu

Przydział do wagonów:
1] 3, 2*
2] 4
3] 2

Klienci nie przydzieleni: 4

// Drugi klient rezygnuje z dwóch miejsc z wagonu pierwszego

Przydział do wagonów:
1] 3
2] 4
3] 2
Klienci nie przydzieleni: 4
```

Jak można zauważyć w powyższym przykładzie, przyznawanie przy sprzedaży biletu numerowanych miejsc może doprowadzić do sytuacji, w której musimy odmówić klientowi, mimo iż miejsc mamy wystarczająco. Jeśli jednak nie przyznamy miejsc na początku, w tej architekturze, nie zwalnia nas to z konieczności przekładania klientów między wagonami, gdy któryś zrezygnuje z miejsca.

Innym ważnym czynnikiem może być problem z rozproszeniem takiej bazy danych.

## Rozwiązanie bazujące na bazie danych Cassandra z partycjonowaniem
Powyżej przedstawione rozwiązanie nie sprawdzi się jednak z bazą danych typu Cassandra. Brak transakcji doprowadzi do nadpisywania się wzajemnie rezerwacji klientów. Wymaga to od nas przeprojektowania systemu.

Proponowany schemat bazy danych:

```
CREATE TABLE IF NOT EXISTS ticketRequest (
   id uuid,
   trainId int,
   customerId int,
   seats int,
   timestamp bigint,
   PRIMARY KEY (trainId, id)
);
```

Uznaliśmy, iż do rozwiązania tego problemu wystarczy jedna tabela przechowująca zlecenia rezerwacji oraz ich odwołań. Dla zwiększenia czytelności rozwiązania pominęliśmy tabelę reprezentującą sprzedane bilety oraz pociągi.

Klient pojawia się w systemie, decyduje ile miejsc chce zarezerwować i tworzy zlecenie rezerwacji, które jest zapisywane w bazie. Po odczekaniu z góry określonego czasu, wykonuje zapytanie do bazy o wszystkie zlecenia rezerwacji, które wystąpiły do tej pory. Na ich podstawie, korzystając z algorytmu przydzielającego miejsca (z którego skorzysta system przy ostatecznym wygenerowaniu biletów) jest w stanie określić, czy rezerwacja zostanie zrealizowana. 

Jeśli decyzją jest odrzucenie rezerwacji wówczas klient rezygnuje z przejazdu i wysyła zlecenie anulowania wcześniejszego zlecenia. Może też pozostawić swoje zlecenie, w ten sposób znajdzie się na liście rezerwowej. Jeśli zwolnią się miejsca, w chwili zamknięcia rezerwacji, jego rezerwacja może jeszcze zostać zrealizowana.

Jeśli decyzją jest przyjęcie rezerwacji klient może z niej zrezygnować wysyłając zlecenie anulowania rezerwacji. W ten sposób zwalniając miejsca dla innych klientów. Rezygnacja może zmienić rozkład pasażerów w przedziałach, ale nie zmieni decyzji co do klientów, którym zgoda została udzielona. Gdy jednak nie zrezygnuje to po zamknięciu rezerwacji sprawdza czy jego została zgodnie z obietnicą zrealizowana. Jeśli nie, zgłasza zażalenie.


## Parametry

W pliku Main.java można ustawić wszystkie parametry, jakie mają wpływ na wielkość, czas i jakość rozwiązania.
Na samym początku ustawiamy id pociągu, do którego będą składane wszystkie rezerwacje, potem możemy zmienić jego właściwości takie jak liczba przedziałów oraz ich pojemność. Ta ostatnia będzie również maksymalną liczbą miejsc jaką może zakupić klient. Te parametry muszą być takie same na wszystkich instancjach programu. Następnie podajemy liczbę klientów (niezależnych wątków) oraz przedział czasu w jakim będą się pojawiać w systemie. Ostatnim parametrem jest czas między zgłoszeniem się do systemu a otrzymaniem decyzji.  

## Wnioski

Naszym celem zatem jest zminimalizowanie ilości zażaleń. Po testach okazało się, że klienci zgłaszają zażalenia, jeśli dane potrzebne do wydania poprawnego werdyktu nie zdążą się zreplikować i klient pobierze dane z nieaktualnej repliki. Ważne jest, więc każdy klient pobrał dane z repliki, która posiada wszystkie zlecenia, które napłynęły przed jego własnym. Aby to osiągnąć należy opóźnić czas pomiędzy wysłaniem zlecenia, a pobraniem danych (pozostawienie czasu na replikacje). Zauważyliśmy, że tego typu praktyka zdaje się być wykorzystywana w wielu aplikacjach internetowych. Czas ten powinien zostać dobrany uwzględniając obciążenie systemu w danym momencie.

Rozwiązanie zaprezentowane w punkcie 3. okazuje się mieć więcej zalet niż klasyczne. Oprócz partycjonowania zyskaliśmy wydajniejsze przetwarzanie zleceń klientów. Baza danych została odciążona i przetwarza tylko lekkie operacje, które dodatkowo są rozpraszane na repliki. 
