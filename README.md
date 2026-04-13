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
    │               ├── HomePage.java
    │               ├── SearchPage.java
    │               └── MedicationDetailPage.java
    └── resources/
        └── style.css

App.java: controls navigation between pages
OpenFdaClient.java: Handles openFDA API calls
OpenFdaResponse.java: Object for openFDA responses
DrugLabelResult.java: Object one individual medication from openFDA response
SavedMedication.java: Simplified medication data to store in personal list
MedicationStore.java: List of saved medications (probably will be changed with database stuff)
HomePage.java: UI for homepage
SearchPage.java: UI for search page
MedicationDetailPage.java: UI for details page for an individual medication
style.css: UI styling formatting