# MediBuddy

## Highlights

MediBuddy is a medication management app designed to help users stay on top of their prescriptions and daily routines.

- Search for medications quickly using openFDA data
- Save medications with dosage and type details
- Build medication schedules and reminders
- Track consistency over time
- Alert emergency contacts when medications are missed
- Support calendar and export-related workflow features

## Overview

MediBuddy was created as a CS411 group project focused on making medication tracking simpler and more reliable. The app is designed for users who may have trouble remembering what to take, when to take it, and how to stay consistent over time. In addition to medication lookup and scheduling, the project explores features like reminders, contact alerts, and calendar integration to support day-to-day medication management.

### Authors

- [Natalie Kochova](https://github.com/nataliekochova)
- [Kristian Plonski](http://github.com/kristian-pl)
- [Mohammad Shaban](http://github.com/mohammadshaban05)
- [David Bunger](http://github.com/drb1220)

## Usage

After launching the application, users can search for medications, save them to a personal list, assign dosage and schedule information, and manage reminders from the JavaFX interface. Some builds of the project also include features for syncing medication reminders to Google Calendar and sending email-based alerts.

## Installation

### Requirements

- Java JDK 17 or newer
- Maven
- Git

Java 17+ should work for the project. Team members have also used Java 21 and Java 25 during development.

### Setup

```bash
git clone https://github.com/nataliekochova/CS411-MediBuddy.git
cd CS411-MediBuddy
cd medibuddy
java -version
mvn -version
```

### Run

```bash
mvn clean javafx:run
```

### Optional Configuration

Some features require local configuration before they can be used:

- Google Calendar integration expects a `credentials.json` file in `medibuddy/src/main/resources`
- Email-related features expect a `config.properties` file with the needed API key values

If those files are not configured, the core application can still be reviewed and run, but integration features may not work.