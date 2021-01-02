# Deliverable 2
[![Build Status](https://travis-ci.org/giuseppelasco17/Deliverable2_Lasco.svg?branch=master)](https://travis-ci.org/giuseppelasco17/Deliverable2_Lasco)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=giuseppelasco17_Deliverable2_Lasco&metric=alert_status)](https://sonarcloud.io/dashboard?id=giuseppelasco17_Deliverable2_Lasco)
##Breve manuale d'uso
Il programma legge le informazioni di configurazione dal file `/Deliverable2_Lasco/config.txt`.

La prima riga rappresenta il percorso in cui risiede il progetto da analizzare
(e.g.`C:\Users\Nome-utente\Desktop\bookkeeper`), la seconda rappresenta l'URL da cui scaricare la repository
(e.g.[`https://github.com/apache/bookkeeper.git`](https://github.com/apache/bookkeeper.git)), la terza rappresenta il 
nome del progetto da analizzare (e.g.`bookkeeper`), mentre la quarta, se inserita, rappresenta il modo in cui 
l'applicazione deve eseguire il retrieve della lista completa dei file. Inserendo `file`, nella suddetta riga, infatti, 
viene eseguito il retrieve dal file `/Deliverable2_Lasco/NOMEPROGETTOFiles.csv` della lista delle classi java del progetto, altrimenti, se 
lasciata vuota, viene eseguito tramite Git CLI, molto più lentamente.



Una volta terminata la configurazione è possibile far partire la prima milestone del programma dal main presente in
`/Deliverable2_Lasco/src/milestone_one/FirstMileController.java`. Il risultato è riportato nel file
`/Deliverable2_Lasco/NOMEPROGETTODataset.csv`

Sulla base dei risultati restituiti è possibile avviare la seconda milestone dal main presente in
`/Deliverable2_Lasco/src/milestone_two/SecondMileController.java`. Il risultato è riportato nel file
`/Deliverable2_Lasco/NOMEPROGETTOResults.csv`