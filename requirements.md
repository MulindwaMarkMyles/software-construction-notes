# Software Construction Notes - Requirements Document

## 1. Introduction

NOTED is a personalized Android notes-taking application that will be programmed using Python with the sole aim of making it portable, practical and user-friendly.

### 1.1 Application Scope

NOTED will primarily allow users to create, delete, organize and manage notes as required with additional capabilities of rich text formatting, notes sharing and categorization.

## 2. Functional Requirements

The application will have to meet basic user needs in regards to notes creation and management:

- Create, Edit and Delete notes
- Organize notes (by last date modified, urgency)
- Rich Text Formatting (Bolding, Italicizing, Underlining)
- Tagging and categorization
- Search function to locate notes
- User Interface for interaction with the application

## 3. Non-Functional Requirements

- Performance: As a portable application, it should be quick to load
- Usability: The user interface should be easy to use
- Scalability: The app should be able to contain hundreds of notes
- Compatibility: The app should be compatible with Android 13-14
- Capacity: It should be a lightweight application barely exceeding 100MBs of storage

## 4. Acceptance Criteria

### 4.1 Create/Edit/Delete Notes

- Users can create, edit and delete notes
- A note should have characters to be saved (empty notes cannot be saved)

### 4.2 Organizing Notes

- Notes created should be arranged chronologically by dates created and dates modified
- Arranging notes by tags and categories should be possible

### 4.3 Collaboration

- Notes should be able to be shared

### 4.4 Search Function

- Notes should be able to be searched for using keywords
