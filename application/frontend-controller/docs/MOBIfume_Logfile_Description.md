# MOBIfume Logfile Beschreibung

Die Logdateien werden unter `%userprofile%/documents/MOBIfume/logs/` gespeichert.
Es wird in zwei verschiedene Arten von Logdateien mit entsprechender Dateiendung unterschieden:
- `.log`: Globale Logdatei
- `.run`: Gruppen Logdatei (jeweils für einen Raum)

Die Logdateien sind in reiner Textform abgespeichert. Die Zeilen in einer Logdatei werden mit `CR LF` (Windows) getrennt.

## .log (Globale Logdatei)

Eine neue globale Logdatei wird bei jedem Start des Programms erzeugt und speichert alle globalen Ereignisse.

Jede Zeile beginnt mit: `{datetime} {log-level} -`

<br>

### Erste Zeile:
> {application}:{version}

### Broker Connection Information
> ConnectionInfo;{broker-ip};{connection-type};{port};{id};{user};{password}
- broker-ip: 192.168.1.1
- connection-type: tcp://
- port: 1883
- id: Zufällig generiert für aktuelle Session
- user: Benutzername für die Anmeldung zum Broker
- password: Passwort des entsprechenden Benutzers

### Broker-Nachrichten

Dem Broker gesendete oder vom Broker empfangene Nachrichten werden über einen Channel mit einer Payload geschickt. Ein Channel beginnt immer mit `/MOBIfume/`. Mit `;` getrennt folgen die Parameter der Payload.

#### Lufbefeuchter online
> /MOBIfume/hum/status/{id};ONLINE;{version}
- id: Geräte-Id
- version (int): Aktuelle Version des Geräts (laufende Nummer)

#### Heizplatte online
> /MOBIfume/base/status/{id};ONLINE;{version}
- id: Geräte-Id
- version (int): Aktuelle Version des Geräts (laufende Nummer)

#### Luftbefeuchter offline
> /MOBIfume/hum/status/{id};OFFLINE
- id: Geräte-Id

#### Heizplatte offline
> /MOBIfume/base/status/{id};OFFLINE
- id: Geräte-Id

#### Ping einer Heizplatte
> /MOBIfume/base/status/{id};P;{rssi};{temperature};{humidity};{heater-setpoint};{heater-temperature};{latch}
- id: Geräte-Id
- rssi (int): Verbindungsstärke zum Router (Wlan); je näher an 0, desto besser die Verbindung
- temperature (double): Aktuell gemessene Raumtemperatur (-128: Sensorfehler)
- humidity (double): Aktuell gemessene Raumluftfeuchtigkeit (-128: Sensorfehler)
- heater-setpoint (double): Eingestellte Soll-Heizplattentemperatur
- heater-temperature (double): Aktuelle Heizplattentemperatur (-128: Sensorfehler)
- latch (int): Zustand der Verriegelung für die Filterung/Reinigung
  - -1: In Bewegung
  - 0: geschlossen
  - 1: geöffnet (Reinigung aktiv)
  - 2/3: Fehler beim Öffnen/Schließen

#### Ping eines Luftbefeuchters
> /MOBIfume/hum/status/{id};P;{rssi};{humidify};{led1};{led2};{over-temperature}
- id: Geräte-Id
- rssi (int): Verbindungsstärke zum Router (Wlan); je näher an 0, desto besser die Verbindung
- humidify (0|1): Zustand des Luftbefeuchters (inaktiv/aktiv)
- led1:
  - aus (0)
  - an (1)
  - blinkt (2): Wasser leer
- led2:
  - aus (0)
  - an (1)
  - blinkt (2)
- over-temperature (bool): Luftbefeuchter überhitzt

#### Kalibrierungseinstellungen der Sensoren
> /MOBIfume/base/status/{id};CALIB_DATA;{humidity-gradient};{humidity-offset};{temperature-gradient};{temperature-offset}
- id: Geräte-Id
- humidity-gradient: Gradient für die Kalibrierung des Luftfeuchtigkeitssensors
- humidity-offset: Offset für die Kalibrierung des Luftfeuchtigkeitssensors
- temperature-gradient: Gradient für die Kalibrierung des Temperatursensors
- temperature-offset: Offset für die Kalibrierung des Temperatursensors

#### Status eines Gerätes
> /MOBIfume/base/status/{id};S;{status}

> /MOBIfume/hum/status/{id};S;{status}
- id: Geräte-Id
- status:
  - RST: Gerät wurde zurückgesetzt
  - CMD_OK: Befehl erfolgreich ausgeführt
  - CMD_ERR: Fehler beim Ausführen des Befehls
  - PROCESS_START: Heizprozess wurde gestartet (nur bei base)
  - PROCESS_END: Heizprozess wurde beendet (nur bei base)

#### Soll-Heizplattentemperatur setzten
> /MOBIfume/base/cmd/{id};F;{heater-setpoint}
- id: Geräte-Id
- heater-setpoint (int): Soll-Heizplattentemperatur

#### Verriegelung für Filterung/Reinigung einstellen
> /MOBIfume/base/cmd/{id};L;{state}
- id: Geräte-Id
- state:
  - 0: schließen
  - 1: öffnen (Reinigung)

#### Heizdauer setzen
> /MOBIfume/base/cmd/{id};T;{duration}
- id: Geräte-Id
- duration (int): Dauer des Heizprozesses in Minuten

#### Gerät zurücksetzen
> /MOBIfume/base/cmd/{id};R;1

> /MOBIfume/hum/cmd/{id};R;1
- id: Geräte-Id

#### Kalibrierungseinstellungen anfordern
> /MOBIfume/base/cmd/{id};G
- id: Geräte-Id

#### Kalibrierungseinstellungen setzen
> /MOBIfume/base/cmd/{id};H;{humidity-offset}

> /MOBIfume/base/cmd/{id};I;{humidity-gradient}

> /MOBIfume/base/cmd/{id};Z;{temperature-offset}

> /MOBIfume/base/cmd/{id};Y;{temperature-gradient}
- id: Geräte-Id
- humidity-gradient: Gradient für die Kalibrierung des Luftfeuchtigkeitssensors
- humidity-offset: Offset für die Kalibrierung des Luftfeuchtigkeitssensors
- temperature-gradient: Gradient für die Kalibrierung des Temperatursensors
- temperature-offset: Offset für die Kalibrierung des Temperatursensors

#### Luftbefeuchtung ein-/ausschalten
> /MOBIfume/hum/cmd/{id};H;{humidify}
- humidify
  - 0: ausschalten
  - 1: einschalten


## .run (Gruppen-Logdatei)

Eine neue Gruppen-Logdatei wird bei jeder Erstellung einer neuen Gruppe (Raum) erzeugt und speichert alle Ereignisse, welche zu dieser Gruppe gehören.

Jede Zeile beginnt mit: `{datetime};{log-level};`

<br>

### Kopfzeile
> HEAD;{application}:{version};{cycle};{group}
- cycle (int): Zyklus-Nummer; Laufende Nummer, welche sich bei jedem neuen Raum erhöht
- group: Eingegebener Raumname

### Geräteliste
> DEVICES;{device-list}
- device-list: Eine Liste aller Geräte, die zu der Gruppe gehören
  - Einzelner Eintrag: `{id},{type}`

### Einstellungen
Wird nach Erstellung der Gruppe und bei Veränderung der Gruppeneinstellung ausgegeben.
> SETTINGS;{humidify-max};{humidify-puffer};{heater-temperature};{heat-time};{purge-time}
- humidify-max (int): Soll-Luftfeuchtigkeit
- humidify-puffer (double): Puffer nach oben bei der Verdampfung (Beispiel: Soll-Luftfeuchtigkeit=80%rH, Puffer=0.3 -> Wenn die Luftfeuchtigkeit über 80.3%rH steigt, schaltet der Luftbefeuchter aus. Wenn die Luftfeuchtigkeit wieder unter 80.0%rH fällt, schaltet er wieder ein.)
- heater-temperature (int); Heizplattentemperatur bei der Verdampfung
- heat-time (int): Dauer der Verdampfung in Minuten
- purge-time (int): Dauer der Reinigung in Minuten

### Prozessstatus
Wird bei jedem Zustandswechsel des Prozesses ausgegeben.
> STATE;{status};{is-humidifying};{is-humidify-max-reached}
- status:
  - START: Gruppe erstellt
  - HUMIDIFY: Luftbefeuchtungsprozess gestartet
  - EVAPORATE: Verdampfungsprozess gestartet (beginnt sobald die Soll-Luftfeuchtigkeit erreicht wurde)
  - PURGE: Reinigungsprozess gestartet (beginnt sobald der Verdampfungstimer ausgelaufen ist)
  - FINISH: Beendet (sobald der Reinigunstimer ausgelaufen ist)
  - RESET: Zurückgesetzt
  - CANCEL: Abgebrochen
- is-humidifying (bool): Aktueller Zustand des Luftbefeuchters (aktiv/inaktiv)
- is-humidify-max-reached (bool): Soll-Luftfeuchtigkeit erreicht

### Ping einer Heizplatte
> BASE;{id};{rssi};{temperature};{humidity};{heater-setpoint};{heater-temperature};{latch}
- id: Geräte-Id
- rssi (int): Verbindungsstärke zum Router (Wlan); je näher an 0, desto besser die Verbindung
- temperature (double): Aktuell gemessene Raumtemperatur (-128: Sensorfehler)
- humidity (double): Aktuell gemessene Raumluftfeuchtigkeit (-128: Sensorfehler)
- heater-setpoint (int): Eingestellte Soll-Heizplattentemperatur
- heater-temperature (double): Aktuelle Heizplattentemperatur (-128: Sensorfehler)
- latch (int): Zustand der Verriegelung für die Filterung/Reinigung
  - -1: In Bewegung
  - 0: geschlossen
  - 1: geöffnet (Reinigung aktiv)
  - 2/3: Fehler beim Öffnen/Schließen

### Ping eines Luftbefeuchters
> HUM;{id};{rssi};{humidify};{led1};{led2}
- humidify (bool): Zustand des Luftbefeuchters (aktiv/inaktiv)
- rssi (int): Verbindungsstärke zum Router (Wlan); je näher an 0, desto besser die Verbindung
- led1:
  - aus (0)
  - an (1)
  - blinkt (2): Wasser leer
- led2:
  - aus (0)
  - an (1)
  - blinkt (2)

### Abbruch eines Prozesses
> CANCEL

### Reinigung ein-/ausgeschaltet
> SET_HUMIDIFY;{humidifying}
- humidifying (bool): Reinigung aktiv/inaktiv

### State wird neu gesendet
Wenn ein Gerät die Verbindung verloren hatte und nun wieder verbunden ist, wird der aktuelle Zustand dem Gerät neu gesendet.
> SENDSTATE;{id};{type}
- type (BASE|HUMIDIFIER)

### Soll-Heizplattentemperatur geändert
> UPDATE_HEATERSETPOINT;{heater-setpoint}
- heater-setpoint (double): Neue Soll-Heizplattentemperatur

### Heizdauer update
> UPDATE_HEATTIMER;{evaporate-starttime};{heat-duration}
- evaporate-starttime (long): Start-Timestamp der Verdampfung
- heat-duration (int): Heizdauer

### Heizdauer zurückgesetzt
> RESET_HEATTIMER;{evaporate-starttime};{heat-duration}
- evaporate-starttime (long): Start-Timestamp der Verdampfung
- heat-duration (int): Heizdauer

### Reinigungsdauer update
> UPDATE_PURGETIMER;{purge-starttime};{purge-duration}
- purge-starttime (long): Start-Timestamp der Reinigung
- purge-duration (int): Reinigungsdauer

### Reinigungsdauer zurückgesetzt
> RESET_PURGETIMER;{purge-starttime};{purge-duration}
- purge-starttime (long): Start-Timestamp der Reinigung
- purge-duration (int): Reinigungsdauer

### Verbindungsverlust eines Gerätes
> DISCONNECT;{id}
- id: Geräte-Id
