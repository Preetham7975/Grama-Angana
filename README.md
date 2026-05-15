<p align="left">
  <img src="https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" />
</p>

## Problem Statement
Rural community halls are often underutilized due to opaque booking systems and a lack of transparent funding for minor repairs. Grama-Angana solves this by replacing informal "keyholder" dependencies with a real-time digital calendar and a community "Maintenance Jar" for crowdfunding upkeep via UPI.

## Features
* **GenAI Impact Reporting:** (Future Scope/Implementation) Integrates with Google Gemini AI to analyze monthly facility usage and automatically draft localized community impact reports for Panchayat officials.
* **Unified Authentication:** Secure login using Firebase Email/Password.
* **Facility Booking System:** Real-time calendar UI preventing double-bookings using Firestore Transactions.
* **Maintenance Jar:** Crowdfunding module with visual progress bars to track repair goals.
* **UPI Integration:** Native intent triggers to launch external payment apps (Google Pay, PhonePe).

## Tech Stack
* **Language:** Kotlin
* **UI:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Backend & DB:** Firebase Authentication, Cloud Firestore
* **Local Cache:** Room DB

## Setup and Installation Instructions
To install and run this project on your local machine, follow these setup steps:
1. Clone the repository: `git clone https://github.com/Preetham7975/Grama-Angana.git`
2. Open the project in **Android Studio**.
3. Allow Gradle to sync and download all dependencies.
4. Add your `google-services.json` file to the `app/` directory (required for Firebase).
5. Click the **Run** button (Shift + F10) to deploy to an emulator or connected Android device.

## Screenshots
<table align="center">
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/197976d8-6155-4044-9158-8c5f172b7aa1" width="250" alt="Login Screen" />
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/0345dee4-53f5-449c-a59a-14ae1e24ac97" width="250" alt="Calendar Booking" />
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/89cfcb93-b4ab-4a02-a536-f8a1e30e2e7b" width="250" alt="Maintenance Jar" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Login Screen</b></td>
    <td align="center"><b>Calendar Booking</b></td>
    <td align="center"><b>Maintenance Jar</b></td>
  </tr>
</table>

## Folder Structure
* `app/src/main/java/.../viewmodels` - Contains all MVVM business logic.
* `app/src/main/java/.../screens` - Contains Jetpack Compose UI elements.
* `app/src/main/java/.../data` - Contains Room DB and Firebase repositories.
