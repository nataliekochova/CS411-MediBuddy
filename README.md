# CS411-MediBuddy


- Java JDK 17+ (I'm using Java 25 locally, but 17+ should work, though you may have to change the pom.xml file locally)
- Maven (https://maven.apache.org/install.html)
- Git
- VS Code or IntelliJ recommended

Setup
- git clone github.com/nataliekochova/CS411-MediBuddy/
- cd medibuddy
- check that java and mvn versions match with "java -version" and "mvn -version"

Run
(while in CS411-MediBuddy/medibuddy directory)
- mvn clean javafx:run

Layout
src/
└── main/
    ├── java/
    │   └── com/
    │       └── medibuddy/
    │           ├── App.java
    │           ├── client/
    │           │   └── OpenFdaClient.java
    │           ├── model/
    │           │   ├── DrugLabelResult.java
    │           │   ├── OpenFdaResponse.java
    │           │   └── SavedMedication.java
    │           ├── service/
    │           │   └── MedicationStore.java
    │           └── ui/
    │               ├── AppShell.java
    │               ├── HomePage.java
    │               ├── SearchPage.java
    │               ├── MedicationDetailPage.java
    │               ├── SchedulePage.java
    │               ├── InteractionsPage.java
    │               └── SettingsPage.java
    └── resources/
        ├── style.css
        └── icons/
            ├── meds.png
            ├── meds_active.png
            ├── schedule.png
            ├── schedule_active.png
            ├── interactions.png
            ├── interactions_active.png
            ├── settings.png
            └── settings_active.png

App.java: Mostly just launches the app and transfers things to AppShell.java
AppShell.java: Framework for navigation bar and controller for page navigation
OpenFdaClient.java: Handles openFDA API calls
OpenFdaResponse.java: Object for openFDA responses
DrugLabelResult.java: Object one individual medication from openFDA response
SavedMedication.java: Simplified medication data to store in personal list
MedicationStore.java: List of saved medications (probably will be changed with database stuff)

HomePage.java: medications list page
    SearchPage.java: medication search within home page
    MedicationDetailPage.java: details page for an individual medication
SchedulePage.java: 
InteractionsPage.java:
SettingsPage.java:

style.css: UI styling formatting

https://code.visualstudio.com/docs/sourcecontrol/branches-worktrees

This has been very helpful for figuring out git source control on vscode ^^^