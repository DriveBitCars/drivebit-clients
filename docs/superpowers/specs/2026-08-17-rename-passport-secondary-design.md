# Rename passport secondary page label — design

Date: 2026-08-17  
Status: approved in brainstorming  
Issue: https://github.com/DriveBitCars/drivebit-clients/issues/361

## Problem

The documents page labels the second Russian passport slot «Вторая страница паспорта». Users need that slot to be named as registration (прописка).

## Goals

- Show «Прописка» as the title of the second passport document slot on the web documents page.

## Non-goals

- Renaming «Первая страница паспорта» or the section header «Паспорт».
- Changing upload type `PassportSecondaryPageRus` or the backend path `upload/passport-secondary`.
- Mobile UI (this label exists only on web).
- Extracting document-slot titles into a shared strings module.

## Decision

Change only the visible slot title from «Вторая страница паспорта» to «Прописка» (capitalized to match sibling slot titles).

## Files to change

- `DrivebitWeb/src/jsMain/kotlin/my/drivebit/screens/DocumentsPage.kt` — `DOCUMENT_SLOTS` title for `PassportSecondaryPageRus`

## Tests

- No new unit/UI test: this is a one-string display title with no existing DocumentsPage tests.
