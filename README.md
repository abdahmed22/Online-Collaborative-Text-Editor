# Online Collaborative Text Editor

## Overview

This is a simplified real-time collaborative plain text editor implemented in Java. The application enables multiple users to simultaneously edit a shared document via unique shareable codes. Each document supports two collaboration modes: **Editor** and **Viewer**, with proper permission handling. The project leverages a custom tree-based CRDT (Conflict-free Replicated Data Type) for real-time character-level editing and concurrency resolution.

---

## Features

### Document & Collaboration Management
- **Import/Export TXT files**: Preserves original formatting and line breaks.
- **Shareable Codes**: Each document generates two unique codes:
  - **Editor Code**: Allows full edit access.
  - **Viewer Code**: Read-only access, with limited UI.

### Real-time Collaborative Editing
- **Character-based Insertion/Deletion**: Including support for text pasting.
- **Concurrent Editing Support**: Using a custom **tree-based CRDT** algorithm.
- **Live Synchronization**: All edits are relayed in real time via a central server.
- **Cursor Tracking**: Visually distinct cursor per active user.
- **User Presence**: Displays a live list of active users in the session.
- **Undo/Redo**: Each user can undo/redo their last 3 actions independently.
---

## How to Run

### Requirements
- Java Development Kit (JDK) 17 or higher
- Maven (for building and managing dependencies)

### Running the Application

1. **Clone the repository** or unzip the provided folder.

2. **Starting the Server**:
   - Run the server component first.
   - Example:
     ```bash
        mvn clean install
        mvn spring-boot:run
     ```

4. **Running Clients**:
   - Launch one or more instances of the client application.
   - Example:
     ```bash
      mvn clean install
      mvn spring-boot:run
     ```

5. **Enjoy the Text Editor!!**
   
